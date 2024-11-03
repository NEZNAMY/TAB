package me.neznamy.tab.shared.cpu;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A class which measures CPU usage of all tasks inserted into it and shows usage
 */
@Getter
public class CpuManager {

    private final int UPDATE_RATE_SECONDS = 10;

    /** Active time in current time period saved as nanoseconds from features */
    private volatile Map<String, Map<String, AtomicLong>> featureUsageCurrent = new ConcurrentHashMap<>();

    /** Active time in current time period saved as nanoseconds from placeholders */
    private volatile Map<String, AtomicLong> placeholderUsageCurrent = new ConcurrentHashMap<>();

    /** Last CPU report */
    @Nullable private CpuReport lastReport;

    /** Scheduler for scheduling delayed and repeating tasks */
    private final ThreadExecutor processingThread = new ThreadExecutor("TAB Processing Thread");

    /** Scheduler for placeholder refreshing task to prevent inefficient placeholders from lagging the entire plugin */
    private final ThreadExecutor placeholderThread = new ThreadExecutor("TAB Placeholder Refreshing Thread");

    /** Scheduler for refreshing permission groups */
    private final ThreadExecutor groupRefreshingThread = new ThreadExecutor("TAB Permission Group Refreshing Thread");

    /** Scheduler for checking for tablist entry values */
    private final ThreadExecutor tablistEntryCheckThread = new ThreadExecutor("TAB TabList Entry Checker Thread");

    /** Scheduler for encoding and sending plugin messages */
    @Getter
    private static final ThreadExecutor pluginMessageEncodeThread = new ThreadExecutor("TAB Plugin Message Encoding Thread");

    /** Scheduler for decoding plugin messages */
    private final ThreadExecutor pluginMessageDecodeThread = new ThreadExecutor("TAB Plugin Message Decoding Thread");

    /** Scheduler for MySQL tasks */
    private final ThreadExecutor mysqlThread = new ThreadExecutor("TAB MySQL Thread");

    /** Tasks submitted to main thread before plugin was fully enabled */
    private final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();

    /** Enabled flag used to queue incoming tasks if plugin is not enabled yet */
    private volatile boolean enabled;

    /** Boolean tracking whether CPU usage should be tracked or not */
    private boolean trackUsage;

    /**
     * Enables CPU usage tracking and returns {@code true} if it was not enabled previously.
     * If it was, does nothing and returns {@code false}.
     *
     * @return  {@code true} if this call enabled it, {@code false} if it was already enabled before
     */
    public boolean enableTracking() {
        if (trackUsage) return false;
        trackUsage = true;
        processingThread.repeatTask(new TimedCaughtTask(this, () -> {
            lastReport = new CpuReport(UPDATE_RATE_SECONDS, featureUsageCurrent, placeholderUsageCurrent);
            featureUsageCurrent = new ConcurrentHashMap<>();
            placeholderUsageCurrent = new ConcurrentHashMap<>();
        }, "CPU Tracking", "Resetting values"), ((int) TimeUnit.SECONDS.toMillis(UPDATE_RATE_SECONDS)));
        return true;
    }

    /**
     * Cancels all tasks and shuts down thread pools
     */
    public void cancelAllTasks() {
        processingThread.shutdown();
        placeholderThread.shutdown();
        groupRefreshingThread.shutdown();
        tablistEntryCheckThread.shutdown();
        pluginMessageDecodeThread.shutdown();
        mysqlThread.shutdown();
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
        if (!enabled) {
            taskQueue.add(task);
            return;
        }
        processingThread.execute(task);
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
        if (!enabled) {
            taskQueue.add(task);
            return;
        }
        processingThread.execute(new TimedCaughtTask(this, task, feature, type));
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
}