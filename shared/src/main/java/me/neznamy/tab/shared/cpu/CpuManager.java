package me.neznamy.tab.shared.cpu;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class which measures CPU usage of all tasks inserted into it and shows usage
 */
public class CpuManager {

    private final int UPDATE_RATE_SECONDS = 10;

    /** Active time in current time period saved as nanoseconds from features */
    private volatile Map<String, Map<String, AtomicLong>> featureUsageCurrent = new ConcurrentHashMap<>();

    /** Active time in current time period saved as nanoseconds from placeholders */
    private volatile Map<String, AtomicLong> placeholderUsageCurrent = new ConcurrentHashMap<>();

    /** Last CPU report */
    @Nullable @Getter private CpuReport lastReport;

    /** Scheduler for scheduling delayed and repeating tasks */
    private final ScheduledExecutorService processingThread = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setNameFormat("TAB Processing Thread").build());

    /** Scheduler for placeholder refreshing task to prevent inefficient placeholders from lagging the entire plugin */
    @Getter
    private final ScheduledExecutorService placeholderThread = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setNameFormat("TAB Placeholder Refreshing Thread").build());

    /** Scheduler for refreshing permission groups */
    @Getter
    private final ScheduledExecutorService groupRefreshingThread = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setNameFormat("TAB Permission Group Refreshing Thread").build());

    /** Scheduler for checking for tablist entry values */
    @Getter
    private final ScheduledExecutorService tablistEntryCheckThread = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setNameFormat("TAB TabList Entry Checker Thread").build());

    /** Scheduler for encoding and sending plugin messages */
    @Getter
    private final ScheduledExecutorService pluginMessageEncodeThread = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setNameFormat("TAB Plugin Message Encoding Thread").build());

    /** Tasks submitted to main thread before plugin was fully enabled */
    private final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();

    /** Enabled flag used to queue incoming tasks if plugin is not enabled yet */
    private volatile boolean enabled;

    /** Boolean tracking whether CPU usage should be tracked or not */
    @Getter private boolean trackUsage;

    /**
     * Enables CPU usage tracking and returns {@code true} if it was not enabled previously.
     * If it was, does nothing and returns {@code false}.
     *
     * @return  {@code true} if this call enabled it, {@code false} if it was already enabled before
     */
    public boolean enableTracking() {
        if (trackUsage) return false;
        trackUsage = true;
        startRepeatingTask((int) TimeUnit.SECONDS.toMillis(UPDATE_RATE_SECONDS), () -> {
            lastReport = new CpuReport(UPDATE_RATE_SECONDS, featureUsageCurrent, placeholderUsageCurrent);
            featureUsageCurrent = new ConcurrentHashMap<>();
            placeholderUsageCurrent = new ConcurrentHashMap<>();
        });
        return true;
    }

    /**
     * Cancels all tasks and shuts down thread pools
     */
    public void cancelAllTasks() {
        processingThread.shutdownNow();
        placeholderThread.shutdownNow();
        groupRefreshingThread.shutdownNow();
        tablistEntryCheckThread.shutdownNow();
        pluginMessageEncodeThread.shutdownNow();
    }

    /**
     * Marks cpu manager as loaded and submits all queued tasks
     */
    public void enable() {
        enabled = true;
        Runnable r;
        while ((r = taskQueue.poll()) != null) {
            submit(r);
        }
    }

    /**
     * Submits task to TAB's main thread. If plugin is not enabled yet,
     * queues the task instead and executes once it's loaded.
     *
     * @param task task to execute
     */
    private void submit(@NotNull Runnable task) {
        if (processingThread.isShutdown()) return;
        if (!enabled) {
            taskQueue.add(task);
            return;
        }
        processingThread.submit(() -> run(task));
    }

    /**
     * Adds cpu time to specified feature and usage type
     *
     * @param feature     feature to add time to
     * @param type        sub-feature to add time to
     * @param nanoseconds time to add
     */
    public void addTime(@NotNull String feature, @NotNull String type, long nanoseconds) {
        if (!trackUsage) return;
        featureUsageCurrent.computeIfAbsent(feature, f -> new ConcurrentHashMap<>())
                .computeIfAbsent(type, t -> new AtomicLong()).addAndGet(nanoseconds);
    }

    /**
     * Adds placeholder time to specified placeholder
     *
     * @param placeholder placeholder to add time to
     * @param nanoseconds time to add
     */
    public void addPlaceholderTime(@NotNull String placeholder, long nanoseconds) {
        if (!trackUsage) return;
        placeholderUsageCurrent.computeIfAbsent(placeholder, l -> new AtomicLong()).addAndGet(nanoseconds);
    }

    /**
     * Adds placeholder time from given map.
     *
     * @param   times
     *          How long each placeholder took
     */
    public void addPlaceholderTimes(@NotNull Map<String, Long> times) {
        if (!trackUsage) return;
        for (Map.Entry<String, Long> entry : times.entrySet()) {
            placeholderUsageCurrent.computeIfAbsent(entry.getKey(), l -> new AtomicLong()).addAndGet(entry.getValue());
        }
    }

    /**
     * Runs a task in TAB's thread and measures how long it took to process.
     *
     * @param   feature
     *          Feature running the task
     * @param   type
     *          Usage type of the feature
     * @param   task
     *          Task to run
     */
    public void runMeasuredTask(@NotNull String feature, @NotNull String type, @NotNull Runnable task) {
        submit(() -> runAndMeasure(task, feature, type));
    }

    /**
     * Runs a task in TAB's thread.
     *
     * @param   task
     *          Task to run
     */
    public void runTask(@NotNull Runnable task) {
        submit(task);
    }

    /**
     * Starts a repeating task that measures how long it takes.
     *
     * @param   intervalMilliseconds
     *          How often should the task run
     * @param   feature
     *          Feature executing the task
     * @param   type
     *          Usage the of the feature
     * @param   task
     *          Task to run periodically
     */
    public void startRepeatingMeasuredTask(int intervalMilliseconds, @NotNull String feature, @NotNull String type, @NotNull Runnable task) {
        if (processingThread.isShutdown()) return;
        processingThread.scheduleAtFixedRate(() -> runAndMeasure(task, feature, type), intervalMilliseconds, intervalMilliseconds, TimeUnit.MILLISECONDS);
    }

    /**
     * Starts a repeating task.
     *
     * @param   intervalMilliseconds
     *          How often should the task run
     * @param   task
     *          Task to run periodically
     */
    public void startRepeatingTask(int intervalMilliseconds, @NotNull Runnable task) {
        if (processingThread.isShutdown()) return;
        processingThread.scheduleAtFixedRate(() -> run(task), intervalMilliseconds, intervalMilliseconds, TimeUnit.MILLISECONDS);
    }

    /**
     * Runs a task with a delay.
     *
     * @param   delayMilliseconds
     *          How long to wait until task is executed
     * @param   feature
     *          Feature executing the task
     * @param   type
     *          Usage the of the feature
     * @param   task
     *          Task to run after a delay
     */
    public void runTaskLater(int delayMilliseconds, @NotNull String feature, @NotNull String type, @NotNull Runnable task) {
        if (processingThread.isShutdown()) return;
        processingThread.schedule(() -> runAndMeasure(task, feature, type), delayMilliseconds, TimeUnit.MILLISECONDS);
    }

    /**
     * Runs a task and measures how long it took.
     *
     * @param   task
     *          Task to run
     * @param   feature
     *          Feature executing the task
     * @param   type
     *          Usage the of the feature
     */
    public void runAndMeasure(@NotNull Runnable task, @NotNull String feature, @NotNull String type) {
        if (!trackUsage) {
            run(task);
            return;
        }
        long time = System.nanoTime();
        run(task);
        addTime(feature, type, System.nanoTime() - time);
    }

    private void run(@NotNull Runnable task) {
        try {
            task.run();
        } catch (Exception | LinkageError | StackOverflowError e) {
            TAB.getInstance().getErrorManager().taskThrewError(e);
        }
    }
}