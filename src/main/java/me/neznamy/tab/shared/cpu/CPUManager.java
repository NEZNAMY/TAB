package me.neznamy.tab.shared.cpu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.neznamy.tab.shared.Shared;

/**
 * A class which measures CPU usage of all tasks inserted into it and shows usage
 */
public class CPUManager {

	private final int bufferSizeMillis = 100;
	private final int dataMemorySize = 600;

//	private ConcurrentMap<String, Long> currentBuffer = new ConcurrentHashMap<String, Long>();
//	private List<ConcurrentMap<String, Long>> lastMinute = Collections.synchronizedList(new ArrayList<ConcurrentMap<String, Long>>());
	
	private Map<TabFeature, Map<UsageType, Long>> featureUsageCurrent = new ConcurrentHashMap<TabFeature, Map<UsageType, Long>>();
	private Map<String, Long> placeholderUsageCurrent = new ConcurrentHashMap<String, Long>();
	private Map<String, Long> bridgePlaceholderUsageCurrent = new ConcurrentHashMap<String, Long>();
	
	private List<Map<TabFeature, Map<UsageType, Long>>> featureUsageLastMinute = Collections.synchronizedList(new ArrayList<Map<TabFeature, Map<UsageType, Long>>>());
	private List<Map<String, Long>> placeholderUsageLastMinute = Collections.synchronizedList(new ArrayList<Map<String, Long>>());
	private List<Map<String, Long>> bridgePlaceholderUsageLastMinute = Collections.synchronizedList(new ArrayList<Map<String, Long>>());

	private ExecutorService exe = Executors.newCachedThreadPool();

	public CPUManager() {
		exe.submit(new Runnable() {

			@Override
			public void run() {
				try {
					while (true) {
						Thread.sleep(bufferSizeMillis);
						
						featureUsageLastMinute.add(featureUsageCurrent);
						placeholderUsageLastMinute.add(placeholderUsageCurrent);
						bridgePlaceholderUsageLastMinute.add(bridgePlaceholderUsageCurrent);
						
						featureUsageCurrent = new ConcurrentHashMap<TabFeature, Map<UsageType, Long>>();
						placeholderUsageCurrent = new ConcurrentHashMap<String, Long>();
						bridgePlaceholderUsageCurrent = new ConcurrentHashMap<String, Long>();
						
						if (featureUsageLastMinute.size() > dataMemorySize) featureUsageLastMinute.remove(0);
						if (placeholderUsageLastMinute.size() > dataMemorySize) placeholderUsageLastMinute.remove(0);
						if (bridgePlaceholderUsageLastMinute.size() > dataMemorySize) bridgePlaceholderUsageLastMinute.remove(0);
					}
				} catch (InterruptedException pluginDisabled) {

				}
			}
		});
	}
	
	public void cancelAllTasks() {
		exe.shutdownNow();
		exe = Executors.newCachedThreadPool();
	}
	
	public void runMeasuredTask(String errorDescription, TabFeature feature, UsageType type, Runnable task) {
		exe.submit(new Runnable() {

			public void run() {
				try {
					long time = System.nanoTime();
					task.run();
					addTime(feature, type, System.nanoTime()-time);
				} catch (Throwable t) {
					Shared.errorManager.printError("An error occurred when " + errorDescription, t);
				}
			}
		});
	}
	
	public void runTask(String errorDescription, Runnable task) {
		exe.submit(new Runnable() {

			public void run() {
				try {
					task.run();
				} catch (Throwable t) {
					Shared.errorManager.printError("An error occurred when " + errorDescription, t);
				}
			}
		});
	}
	
	public void startRepeatingMeasuredTask(int intervalMilliseconds, String errorDescription, TabFeature feature, UsageType type, Runnable task) {
		if (intervalMilliseconds <= 0) return;
		exe.submit(new Runnable() {

			public void run() {
				long lastLoop = System.currentTimeMillis()-intervalMilliseconds;
				while (true) {
					try {
						long sleep = intervalMilliseconds - (System.currentTimeMillis()-lastLoop);
						if (sleep < 0) {
							sleep = 0;
						}
						Thread.sleep(sleep);
						lastLoop = System.currentTimeMillis();
						long time = System.nanoTime();
						task.run();
						addTime(feature, type, System.nanoTime()-time);
					} catch (InterruptedException pluginDisabled) {
						break;
					} catch (Throwable t) {
						Shared.errorManager.printError("An error occurred when " + errorDescription, t);
					}
				}
			}
		});
	}
	
	public void runTaskLater(int delayMilliseconds, String errorDescription, TabFeature feature, UsageType type, Runnable task) {
		exe.submit(new Runnable() {

			public void run() {
				try {
					Thread.sleep(delayMilliseconds);
					long time = System.nanoTime();
					task.run();
					addTime(feature, type, System.nanoTime()-time);
				} catch (InterruptedException pluginDisabled) {
				} catch (Throwable t) {
					Shared.errorManager.printError("An error occurred when " + errorDescription, t);
				}
			}
		});
	}
	
	public Map<String, Float> getPlaceholderUsage(){
		return getUsage(placeholderUsageLastMinute);
	}
	
	public Map<String, Float> getBridgeUsage(){
		return getUsage(bridgePlaceholderUsageLastMinute);
	}
	
	private Map<String, Float> getUsage(List<Map<String, Long>> list){
		Map<String, Long> nanoMap = new HashMap<String, Long>();
		String key;
		synchronized (list) {
			for (Map<String, Long> second : list) {
				for (Entry<String, Long> nanos : second.entrySet()) {
					key = nanos.getKey();
					if (!nanoMap.containsKey(key)) nanoMap.put(key, 0L);
					nanoMap.put(key, nanoMap.get(key)+nanos.getValue());
				}
			}
		}
		Map<String, Float> percentMap = new HashMap<String, Float>();
		for (Entry<String, Long> entry : nanoMap.entrySet()) {
			percentMap.put(entry.getKey(), nanosToPercent(entry.getValue()));
		}
		return sortByValue(percentMap);
	}
	
	public Map<TabFeature, Map<UsageType, Float>> getFeatureUsage(){
		Map<TabFeature, Map<UsageType, Long>> total = new HashMap<TabFeature, Map<UsageType, Long>>();
		synchronized (featureUsageLastMinute) {
			TabFeature key;
			for (Map<TabFeature, Map<UsageType, Long>> second : featureUsageLastMinute) {
				for (Entry<TabFeature, Map<UsageType, Long>> nanos : second.entrySet()) {
					key = nanos.getKey();
					if (!total.containsKey(key)) {
						total.put(key, new HashMap<UsageType, Long>());
					}
					Map<UsageType, Long> usage = total.get(key);
					for (Entry<UsageType, Long> entry : nanos.getValue().entrySet()) {
						if (!usage.containsKey(entry.getKey())) {
							usage.put(entry.getKey(), 0L);
						}
						usage.put(entry.getKey(), usage.get(entry.getKey()) + entry.getValue());
					}
				}
			}
		}
		Map<TabFeature, Map<UsageType, Float>> sorted = new LinkedHashMap<TabFeature, Map<UsageType, Float>>();
		for (TabFeature key : sortKeys(total)) {
			Map<UsageType, Long> local = sortByValue(total.get(key));
			Map<UsageType, Float> percent = new LinkedHashMap<UsageType, Float>();
			for (Entry<UsageType, Long> entry : local.entrySet()) {
				percent.put(entry.getKey(), nanosToPercent(entry.getValue()));
			}
			sorted.put(key, percent);
		}
		return sorted;
	}
	
	private float nanosToPercent(long nanos) {
		nanos /= featureUsageLastMinute.size(); //average nanoseconds per buffer (0.1 second)
		float percent = (float) nanos / bufferSizeMillis / 1000000; //relative usage (0-1)
		percent *= 100; //relative into %
		return percent;
	}
	
	private <K, V extends Comparable<V>> Map<K, V> sortByValue(final Map<K, V> map) {
	    Comparator<K> valueComparator =  new Comparator<K>() {
	        public int compare(K k1, K k2) {
	            int compare = map.get(k2).compareTo(map.get(k1));
	            if (compare == 0) return 1;
	            else return compare;
	        }
	    };
	    Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
	    sortedByValues.putAll(map);
	    return sortedByValues;
	}
	
	private <K, L> List<K> sortKeys(Map<K, Map<L, Long>> map){
		Map<K, Long> simplified = new LinkedHashMap<K, Long>();
		for (Entry<K, Map<L, Long>> entry : map.entrySet()) {
			simplified.put(entry.getKey(), sumValues(entry.getValue()));
		}
		simplified = sortByValue(simplified);
		List<K> sortedKeys = new ArrayList<K>();
		for (K key : simplified.keySet()) {
			sortedKeys.add(key);
		}
		return sortedKeys;
	}
	
	private <K, V> long sumValues(Map<K, Long> map) {
		long total = 0;
		for (Long value : map.values()) {
			total += value;
		}
		return total;
	}

	public void addTime(TabFeature feature, UsageType type, long nanoseconds) {
		if (!featureUsageCurrent.containsKey(feature)) {
			featureUsageCurrent.put(feature, new ConcurrentHashMap<UsageType, Long>());
		}
		Map<UsageType, Long> usage = featureUsageCurrent.get(feature);
		if (!usage.containsKey(type)) {
			usage.put(type, 0L);
		}
		Long current = usage.get(type);
		if (current == null) {
			usage.put(type, nanoseconds);
		} else {
			usage.put(type, current + nanoseconds);
		}
	}
	
	public void addPlaceholderTime(String placeholder, long nanoseconds) {
		Long current = placeholderUsageCurrent.get(placeholder);
		if (current == null) {
			placeholderUsageCurrent.put(placeholder, nanoseconds);
		} else {
			placeholderUsageCurrent.put(placeholder, current + nanoseconds);
		}
	}
	
	public void addBridgePlaceholderTime(String placeholder, long nanoseconds) {
		Long current = bridgePlaceholderUsageCurrent.get(placeholder);
		if (current == null) {
			bridgePlaceholderUsageCurrent.put(placeholder, nanoseconds);
		} else {
			bridgePlaceholderUsageCurrent.put(placeholder, current + nanoseconds);
		}
	}
}