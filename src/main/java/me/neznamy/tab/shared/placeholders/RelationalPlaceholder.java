package me.neznamy.tab.shared.placeholders;

import java.util.HashMap;
import java.util.Map;

import me.neznamy.tab.shared.ITabPlayer;

/**
 * A relational placeholder (output different for every pair of players)
 */
public abstract class RelationalPlaceholder {

	public final int refresh;
	public final String identifier;
	public final Map<String, String> lastValue = new HashMap<String, String>();

	public RelationalPlaceholder(String identifier, int refresh) {
		this.identifier = identifier;
		this.refresh = refresh;
	}
	public boolean update(ITabPlayer viewer, ITabPlayer target) {
		String mapKey = viewer.getName() + "-" + target.getName();
		String newValue = get(viewer, target);
		if (!lastValue.containsKey(mapKey) || !lastValue.get(mapKey).equals(newValue)) {
			lastValue.put(mapKey, newValue);
			return true;
		}
		return false;
	}
	public String getLastValue(ITabPlayer viewer, ITabPlayer target) {
		if (!lastValue.containsKey(viewer.getName() + "-" + target.getName())) update(viewer, target);
		return lastValue.get(viewer.getName() + "-" + target.getName());
	}

	public abstract String get(ITabPlayer viewer, ITabPlayer target);
}