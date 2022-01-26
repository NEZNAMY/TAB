package me.neznamy.tab.shared;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.task.RepeatingTask;
import me.neznamy.tab.api.task.ThreadManager;

/**
 * A class which measures CPU usage of all tasks inserted into it and shows usage
 */
public class CpuManager implements ThreadManager {

	//data reset interval in milliseconds
	private static final int BUFFER_SIZE_MILLIS = 10000;

	//nanoseconds worked in the current 10 seconds
	private Map<String, Map<String, AtomicLong>> featureUsageCurrent = new ConcurrentHashMap<>();
	private Map<String, AtomicLong> placeholderUsageCurrent = new ConcurrentHashMap<>();
	private Map<String, AtomicLong> bridgePlaceholderUsageCurrent = new ConcurrentHashMap<>();
	private Map<String, AtomicLong> methodUsageCurrent = new ConcurrentHashMap<>();
	
	//packets sent in the current 10 seconds
	private Map<String, AtomicInteger> packetsCurrent = new ConcurrentHashMap<>();

	//nanoseconds worked in the previous 10 seconds
	private Map<String, Map<String, AtomicLong>> featureUsagePrevious = new HashMap<>();
	private Map<String, AtomicLong> placeholderUsagePrevious = new HashMap<>();
	private Map<String, AtomicLong> bridgePlaceholderUsagePrevious = new HashMap<>();
	private Map<String, AtomicLong> methodUsagePrevious = new HashMap<>();
	
	//packets sent in the previous 10 seconds
	private Map<String, AtomicInteger> packetsPrevious = new ConcurrentHashMap<>();

	//thread pools
	private ExecutorService thread = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("TAB Processing Thread").build());
	private final ExecutorService threadPool = Executors.newCachedThreadPool();

	private final Map<Runnable, String> taskQueue = new HashMap<>();
	private boolean enabled = false;

	//error manager
	private final ErrorManager errorManager;

	/**
	 * Constructs new instance and starts repeating task that resets values every 10 seconds
	 * @param errorManager - error manager
	 */
	public CpuManager(ErrorManager errorManager) {
		this.errorManager = errorManager;
	}
	
	public void registerPlaceholder() {
		TAB.getInstance().getPlaceholderManager().registerServerPlaceholder("%cpu%", BUFFER_SIZE_MILLIS, () -> {
			
			//dummy placeholder to trigger refresh periodically from placeholder refreshing thread to not need a new thread just for this
			featureUsagePrevious = featureUsageCurrent;
			placeholderUsagePrevious = placeholderUsageCurrent;
			bridgePlaceholderUsagePrevious = bridgePlaceholderUsageCurrent;
			methodUsagePrevious = methodUsageCurrent;
			packetsPrevious = packetsCurrent;

			featureUsageCurrent = new ConcurrentHashMap<>();
			placeholderUsageCurrent = new ConcurrentHashMap<>();
			bridgePlaceholderUsageCurrent = new ConcurrentHashMap<>();
			methodUsageCurrent = new ConcurrentHashMap<>();
			packetsCurrent = new ConcurrentHashMap<>();
			return "";
		});
		TAB.getInstance().getPlaceholderManager().addUsedPlaceholders(Collections.singletonList("%cpu%"));
	}

	/**
	 * Cancels all tasks, new instance is set to avoid errors when starting tasks on shutdown (such as packet readers)
	 */
	public void cancelAllTasks() {
		//preventing errors when tasks are inserted while shutting down
		ExecutorService old = thread;
		thread = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("TAB Processing Thread").build());
		old.shutdownNow();
		threadPool.shutdownNow();
	}

	public void enable() {
		enabled = true;
		taskQueue.forEach((r, e) -> submit(e, r));
		taskQueue.clear();
	}

	@Override
	public Future<Void> runMeasuredTask(String errorDescription, TabFeature feature, String type, Runnable task) {
		return runMeasuredTask(errorDescription, feature.getFeatureName(), type, task);
	}

	@Override
	public Future<Void> runMeasuredTask(String errorDescription, String feature, String type, Runnable task) {
		return submit(errorDescription, () -> {
			long time = System.nanoTime();
			task.run();
			addTime(feature, type, System.nanoTime()-time);
		});
	}

	@Override
	public Future<Void> runTask(String errorDescription, Runnable task) {
		return submit(errorDescription, task);
	}

	@Override
	public RepeatingTask startRepeatingMeasuredTask(int intervalMilliseconds, String errorDescription, TabFeature feature, String type, Runnable task) {
		return new TabRepeatingTask(threadPool, task, errorDescription, feature, type, intervalMilliseconds);
	}

	@Override
	public Future<?> runTaskLater(int delayMilliseconds, String errorDescription, TabFeature feature, String type, Runnable task) {
		return threadPool.submit(() -> {
			try {
				Thread.sleep(delayMilliseconds);
				runMeasuredTask(errorDescription, feature, type, task);
			} catch (InterruptedException pluginDisabled) {
				Thread.currentThread().interrupt();
			}
		});
	}

	@Override
	public Future<?> runTaskLater(int delayMilliseconds, String errorDescription, Runnable task) {
		return threadPool.submit(() -> {
			try {
				Thread.sleep(delayMilliseconds);
				submit(errorDescription, task);
			} catch (InterruptedException pluginDisabled) {
				Thread.currentThread().interrupt();
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	private Future<Void> submit(String errorDescription, Runnable task) {
		if (!enabled) {
			taskQueue.put(task, errorDescription);
			return null;
		}
		return (Future<Void>) thread.submit(() -> {
			try {
				task.run();
			} catch (Exception | LinkageError e) {
				errorManager.printError("An error occurred when " + errorDescription, e);
			}
		});
	}

	/**
	 * Returns cpu usage map of placeholders from previous 10 seconds
	 * @return cpu usage map of placeholders
	 */
	public Map<String, Float> getPlaceholderUsage(){
		return getUsage(placeholderUsagePrevious);
	}

	/**
	 * Returns cpu usage map of placeholders on bukkit side from previous 10 seconds. This is only used
	 * when TAB is on BungeeCord with PAPI placeholder support
	 * @return cpu usage map of placeholders
	 */
	public Map<String, Float> getBridgeUsage(){
		return getUsage(bridgePlaceholderUsagePrevious);
	}
	
	/**
	 * Returns cpu usage map of methods previous 10 seconds
	 * @return cpu usage map of methods
	 */
	public Map<String, Float> getMethodUsage(){
		return getUsage(methodUsagePrevious);
	}
	
	/**
	 * Returns map of sent packets per feature in previous 10 seconds
	 * @return map of sent packets per feature
	 */
	public Map<String, AtomicInteger> getSentPackets(){
		return sortByValue1(packetsPrevious);
	}

	/**
	 * Converts nano map to percent and sorts it from highest to lowest usage
	 * @param map - map to converted
	 * @return converted and sorted map
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
	 * Returns map of CPU usage per feature and type in the previous 10 seconds
	 * @return map of CPU usage per feature and type
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
	 * Converts nanoseconds to percent usage
	 * @param nanos - nanoseconds worked
	 * @return usage in % (0-100)
	 */
	private float nanosToPercent(long nanos) {
		float percent = (float) nanos / BUFFER_SIZE_MILLIS / 1000000; //relative usage (0-1)
		percent *= 100; //relative into %
		return percent;
	}

	/**
	 * Sorts map by value from highest to lowest
	 * @param <K> - map key
	 * @param <V> - map value
	 * @param map - map to sort
	 * @return sorted map
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
	 * @param <K> - map key
	 * @param map - map to sort
	 * @return sorted map
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
	 * @param <K> - map key
	 * @param map - map to sort
	 * @return list of keys sorted from the highest usage to lowest
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
	 * @param feature - feature to add time to
	 * @param type - usage to add time to of the feature
	 * @param nanoseconds - time to add
	 */
	public void addTime(TabFeature feature, String type, long nanoseconds) {
		featureUsageCurrent.computeIfAbsent(feature.getFeatureName(), f -> new ConcurrentHashMap<>()).computeIfAbsent(type, t -> new AtomicLong()).addAndGet(nanoseconds);
	}
	
	/**
	 * Adds cpu time to specified feature and usage type
	 * @param feature - feature to add time to
	 * @param type - usage to add time to of the feature
	 * @param nanoseconds - time to add
	 */
	public void addTime(String feature, String type, long nanoseconds) {
		featureUsageCurrent.computeIfAbsent(feature, f -> new ConcurrentHashMap<>()).computeIfAbsent(type, t -> new AtomicLong()).addAndGet(nanoseconds);
	}
	
	/**
	 * Adds used time to specified key into specified map
	 * @param map - map to add usage to
	 * @param key - usage key
	 * @param time - nanoseconds the task took
	 */
	private void addTime(Map<String, AtomicLong> map, String key, long time) {
		map.computeIfAbsent(key, k -> new AtomicLong()).addAndGet(time);
	}

	/**
	 * Adds placeholder time to defined placeholder
	 * @param placeholder - placeholder to add time to
	 * @param nanoseconds - time to add
	 */
	public void addPlaceholderTime(String placeholder, long nanoseconds) {
		addTime(placeholderUsageCurrent, placeholder, nanoseconds);
	}

	/**
	 * Adds placeholder time to defined placeholder
	 * @param placeholder - placeholder to add time to
	 * @param nanoseconds - time to add
	 */
	public void addBridgePlaceholderTime(String placeholder, long nanoseconds) {
		addTime(bridgePlaceholderUsageCurrent, placeholder, nanoseconds);
	}
	
	/**
	 * Adds method time to defined method
	 * @param method - method to add time to
	 * @param nanoseconds - time to add
	 */
	public void addMethodTime(String method, long nanoseconds) {
		addTime(methodUsageCurrent, method, nanoseconds);
	}
	
	/**
	 * Increments packets sent by 1 of specified feature
	 * @param feature - feature to increment packet counter of
	 */
	public void packetSent(String feature) {
		packetsCurrent.computeIfAbsent(feature, f -> new AtomicInteger()).incrementAndGet();
	}
}