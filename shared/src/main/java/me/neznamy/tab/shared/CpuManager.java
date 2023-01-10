package me.neznamy.tab.shared;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.task.ThreadManager;

/**
 * A class which measures CPU usage of all tasks inserted into it and shows usage
 */
public class CpuManager implements ThreadManager {

    /** Data reset interval in milliseconds */
    private final int BUFFER_SIZE_MILLIS = 10000;

    /** Active time in current time period saved as nanoseconds from features */
    private Map<String, Map<String, AtomicLong>> featureUsageCurrent = new ConcurrentHashMap<>();

    /** Active time in current time period saved as nanoseconds from placeholders */
    private Map<String, AtomicLong> placeholderUsageCurrent = new ConcurrentHashMap<>();

    /** Active time in current time period saved as nanoseconds from chosen methods */
    private Map<String, AtomicLong> methodUsageCurrent = new ConcurrentHashMap<>();

    /** Amount of sent packets in current time period */
    private Map<String, AtomicInteger> packetsCurrent = new ConcurrentHashMap<>();

    /** Active time in previous time period saved as nanoseconds from features */
    private Map<String, Map<String, AtomicLong>> featureUsagePrevious = new HashMap<>();

    /** Active time in previous time period saved as nanoseconds from placeholders */
    private Map<String, AtomicLong> placeholderUsagePrevious = new HashMap<>();

    /** Active time in previous time period saved as nanoseconds from chosen methods */
    private Map<String, AtomicLong> methodUsagePrevious = new HashMap<>();

    /** Amount of sent packets in previous time period */
    private Map<String, AtomicInteger> packetsPrevious = new ConcurrentHashMap<>();

    /** TAB's main thread where all tasks are executed */
    private final ExecutorService thread = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("TAB Processing Thread").build());

    /** Thread pool for delayed and repeating tasks to perform sleep before submitting task to main thread */
    private final ScheduledThreadPoolExecutor threadPool = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(20,
            new ThreadFactoryBuilder().setNameFormat("TAB Repeating / Delayed Thread %d").build());

    /** Tasks submitted to main thread before plugin was fully enabled */
    private final List<Runnable> taskQueue = new ArrayList<>();

    /** Enabled flag used to queue incoming tasks if plugin is not enabled yet */
    private boolean enabled = false;

    /**
     * Constructs new instance and starts repeating task that resets values in configured interval
     */
    public CpuManager() {
        startRepeatingTask(BUFFER_SIZE_MILLIS, () -> {
            featureUsagePrevious = featureUsageCurrent;
            placeholderUsagePrevious = placeholderUsageCurrent;
            methodUsagePrevious = methodUsageCurrent;
            packetsPrevious = packetsCurrent;

            featureUsageCurrent = new ConcurrentHashMap<>();
            placeholderUsageCurrent = new ConcurrentHashMap<>();
            methodUsageCurrent = new ConcurrentHashMap<>();
            packetsCurrent = new ConcurrentHashMap<>();
        });
    }

    /**
     * Cancels all tasks and shuts down thread pools
     */
    public void cancelAllTasks() {
        thread.shutdownNow();
        threadPool.shutdownNow();
    }

    /**
     * Marks cpu manager as loaded and submits all queued tasks
     */
    public void enable() {
        enabled = true;
        new ArrayList<>(taskQueue).forEach(this::submit);
        taskQueue.clear();
    }

    /**
     * Submits task to TAB's main thread. If plugin is not enabled yet,
     * queues the task instead and executes once it's loaded.
     *
     * @param   task
     *          task to execute
     * @return  future returned by executor service
     */
    @SuppressWarnings("unchecked")
    private Future<Void> submit(Runnable task) {
        if (!enabled) {
            taskQueue.add(task);
            return null;
        }
        if (thread.isShutdown()) return null;
        try {
            return (Future<Void>) thread.submit(() -> {
                try {
                    task.run();
                } catch (Exception | LinkageError | StackOverflowError e) {
                    TAB.getInstance().getErrorManager().printError("An error was thrown when executing task", e);
                }
            });
        } catch (OutOfMemoryError e) {
            TAB.getInstance().getErrorManager().criticalError("Failed to schedule task due to " + e.getClass().getName() +
                    ": " + e.getMessage() + ". Threads created by TAB: " + (threadPool.getActiveCount()+1), null);
            return null;
        }
    }

    /**
     * Returns cpu usage map of placeholders from previous time period
     *
     * @return  cpu usage map of placeholders
     */
    public Map<String, Float> getPlaceholderUsage(){
        return getUsage(placeholderUsagePrevious);
    }

    /**
     * Returns cpu usage map of methods previous time period
     *
     * @return  cpu usage map of methods
     */
    public Map<String, Float> getMethodUsage(){
        return getUsage(methodUsagePrevious);
    }

    /**
     * Returns map of sent packets per feature in previous time period
     *
     * @return  map of sent packets per feature
     */
    public Map<String, AtomicInteger> getSentPackets(){
        return sortByValue1(packetsPrevious);
    }

    /**
     * Converts nano map to percent and sorts it from highest to lowest usage.
     *
     * @param   map
     *          map to convert
     * @return  converted and sorted map
     */
    private Map<String, Float> getUsage(Map<String, AtomicLong> map){
        Map<String, Long> nanoMap = new HashMap<>();
        String key;
        for (Entry<String, AtomicLong> nanos : map.entrySet()) {
            key = nanos.getKey();
            nanoMap.putIfAbsent(key, 0L);
            nanoMap.put(key, nanoMap.get(key)+nanos.getValue().get());
        }
        Map<String, Float> percentMap = new HashMap<>();
        for (Entry<String, Long> entry : nanoMap.entrySet()) {
            percentMap.put(entry.getKey(), nanosToPercent(entry.getValue()));
        }
        return sortByValue(percentMap);
    }

    /**
     * Returns map of CPU usage per feature and type in the previous time period
     *
     * @return  map of CPU usage per feature and type
     */
    public Map<String, Map<String, Float>> getFeatureUsage(){
        Map<String, Map<String, Long>> total = new HashMap<>();
        for (Entry<String, Map<String, AtomicLong>> nanos : featureUsagePrevious.entrySet()) {
            String key = nanos.getKey();
            total.putIfAbsent(key, new HashMap<>());
            Map<String, Long> usage = total.get(key);
            for (Entry<String, AtomicLong> entry : nanos.getValue().entrySet()) {
                usage.putIfAbsent(entry.getKey(), 0L);
                usage.put(entry.getKey(), usage.get(entry.getKey()) + entry.getValue().get());
            }
        }
        Map<String, Map<String, Float>> sorted = new LinkedHashMap<>();
        for (String key : sortKeys(total)) {
            Map<String, Long> local = sortByValue(total.get(key));
            Map<String, Float> percent = new LinkedHashMap<>();
            for (Entry<String, Long> entry : local.entrySet()) {
                percent.put(entry.getKey(), nanosToPercent(entry.getValue()));
            }
            sorted.put(key, percent);
        }
        return sorted;
    }

    /**
     * Converts nanoseconds to percent usage.
     *
     * @param   nanos
     *          nanoseconds of cpu time
     * @return  usage in % (0-100)
     */
    private float nanosToPercent(long nanos) {
        float percent = (float) nanos / BUFFER_SIZE_MILLIS / 1000000; //relative usage (0-1)
        percent *= 100; //relative into %
        return percent;
    }

    /**
     * Sorts map by value from highest to lowest
     *
     * @param   <K>
     *          map key type
     * @param   <V>
     *          map value type
     * @param   map
     *          map to sort
     * @return  sorted map
     */
    private <K, V extends Comparable<V>> Map<K, V> sortByValue(Map<K, V> map) {
        Comparator<K> valueComparator = (k1, k2) -> {
            int diff = map.get(k2).compareTo(map.get(k1));
            return diff == 0 ? 1 : diff; //otherwise, entries with duplicate values are lost
        };
        Map<K, V> sortedByValues = new TreeMap<>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }

    /**
     * Sorts map by value from highest to lowest
     *
     * @param   <K>
     *          map key type
     * @param   map
     *          map to sort
     * @return  sorted map
     */
    private <K> Map<K, AtomicInteger> sortByValue1(Map<K, AtomicInteger> map) {
        Comparator<K> valueComparator = (k1, k2) -> {
            int diff = map.get(k2).get() - map.get(k1).get();
            return diff == 0 ? 1 : diff; //otherwise, entries with duplicate values are lost
        };
        Map<K, AtomicInteger> sortedByValues = new TreeMap<>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }

    /**
     * Sorts keys by map nested values from highest to lowest and returns sorted list of keys
     * @param   <K>
     *          map key type
     * @param   map
     *          map to sort
     * @return  list of keys sorted from the highest map value to lowest
     */
    private <K> List<K> sortKeys(Map<K, Map<String, Long>> map){
        Map<K, Long> simplified = new LinkedHashMap<>();
        for (Entry<K, Map<String, Long>> entry : map.entrySet()) {
            simplified.put(entry.getKey(), entry.getValue().values().stream().mapToLong(Long::longValue).sum());
        }
        return new ArrayList<>(sortByValue(simplified).keySet());
    }

    /**
     * Adds cpu time to specified feature and usage type
     *
     * @param   feature
     *          feature to add time to
     * @param   type
     *          sub-feature to add time to
     * @param   nanoseconds
     *          time to add
     */
    public void addTime(TabFeature feature, String type, long nanoseconds) {
        addTime(feature.getFeatureName(), type, nanoseconds);
    }

    /**
     * Adds cpu time to specified feature and usage type
     *
     * @param   feature
     *          feature to add time to
     * @param   type
     *          sub-feature to add time to
     * @param   nanoseconds
     *          time to add
     */
    public void addTime(String feature, String type, long nanoseconds) {
        featureUsageCurrent.computeIfAbsent(feature, f -> new ConcurrentHashMap<>()).computeIfAbsent(type, t -> new AtomicLong()).addAndGet(nanoseconds);
    }

    /**
     * Adds used time to specified key into specified map
     *
     * @param   map
     *          map to add usage to
     * @param   key
     *          usage key
     * @param   time
     *          nanoseconds to add
     */
    private void addTime(Map<String, AtomicLong> map, String key, long time) {
        map.computeIfAbsent(key, k -> new AtomicLong()).addAndGet(time);
    }

    /**
     * Adds placeholder time to specified placeholder
     *
     * @param   placeholder
     *          placeholder to add time to
     * @param   nanoseconds
     *          time to add
     */
    public void addPlaceholderTime(String placeholder, long nanoseconds) {
        addTime(placeholderUsageCurrent, placeholder, nanoseconds);
    }

    /**
     * Adds method time to specified method
     *
     * @param   method
     *          method to add time to
     * @param   nanoseconds
     *          time to add
     */
    public void addMethodTime(String method, long nanoseconds) {
        addTime(methodUsageCurrent, method, nanoseconds);
    }

    /**
     * Increments packets sent by 1 of specified feature
     *
     * @param   feature
     *          feature to increment packet counter of
     */
    public void packetSent(String feature) {
        packetsCurrent.computeIfAbsent(feature, f -> new AtomicInteger()).incrementAndGet();
    }

    @Override
    public Future<Void> runMeasuredTask(TabFeature feature, String type, Runnable task) {
        return runMeasuredTask(feature.getFeatureName(), type, task);
    }

    @Override
    public Future<Void> runMeasuredTask(String feature, String type, Runnable task) {
        return submit(() -> {
            long time = System.nanoTime();
            task.run();
            addTime(feature, type, System.nanoTime()-time);
        });
    }

    @Override
    public Future<Void> runTask(Runnable task) {
        return submit(task);
    }

    @Override
    public Future<?> startRepeatingMeasuredTask(int intervalMilliseconds, TabFeature feature, String type, Runnable task) {
        if (threadPool.isShutdown()) return null;
        return threadPool.scheduleAtFixedRate(() -> runMeasuredTask(feature, type, task), 0, intervalMilliseconds, TimeUnit.MILLISECONDS);
    }

    @Override
    public Future<?> startRepeatingTask(int intervalMilliseconds, Runnable task) {
        if (threadPool.isShutdown()) return null;
        return threadPool.scheduleAtFixedRate(() -> runTask(task), 0, intervalMilliseconds, TimeUnit.MILLISECONDS);
    }

    @Override
    public Future<?> runTaskLater(int delayMilliseconds, TabFeature feature, String type, Runnable task) {
        if (threadPool.isShutdown()) return null;
        return threadPool.schedule(() -> runMeasuredTask(feature, type, task), delayMilliseconds, TimeUnit.MILLISECONDS);
    }

    @Override
    public Future<?> runTaskLater(int delayMilliseconds, Runnable task) {
        if (threadPool.isShutdown()) return null;
        return threadPool.schedule(() -> submit(task), delayMilliseconds, TimeUnit.MILLISECONDS);
    }
}