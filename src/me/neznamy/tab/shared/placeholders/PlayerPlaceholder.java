package me.neznamy.tab.shared.placeholders;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

public abstract class PlayerPlaceholder extends Placeholder{

	public PlayerPlaceholder(String identifier) {
		super(identifier);
	}
	@Override
	public String set(String s, ITabPlayer p) {
		try {
			return s.replace(identifier, getValue(p));
		} catch (Throwable t) {
			return Shared.error(s, "An error occured when setting placeholder \"" + identifier + "\" for " + p.getName(), t);
		}
	}
	private String getValue(ITabPlayer p) {
		long startTime = System.nanoTime();
		String value = get(p);
		Shared.placeholderCpu(identifier, System.nanoTime()-startTime);
		return value;
	}
	public abstract String get(ITabPlayer p);
}