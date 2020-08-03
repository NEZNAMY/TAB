package me.neznamy.tab.shared.cpu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.neznamy.tab.shared.Shared;

public class CPUManager {

	private final int bufferSizeMillis = 100;
	private final int dataMemorySize = 600;

	private ConcurrentMap<String, Long> lastSecond = new ConcurrentHashMap<String, Long>();
	private List<ConcurrentMap<String, Long>> lastMinute = Collections.synchronizedList(new ArrayList<ConcurrentMap<String, Long>>());

	private static ExecutorService exe = Executors.newCachedThreadPool();

	public CPUManager() {
		exe.submit(new Runnable() {

			@Override
			public void run() {
				try {
					while (true) {
						Thread.sleep(bufferSizeMillis);
						lastMinute.add(lastSecond);
						lastSecond = new ConcurrentHashMap<String, Long>();
						if (lastMinute.size() > dataMemorySize) lastMinute.remove(0);
					}
				} catch (InterruptedException pluginDisabled) {

				}
			}
		});
	}
	public static void cancelAllTasks() {
		exe.shutdownNow();
		exe = Executors.newCachedThreadPool();
	}
	public void runMeasuredTask(String errorDescription, CPUFeature feature, Runnable task) {
		exe.submit(new Runnable() {

			public void run() {
				try {
					long time = System.nanoTime();
					task.run();
					addTime(feature, System.nanoTime()-time);
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
	public void startRepeatingMeasuredTask(int intervalMilliseconds, String errorDescription, CPUFeature feature, Runnable task) {
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
						addTime(feature, System.nanoTime()-time);
					} catch (InterruptedException pluginDisabled) {
						break;
					} catch (Throwable t) {
						Shared.errorManager.printError("An error occurred when " + errorDescription, t);
					}
				}
			}
		});
	}
	public void runTaskLater(int delayMilliseconds, String errorDescription, CPUFeature feature, Runnable task) {
		exe.submit(new Runnable() {

			public void run() {
				try {
					Thread.sleep(delayMilliseconds);
					long time = System.nanoTime();
					task.run();
					addTime(feature, System.nanoTime()-time);
				} catch (InterruptedException pluginDisabled) {
				} catch (Throwable t) {
					Shared.errorManager.printError("An error occurred when " + errorDescription, t);
				}
			}
		});
	}
	public Map<Object, Float> getUsage(){
		Map<Object, Long> nanoMap = new HashMap<Object, Long>();
		Object key;
		synchronized (lastMinute) {
			for (ConcurrentMap<String, Long> second : lastMinute) {
				for (Entry<String, Long> nanos : second.entrySet()) {
					key = nanos.getKey();
					if (!nanoMap.containsKey(key)) nanoMap.put(key, 0L);
					nanoMap.put(key, nanoMap.get(key)+nanos.getValue());
				}
			}
		}
		Map<Object, Float> percentMap = new HashMap<Object, Float>();
		long nanotime;
		float percent;
		for (Entry<Object, Long> entry : nanoMap.entrySet()) {
			nanotime = entry.getValue(); //nano seconds total (last minute)
			nanotime /= lastMinute.size(); //average nanoseconds per buffer (0.1 second)
			percent = (float) nanotime / bufferSizeMillis / 1000000; //relative usage (0-1)
			percent *= 100; //relative into %
			percentMap.put(entry.getKey(), percent);
		}
		return sortByValue(percentMap);
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

	public void addTime(CPUFeature feature, long nanoseconds) {
		addTime0(feature.toString(), nanoseconds);
	}
	public void addTime(String placeholder, long nanoseconds) {
		addTime0(placeholder, nanoseconds);
	}
	private void addTime0(String key, long nanoseconds) {
		Long current = lastSecond.get(key);
		if (current == null) {
			lastSecond.put(key, nanoseconds);
		} else {
			lastSecond.put(key, current + nanoseconds);
		}
	}
}