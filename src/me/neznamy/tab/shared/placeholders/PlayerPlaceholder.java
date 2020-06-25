package me.neznamy.tab.shared.placeholders;

import java.util.HashMap;
import java.util.Map;

import me.neznamy.tab.shared.ITabPlayer;

public abstract class PlayerPlaceholder extends Placeholder{

	public Map<String, String> lastValue = new HashMap<String, String>();

	public PlayerPlaceholder(String identifier, int cooldown) {
		super(identifier, cooldown);
	}
	public boolean update(ITabPlayer p) {
		String name = (p == null ? "null" : p.getName());
		String newValue = get(p);
		if (newValue == null) newValue = "";
		if (!newValue.equals("ERROR") && (!lastValue.containsKey(p.getName()) || !lastValue.get(p.getName()).equals(newValue))) {
			lastValue.put(name, newValue);
			return true;
		}
		return false;
	}
	public String getLastValue(ITabPlayer p) {
		return lastValue.get(p.getName());
	}
	public abstract String get(ITabPlayer p);
}