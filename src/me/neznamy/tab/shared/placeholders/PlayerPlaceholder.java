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
	protected String getValue(ITabPlayer p) {
		String name = (p == null ? "null" : p.getName());
		long startTime = System.nanoTime();
		if (!lastRefresh.containsKey(name) || System.currentTimeMillis() - lastRefresh.get(name) >= cooldown) {
			String value = get(p);
			lastRefresh.put(name, System.currentTimeMillis());
			if (value == null || !value.equals("ERROR")) {
				lastValue.put(name, value);
			}
		}
		Shared.placeholderCpu.addTime(this, System.nanoTime()-startTime);
		return lastValue.get(name);
	}
}