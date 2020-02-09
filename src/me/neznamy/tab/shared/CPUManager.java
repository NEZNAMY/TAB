package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CPUManager {

	private ConcurrentMap<String, Long> placeholdersLastSecond = new ConcurrentHashMap<String, Long>();
	private List<ConcurrentMap<String, Long>> placeholdersLastMinute = new ArrayList<ConcurrentMap<String, Long>>();
	
	private ConcurrentMap<String, Long> featuresLastSecond = new ConcurrentHashMap<String, Long>();
	private List<ConcurrentMap<String, Long>> featuresLastMinute = new ArrayList<ConcurrentMap<String, Long>>();

	public CPUManager() {
		Shared.scheduleRepeatingTask(1000, "Calculating cpu usage", "Calculating CPU usage", new Runnable() {

			@Override
			public void run() {
				placeholdersLastMinute.add(placeholdersLastSecond);
				placeholdersLastSecond = new ConcurrentHashMap<String, Long>();
				if (placeholdersLastMinute.size() > 60) placeholdersLastMinute.remove(0);
				
				featuresLastMinute.add(featuresLastSecond);
				featuresLastSecond = new ConcurrentHashMap<String, Long>();
				if (featuresLastMinute.size() > 60) featuresLastMinute.remove(0);
			}
		});
	}
	public long getHistorySize() {
		return placeholdersLastMinute.size();
	}
	public Map<String, Long> getFeatureCPU(){
		return getCPU(featuresLastMinute);
	}
	public Map<String, Long> getPlaceholderCPU(){
		return getCPU(placeholdersLastMinute);
	}
	public void addFeatureTime(String feature, long nanoseconds) {
		addTime(featuresLastSecond, feature, nanoseconds);
	}
	public void addPlaceholderTime(String placeholder, long nanoseconds) {
		addTime(placeholdersLastSecond, placeholder, nanoseconds);
	}
	private Map<String, Long> getCPU(List<ConcurrentMap<String, Long>> source){
		Map<String, Long> map = new HashMap<String, Long>();
		for (ConcurrentMap<String, Long> second : source) {
			for (Entry<String, Long> nanos : second.entrySet()) {
				String key = nanos.getKey();
				if (!map.containsKey(key)) map.put(key, 0L);
				map.put(key, map.get(key)+nanos.getValue());
			}
		}
		return map;
	}
	private synchronized void addTime(ConcurrentMap<String, Long> map, String key, long nanoseconds) {
		if (!map.containsKey(key)) map.put(key, 0L);
		map.put(key, map.get(key)+nanoseconds);
	}
}