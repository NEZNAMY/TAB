package me.neznamy.tab.shared.placeholders;

import me.neznamy.tab.shared.ITabPlayer;

/**
 * A server placeholder (output same for all players)
 */
public abstract class ServerPlaceholder extends Placeholder{

	private String lastValue;

	public ServerPlaceholder(String identifier, int cooldown) {
		super(identifier, cooldown);
	}
	public boolean update() {
		String newValue = get();
		if (newValue == null) newValue = "";
		if (!newValue.equals("ERROR") && (lastValue == null || !lastValue.equals(newValue))) {
			lastValue = newValue;
			return true;
		}
		return false;
	}
	public String getLastValue() {
		return lastValue;
	}
	@Override
	public String getLastValue(ITabPlayer p) {
		if (lastValue == null) update();
		return lastValue;
	}
	public String get(ITabPlayer p) {
		return get();
	}
	public abstract String get();
}