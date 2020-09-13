package me.neznamy.tab.shared.placeholders;

import java.util.HashMap;
import java.util.Map;

import me.neznamy.tab.shared.ITabPlayer;

/**
 * A player placeholder (output is different for every player)
 */
public abstract class PlayerPlaceholder extends Placeholder {

	public Map<String, String> lastValue = new HashMap<String, String>();

	public PlayerPlaceholder(String identifier, int cooldown) {
		super(identifier, cooldown);
	}
	public boolean update(ITabPlayer p) {
		String newValue = get(p);
		if (newValue == null) newValue = "";
		if (!newValue.equals("ERROR") && (!lastValue.containsKey(p.getName()) || !lastValue.get(p.getName()).equals(newValue))) {
			lastValue.put(p.getName(), newValue);
			return true;
		}
		return false;
	}
	public String getLastValue(ITabPlayer p) {
		if (p == null) return identifier;
		if (!lastValue.containsKey(p.getName())) {
			lastValue.put(p.getName(), ""); //preventing stack overflow on bungee when initializing
			update(p);
		}
		return lastValue.get(p.getName());
	}
	public abstract String get(ITabPlayer p);
}