package me.neznamy.tab.shared;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import me.neznamy.tab.api.feature.TabFeature;


/**
 * A class which measures CPU usage of all tasks inserted into it and shows usage
 */
public class CpuManager {
    private static final int UPDATE_RATE_SECONDS = 10;

    private static final long TIME_PERCENT
            = TimeUnit.SECONDS.toNanos(1) / UPDATE_RATE_SECONDS;
    /**
     * Data reset interval in milliseconds
     */
    private static final int BUFFER_SIZE_MILLIS =
            (int) TimeUnit.SECONDS.toMillis(UPDATE_RATE_SECONDS);

    /**
     * Active time in current time period saved as nanoseconds from features
     */
    private volatile Map<String, Map<String, Long>> featureUsageCurrent
            = new ConcurrentHashMap<>();

    /**
     * Active time in current time period saved as nanoseconds from placeholders
     */
    private volatile Map<String, Long> placeholderUsageCurrent
            = new ConcurrentHashMap<>();

    /**
     * Active time in previous time period saved as nanoseconds from features
     */
    private volatile Map<String, Map<String, Long>> featureUsagePrevious
            = new HashMap<>();

    /**
     * Active time in previous time period saved as nanoseconds from placeholders
     */
    private volatile Map<String, Long> placeholderUsagePrevious
            = new HashMap<>();

    // Scheduler for scheduling delayed and repeating tasks
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setNameFormat("TAB Processing Thread").build());

    /**
     * Constructs new instance and starts repeating task that resets values in configured interval
     */
    public CpuManager() {
        startRepeatingTask(BUFFER_SIZE_MILLIS, () -> {
            featureUsagePrevious = Collections.unmodifiableMap(featureUsageCurrent);
            placeholderUsagePrevious = Collections.unmodifiableMap(placeholderUsageCurrent);

            featureUsageCurrent = new ConcurrentHashMap<>();
            placeholderUsageCurrent = new ConcurrentHashMap<>();
        });
    }

    /**
     * Cancels all tasks and shuts down thread pools
     */
    public void cancelAllTasks() {
        scheduler.shutdownNow();
    }

    /**
     * Submits task to TAB's main thread. If plugin is not enabled yet,
     * queues the task instead and executes once it's loaded.
     *
     * @param task task to execute
     */
    private void submit(Runnable task) {
        if (scheduler.isShutdown()) return;
        scheduler.submit(() -> {
            try {
                task.run();
            } catch (Exception | LinkageError | StackOverflowError e) {
                TAB.getInstance().getErrorManager().printError("An error was thrown when executing task", e);
            }
        });
    }

    /**
     * Returns cpu usage map of placeholders from previous time period
     *
     * @return cpu usage map of placeholders
     */
    public Map<String, Float> getPlaceholderUsage() {
        return getUsage(placeholderUsagePrevious);
    }

    /**
     * Converts nano map to percent and sorts it from highest to lowest usage.
     *
     * @param map map to convert
     * @return converted and sorted map
     */
    private static Map<String, Float> getUsage(Map<String, Long> map) {
        return map
                .entrySet()
                .stream()
                .sorted(Entry.comparingByValue((o1, o2) -> Long.compare(o2, o1)))
                .collect(LinkedHashMap::new,
                        (m, e) -> m.put(e.getKey(), nanosToPercent(e.getValue())),
                        Map::putAll
                );
    }

    /**
     * Returns map of CPU usage per feature and type in the previous time period
     *
     * @return map of CPU usage per feature and type
     */
    public Map<String, Map<String, Float>> getFeatureUsage() {
        final Map<String, Map<String, Long>> map = featureUsagePrevious;

        TreeMap<Long, Map.Entry<String, Map<String, Float>>> sorted
                = new TreeMap<>((o1, o2) -> Long.compare(o2, o1));

        map.forEach((key, val) -> {
            Set<Map.Entry<String, Long>> entries = val.entrySet();
            Map<String, Float> percent = new LinkedHashMap<>(entries.size());
            long sum = entries
                    .stream()
                    .sorted(Map.Entry.comparingByValue((o1, o2) -> Long.compare(o2, o1)))
                    .peek(e -> percent.put(e.getKey(), nanosToPercent(e.getValue())))
                    .mapToLong(Map.Entry::getValue)
                    .sum();
            sorted.put(sum, // Map.entry(key, percent) inline type java9+
                    new AbstractMap.SimpleImmutableEntry<>(key, percent));
        });
        // we will also try to get rid of O(log(n)) for random reading
        int assumeCapacity = map.size();
        return sorted
                .values()
                .stream()
                .collect(() -> new LinkedHashMap<>(assumeCapacity),
                        (m, e) -> m.put(e.getKey(), e.getValue()),
                        Map::putAll
                );
    }

    /**
     * Converts nanoseconds to percent usage.
     *
     * @param nanos nanoseconds of cpu time
     * @return usage in % (0-100)
     */
    private static float nanosToPercent(long nanos) {
        return (float) nanos / TIME_PERCENT;
    }
    /**
     * Adds cpu time to specified feature and usage type
     *
     * @param feature     feature to add time to
     * @param type        sub-feature to add time to
     * @param nanoseconds time to add
     */
    public void addTime(TabFeature feature, String type, long nanoseconds) {
        addTime(feature.getFeatureName(), type, nanoseconds);
    }

    /**
     * Adds cpu time to specified feature and usage type
     *
     * @param feature     feature to add time to
     * @param type        sub-feature to add time to
     * @param nanoseconds time to add
     */
    public void addTime(String feature, String type, long nanoseconds) {
        featureUsageCurrent
                .computeIfAbsent(feature, f -> new ConcurrentHashMap<>())
                .merge(type, nanoseconds, Long::sum);
    }

    /**
     * Adds used time to specified key into specified map
     *
     * @param map  map to add usage to
     * @param key  usage key
     * @param time nanoseconds to add
     */
    private static void addTime(Map<String, Long> map, String key, long time) {
        map.merge(key, time, Long::sum);
    }

    /**
     * Adds placeholder time to specified placeholder
     *
     * @param placeholder placeholder to add time to
     * @param nanoseconds time to add
     */
    public void addPlaceholderTime(String placeholder, long nanoseconds) {
        addTime(placeholderUsageCurrent, placeholder, nanoseconds);
    }

    public void runMeasuredTask(TabFeature feature, String type, Runnable task) {
        runMeasuredTask(feature.getFeatureName(), type, task);
    }

    public void runMeasuredTask(String feature, String type, Runnable task) {
        submit(() -> {
            long time = System.nanoTime();
            task.run();
            addTime(feature, type, System.nanoTime() - time);
        });
    }

    public void runTask(Runnable task) {
        submit(task);
    }

    public void startRepeatingMeasuredTask(int intervalMilliseconds, TabFeature feature, String type, Runnable task) {
        if (scheduler.isShutdown()) return;
        scheduler.scheduleAtFixedRate(() -> runMeasuredTask(feature, type, task), 0, intervalMilliseconds, TimeUnit.MILLISECONDS);
    }

    public void startRepeatingTask(int intervalMilliseconds, Runnable task) {
        if (scheduler.isShutdown()) return;
        scheduler.scheduleAtFixedRate(() -> runTask(task), 0, intervalMilliseconds, TimeUnit.MILLISECONDS);
    }

    public void runTaskLater(int delayMilliseconds, TabFeature feature, String type, Runnable task) {
        if (scheduler.isShutdown()) return;
        scheduler.schedule(() -> runMeasuredTask(feature, type, task), delayMilliseconds, TimeUnit.MILLISECONDS);
    }
}