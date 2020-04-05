package me.neznamy.tab.shared.placeholders;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

public abstract class ServerPlaceholder extends Placeholder{

	private long lastRefresh;
	private String lastValue;

	public ServerPlaceholder(String identifier, int cooldown) {
		super(identifier, cooldown);
	}
	public abstract String get();
	
	@Override
	public String getValue(ITabPlayer p) {
		long startTime = System.nanoTime();
		if (System.currentTimeMillis() - lastRefresh >= cooldown) {
			String value = get();
			if (value == null || !value.equals("ERROR")) {
				lastValue = value;
			}
			lastRefresh = System.currentTimeMillis();
		}
		Shared.cpu.addPlaceholderTime(identifier, System.nanoTime()-startTime);
		return lastValue;
	}
}