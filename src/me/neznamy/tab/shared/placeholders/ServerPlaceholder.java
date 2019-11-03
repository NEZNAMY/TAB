package me.neznamy.tab.shared.placeholders;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

public abstract class ServerPlaceholder extends Placeholder{

	private int cooldown;
	private long lastRefresh;
	private String lastValue = "";

	public ServerPlaceholder(String identifier, int cooldown) {
		super(identifier);
		this.cooldown = cooldown;
	}
	@Override
	public String set(String s, ITabPlayer p) {
		try {
			return s.replace(identifier, getValue());
		} catch (Throwable t) {
			return Shared.error(s, "An error occured when setting placeholder \"" + identifier + "\" for " + p.getName(), t);
		}
	}
	private String getValue() {
		long startTime = System.nanoTime();
		if (System.currentTimeMillis() - lastRefresh >= cooldown) {
			lastRefresh = System.currentTimeMillis();
			lastValue = get();
		}
		Shared.placeholderCpu(identifier, System.nanoTime()-startTime);
		return lastValue;
	}
	public abstract String get();
}