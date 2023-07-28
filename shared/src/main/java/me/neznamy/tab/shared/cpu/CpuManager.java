package me.neznamy.tab.shared.cpu;

import java.util.*;
import java.util.concurrent.*;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import lombok.Getter;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.types.TabFeature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class which measures CPU usage of all tasks inserted into it and shows usage
 */
public class CpuManager {

    private final int UPDATE_RATE_SECONDS = 10;

    /** Active time in current time period saved as nanoseconds from features */
    private volatile Map<String, Map<String, Long>> featureUsageCurrent = new ConcurrentHashMap<>();

    /** Active time in current time period saved as nanoseconds from placeholders */
    private volatile Map<String, Long> placeholderUsageCurrent = new ConcurrentHashMap<>();

    /** Last CPU report */
    @Nullable @Getter private CpuReport lastReport;

    /** Scheduler for scheduling delayed and repeating tasks */
    private final ScheduledExecutorService processingThread = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setNameFormat("TAB Processing Thread").build());

    /** Scheduler for placeholder refreshing task to prevent inefficient placeholders from lagging the entire plugin */
    private final ScheduledExecutorService placeholderThread = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setNameFormat("TAB Placeholder Refreshing Thread").build());

    /** Tasks submitted to main thread before plugin was fully enabled */
    private final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();

    /** Enabled flag used to queue incoming tasks if plugin is not enabled yet */
    private volatile boolean enabled = false;

    /**
     * Constructs new instance and starts repeating task that resets values in configured interval
     */
    public CpuManager() {
        startRepeatingTask((int) TimeUnit.SECONDS.toMillis(UPDATE_RATE_SECONDS), () -> {
            lastReport = new CpuReport(UPDATE_RATE_SECONDS, featureUsageCurrent, placeholderUsageCurrent);
            featureUsageCurrent = new ConcurrentHashMap<>();
            placeholderUsageCurrent = new ConcurrentHashMap<>();
            if (lastReport.getPlaceholderUsageTotal() > 50) {
                TAB.getInstance().getPlatform().logWarn(new IChatBaseComponent("CPU usage of placeholders is " + (int) lastReport.getPlaceholderUsageTotal() +
                        "%. See /" + (TAB.getInstance().getServerVersion() == ProtocolVersion.PROXY ? TabConstants.COMMAND_PROXY : TabConstants.COMMAND_BACKEND) +
                        " cpu for more info. Try increasing refresh intervals."));
            }
        });
    }

    /**
     * Cancels all tasks and shuts down thread pools
     */
    public void cancelAllTasks() {
        processingThread.shutdownNow();
        placeholderThread.shutdownNow();
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
        // This one cannot be queued to processing thread, because we want it in different thread
        placeholderThread.scheduleAtFixedRate(() -> run(() -> {
                    long time = System.nanoTime();
                    TAB.getInstance().getPlaceholderManager().refresh();
                    addTime(TAB.getInstance().getPlaceholderManager().getFeatureName(), TabConstants.CpuUsageCategory.PLACEHOLDER_REFRESHING, System.nanoTime() - time);
                }),
                TabConstants.Placeholder.MINIMUM_REFRESH_INTERVAL,
                TabConstants.Placeholder.MINIMUM_REFRESH_INTERVAL,
                TimeUnit.MILLISECONDS
        );
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
    public void addTime(@NotNull TabFeature feature, @NotNull String type, long nanoseconds) {
        addTime(feature.getFeatureName(), type, nanoseconds);
    }

    /**
     * Adds cpu time to specified feature and usage type
     *
     * @param feature     feature to add time to
     * @param type        sub-feature to add time to
     * @param nanoseconds time to add
     */
    public void addTime(@NotNull String feature, @NotNull String type, long nanoseconds) {
        featureUsageCurrent.computeIfAbsent(feature, f -> new ConcurrentHashMap<>()).merge(type, nanoseconds, Long::sum);
    }

    /**
     * Adds used time to specified key into specified map
     *
     * @param map  map to add usage to
     * @param key  usage key
     * @param time nanoseconds to add
     */
    private void addTime(@NotNull Map<String, Long> map, @NotNull String key, long time) {
        map.merge(key, time, Long::sum);
    }

    /**
     * Adds placeholder time to specified placeholder
     *
     * @param placeholder placeholder to add time to
     * @param nanoseconds time to add
     */
    public void addPlaceholderTime(@NotNull String placeholder, long nanoseconds) {
        addTime(placeholderUsageCurrent, placeholder, nanoseconds);
    }

    public void runMeasuredTask(@NotNull String feature, @NotNull String type, @NotNull Runnable task) {
        submit(() -> {
            long time = System.nanoTime();
            task.run();
            addTime(feature, type, System.nanoTime() - time);
        });
    }

    public void runTask(@NotNull Runnable task) {
        submit(task);
    }

    public void startRepeatingMeasuredTask(int intervalMilliseconds, @NotNull String feature, @NotNull String type, @NotNull Runnable task) {
        if (processingThread.isShutdown()) return;
        processingThread.scheduleAtFixedRate(() -> runMeasuredTask(feature, type, task), intervalMilliseconds, intervalMilliseconds, TimeUnit.MILLISECONDS);
    }

    public void startRepeatingTask(int intervalMilliseconds, @NotNull Runnable task) {
        if (processingThread.isShutdown()) return;
        processingThread.scheduleAtFixedRate(() -> run(task), intervalMilliseconds, intervalMilliseconds, TimeUnit.MILLISECONDS);
    }

    public void runTaskLater(int delayMilliseconds, @NotNull String feature, @NotNull String type, @NotNull Runnable task) {
        if (processingThread.isShutdown()) return;
        processingThread.schedule(() -> runMeasuredTask(feature, type, task), delayMilliseconds, TimeUnit.MILLISECONDS);
    }

    private void run(@NotNull Runnable task) {
        try {
            task.run();
        } catch (Exception | LinkageError | StackOverflowError e) {
            TAB.getInstance().getErrorManager().printError("An error was thrown when executing task", e);
        }
    }
}