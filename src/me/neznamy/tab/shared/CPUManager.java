package me.neznamy.tab.shared;

import java.util.ArrayList;
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

	private ConcurrentMap<String, Long> placeholdersLastSecond = new ConcurrentHashMap<String, Long>();
	private List<ConcurrentMap<String, Long>> placeholdersLastMinute = new ArrayList<ConcurrentMap<String, Long>>();
	
	private ConcurrentMap<String, Long> featuresLastSecond = new ConcurrentHashMap<String, Long>();
	private List<ConcurrentMap<String, Long>> featuresLastMinute = new ArrayList<ConcurrentMap<String, Long>>();

	private ExecutorService exe = Executors.newCachedThreadPool();
	
	public CPUManager() {
		exe.submit(new Runnable() {

			@Override
			public void run() {
				try {
					while (true) {
						Thread.sleep(1000);
						placeholdersLastMinute.add(placeholdersLastSecond);
						placeholdersLastSecond = new ConcurrentHashMap<String, Long>();
						if (placeholdersLastMinute.size() > 60) placeholdersLastMinute.remove(0);
						
						featuresLastMinute.add(featuresLastSecond);
						featuresLastSecond = new ConcurrentHashMap<String, Long>();
						if (featuresLastMinute.size() > 60) featuresLastMinute.remove(0);
					}
				} catch (Exception e) {
				}
			}
		});
	}
	public void cancelAllTasks() {
		exe.shutdownNow();
	}
	public void runMeasuredTask(String errorDescription, String feature, Runnable task) {
		exe.submit(new Runnable() {

			public void run() {
				try {
					long time = System.nanoTime();
					task.run();
					addFeatureTime(feature, System.nanoTime()-time);
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
						addFeatureTime(feature, System.nanoTime()-time);
					} catch (InterruptedException pluginDisabled) {
						break;
					} catch (Throwable t) {
						Shared.errorManager.printError("An error occurred when " + errorDescription, t);
					}
				}
			}
		});
	}
	public Map<String, Float> getFeatureCPU(){
		return getCPU(featuresLastMinute);
	}
	public Map<String, Float> getPlaceholderCPU(){
		return getCPU(placeholdersLastMinute);
	}
	public void addFeatureTime(String feature, long nanoseconds) {
		addTime(featuresLastSecond, feature, nanoseconds);
	}
	public void addPlaceholderTime(String placeholder, long nanoseconds) {
		addTime(placeholdersLastSecond, placeholder, nanoseconds);
	}
	private Map<String, Float> getCPU(List<ConcurrentMap<String, Long>> source){
		Map<String, Long> nanoMap = new HashMap<String, Long>();
		String key;
		for (ConcurrentMap<String, Long> second : source) {
			for (Entry<String, Long> nanos : second.entrySet()) {
				key = nanos.getKey();
				if (!nanoMap.containsKey(key)) nanoMap.put(key, 0L);
				nanoMap.put(key, nanoMap.get(key)+nanos.getValue());
			}
		}
		Map<String, Float> percentMap = new HashMap<String, Float>();
		long nanotime;
		float percent;
		for (Entry<String, Long> entry : nanoMap.entrySet()) {
			nanotime = entry.getValue(); //nano seconds last minute
			nanotime /= featuresLastMinute.size(); //average nanoseconds per second
			percent = (float) nanotime / 10000000;
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
	private synchronized void addTime(ConcurrentMap<String, Long> map, String key, long nanoseconds) {
		if (!map.containsKey(key)) map.put(key, 0L);
		map.put(key, map.get(key)+nanoseconds);
	}
}