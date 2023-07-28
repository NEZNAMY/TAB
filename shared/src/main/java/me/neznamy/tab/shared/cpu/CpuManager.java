package me.neznamy.tab.shared.cpu;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import lombok.Getter;
import me.neznamy.tab.shared.TAB;
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
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setNameFormat("TAB Processing Thread").build());

    /** Tasks submitted to main thread before plugin was fully enabled */
    private final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();

    /** Enabled flag used to queue incoming tasks if plugin is not enabled yet */
    private volatile boolean enabled = false;

    /**
     * Constructs new instance and starts repeating task that resets values in configured interval
     */
    public CpuManager() {
        startRepeatingTask((int) TimeUnit.SECONDS.toMillis(UPDATE_RATE_SECONDS), this::makeReport);
    }

    private void makeReport() {
        lastReport = new CpuReport(UPDATE_RATE_SECONDS, featureUsageCurrent, placeholderUsageCurrent);
        featureUsageCurrent = new ConcurrentHashMap<>();
        placeholderUsageCurrent = new ConcurrentHashMap<>();

        for (Entry<String, Float> entry : lastReport.getPlaceholderUsage().entrySet()) {
            if (entry.getValue() > 30) {
                TAB.getInstance().getPlatform().logWarn(new IChatBaseComponent("CPU usage of placeholder " + entry.getKey() +
                        " is " + (int)(float)entry.getValue() + "%. It will most likely cause problems. Try increasing refresh interval."));
            }
        }
        Map<String, Map<String, Float>> features = lastReport.getFeatureUsage();
        double featuresTotal = 0;
        for (Map<String, Float> map : features.values()) {
            featuresTotal += map.values().stream().mapToDouble(Float::floatValue).sum();
        }
        if (featuresTotal > 90) {
            TAB.getInstance().getPlatform().logWarn(new IChatBaseComponent("CPU usage of the plugin is "
                    + (int)featuresTotal + "%. This will cause problems. Check /tab cpu to find out why."));
        }
    }

    /**
     * Cancels all tasks and shuts down thread pools
     */
    public void cancelAllTasks() {
        scheduler.shutdownNow();
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
        if (scheduler.isShutdown()) return;
        if (!enabled) {
            taskQueue.add(task);
            return;
        }
        scheduler.submit(() -> run(task));
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
        if (scheduler.isShutdown()) return;
        scheduler.scheduleAtFixedRate(() -> runMeasuredTask(feature, type, task), intervalMilliseconds, intervalMilliseconds, TimeUnit.MILLISECONDS);
    }

    public void startRepeatingTask(int intervalMilliseconds, @NotNull Runnable task) {
        if (scheduler.isShutdown()) return;
        scheduler.scheduleAtFixedRate(() -> run(task), intervalMilliseconds, intervalMilliseconds, TimeUnit.MILLISECONDS);
    }

    public void runTaskLater(int delayMilliseconds, @NotNull String feature, @NotNull String type, @NotNull Runnable task) {
        if (scheduler.isShutdown()) return;
        scheduler.schedule(() -> runMeasuredTask(feature, type, task), delayMilliseconds, TimeUnit.MILLISECONDS);
    }

    private void run(@NotNull Runnable task) {
        try {
            task.run();
        } catch (Exception | LinkageError | StackOverflowError e) {
            TAB.getInstance().getErrorManager().printError("An error was thrown when executing task", e);
        }
    }
}