package me.neznamy.tab.shared;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CPUManager {

	public static final int CPU_HISTORY = 60;
	
	private ConcurrentMap<String, ConcurrentHashMap<Long, Long>> placeholders = new ConcurrentHashMap<String, ConcurrentHashMap<Long, Long>>();
	private ConcurrentMap<String, ConcurrentHashMap<Long, Long>> features = new ConcurrentHashMap<String, ConcurrentHashMap<Long, Long>>();
	private long startTime = System.currentTimeMillis();
	
	public long getHistory() {
		return Math.min(CPU_HISTORY, (System.currentTimeMillis()-startTime)/1000);
	}
	public Map<String, Long> getFeatureCPU(){
		Map<String, Long> map = new HashMap<String, Long>();
		for (Entry<String, ConcurrentHashMap<Long, Long>> entry : features.entrySet()) {
			long time = 0;
			for (Entry<Long, Long> nanos : entry.getValue().entrySet()) {
				time += nanos.getValue();
			}
			map.put(entry.getKey(), time);
		}
		return map;
	}
	public Map<String, Long> getPlaceholderCPU(){
		Map<String, Long> map = new HashMap<String, Long>();
		for (Entry<String, ConcurrentHashMap<Long, Long>> entry : placeholders.entrySet()) {
			long time = 0;
			for (Entry<Long, Long> nanos : entry.getValue().entrySet()) {
				time += nanos.getValue();
			}
			map.put(entry.getKey(), time);
		}
		return map;
	}
	public void addFeatureTime(String feature, long nanoseconds) {
		if (!features.containsKey(feature)) {
			features.put(feature, new ConcurrentHashMap<Long, Long>());
		}
		long ms = System.currentTimeMillis();
		long time = 0;
		if (features.get(feature).containsKey(ms)) {
			//some time worked already exists for this millisecond
			time = features.get(feature).get(ms);
		}
		features.get(feature).put(ms, time + nanoseconds);
		
		//removing old history
		for (Entry<Long, Long> entry : features.get(feature).entrySet()) {
			if (entry.getKey() + CPU_HISTORY*1000 < ms) features.get(feature).remove(entry.getKey());
		}
	}
	public synchronized void addPlaceholderTime(String placeholder, long nanoseconds) {
		if (!placeholders.containsKey(placeholder)) {
			placeholders.put(placeholder, new ConcurrentHashMap<Long, Long>());
		}
		long ms = System.currentTimeMillis();
		long time = 0;
		if (placeholders.get(placeholder).containsKey(ms)) {
			//some time worked already exists for this millisecond
			time = placeholders.get(placeholder).get(ms);
		}
		placeholders.get(placeholder).put(ms, time + nanoseconds);
		
		//removing old history
		for (Entry<Long, Long> entry : placeholders.get(placeholder).entrySet()) {
			if (entry.getKey() + CPU_HISTORY*1000 < ms) placeholders.get(placeholder).remove(entry.getKey());
		}
	}
}