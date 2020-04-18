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
		if (p == null) return identifier;
		long startTime = System.nanoTime();
		if (!lastRefresh.containsKey(p.getName()) || System.currentTimeMillis() - lastRefresh.get(p.getName()) >= cooldown) {
			String value = get(p);
			lastRefresh.put(p.getName(), System.currentTimeMillis());
			if (value == null || !value.equals("ERROR")) {
				lastValue.put(p.getName(), value);
			}
		}
		Shared.cpu.addPlaceholderTime(identifier, System.nanoTime()-startTime);
		return lastValue.get(p.getName());
	}
}