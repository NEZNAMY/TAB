package me.neznamy.tab.shared.placeholders;

import java.util.HashMap;
import java.util.Map;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

public abstract class PlayerPlaceholder extends Placeholder{

	public Map<String, Long> lastRefresh = new HashMap<String, Long>();
	public Map<String, String> lastValue = new HashMap<String, String>();
	
	public PlayerPlaceholder(String identifier, int cooldown) {
		super(identifier, cooldown);
	}
	public abstract String get(ITabPlayer p);
	
	@Override
	public String getValue(ITabPlayer p) {
		long startTime = System.nanoTime();
		if (!lastRefresh.containsKey(p.getName()) || System.currentTimeMillis() - lastRefresh.get(p.getName()) >= cooldown) {
			lastValue.put(p.getName(), get(p));
			lastRefresh.put(p.getName(), System.currentTimeMillis());
		}
		Shared.placeholderCpu(identifier, System.nanoTime()-startTime);
		return lastValue.get(p.getName());
	}
}