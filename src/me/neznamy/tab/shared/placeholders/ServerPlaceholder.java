package me.neznamy.tab.shared.placeholders;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

public abstract class ServerPlaceholder extends Placeholder{

	private long lastRefresh;
	private String lastValue = "";

	public ServerPlaceholder(String identifier, int cooldown) {
		super(identifier, cooldown, identifier);
	}
	public ServerPlaceholder(String identifier, int cooldown, String cpuDisplay) {
		super(identifier, cooldown, cpuDisplay);
	}
	public abstract String get();
	
	@Override
	public String getValue(ITabPlayer p) {
		long startTime = System.nanoTime();
		if (System.currentTimeMillis() - lastRefresh >= cooldown) {
			lastRefresh = System.currentTimeMillis();
			lastValue = get();
		}
		Shared.placeholderCpu(cpuDisplay, System.nanoTime()-startTime);
		return lastValue;
	}
}