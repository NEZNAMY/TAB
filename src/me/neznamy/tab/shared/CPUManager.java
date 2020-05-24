package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CPUManager {

	private static final int bufferSizeMillis = 100;
	private static final int dataMemorySize = 600;
	
	private ConcurrentMap<String, Long> lastSecond = new ConcurrentHashMap<String, Long>();
	private List<ConcurrentMap<String, Long>> lastMinute = Collections.synchronizedList(new ArrayList<ConcurrentMap<String, Long>>());

	private ExecutorService exe = Executors.newCachedThreadPool();
	
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
	public void cancelAllTasks() {
		exe.shutdownNow();
		exe = Executors.newCachedThreadPool();
	}
	public void runMeasuredTask(String errorDescription, String feature, Runnable task) {
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
	public void startRepeatingMeasuredTask(int intervalMilliseconds, String errorDescription, String feature, Runnable task) {
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
	public void runTaskLater(int delayMilliseconds, String errorDescription, String feature, Runnable task) {
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
	public Map<String, Float> getUsage(){
		Map<String, Long> nanoMap = new HashMap<String, Long>();
		String key;
		synchronized (lastMinute) {
			for (ConcurrentMap<String, Long> second : lastMinute) {
				for (Entry<String, Long> nanos : second.entrySet()) {
					key = nanos.getKey();
					if (!nanoMap.containsKey(key)) nanoMap.put(key, 0L);
					nanoMap.put(key, nanoMap.get(key)+nanos.getValue());
				}
			}
		}
		Map<String, Float> percentMap = new HashMap<String, Float>();
		long nanotime;
		float percent;
		for (Entry<String, Long> entry : nanoMap.entrySet()) {
			nanotime = entry.getValue(); //nano seconds total (last minute)
			nanotime /= lastMinute.size(); //average nanoseconds per buffer (0.1 second)
			percent = (float) nanotime / bufferSizeMillis / 1000000; //relative usage (0-1)
			percent *= 100; //relative into %
			percentMap.put(entry.getKey(), percent);
		}
		return sortByValue(percentMap);
	}
	private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
		list.sort(Entry.comparingByValue());
		Map<K, V> result = new LinkedHashMap<>();
		for (int i=list.size()-1; i>=0; i--) {
			Entry<K, V> entry = list.get(i);
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	public synchronized void addTime(String key, long nanoseconds) {
		if (!lastSecond.containsKey(key)) lastSecond.put(key, 0L);
		lastSecond.put(key, lastSecond.get(key)+nanoseconds);
	}
}