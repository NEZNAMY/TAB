package me.neznamy.tab.shared.placeholders;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.neznamy.tab.api.TabPlayer;

/**
 * A player placeholder (output is different for every player)
 */
public abstract class PlayerPlaceholder extends Placeholder {

	public Map<String, String> lastValue = new HashMap<String, String>();
	public Set<String> forceUpdate = new HashSet<String>();

	public PlayerPlaceholder(String identifier, int refresh) {
		super(identifier, refresh);
	}
	public boolean update(TabPlayer p) {
		String newValue = get((TabPlayer) p);
		if (newValue == null) newValue = "";
		if (!newValue.equals("ERROR") && !newValue.equals(identifier) && (!lastValue.containsKey(p.getName()) || !lastValue.get(p.getName()).equals(newValue))) {
			lastValue.put(p.getName(), newValue);
			return true;
		}
		if (forceUpdate.contains(p.getName())) {
			forceUpdate.remove(p.getName());
			return true;
		}
		return false;
	}
	public String getLastValue(TabPlayer p) {
		if (p == null) return identifier;
		if (!lastValue.containsKey(p.getName())) {
			lastValue.put(p.getName(), ""); //preventing stack overflow on bungee when initializing
			update(p);
		}
		return lastValue.get(p.getName());
	}
	public abstract String get(TabPlayer p);
}