package me.neznamy.tab.shared.placeholders;

import java.util.HashMap;
import java.util.Map;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.ITabPlayer;

/**
 * A relational placeholder (output different for every pair of players)
 */
public abstract class RelationalPlaceholder {

	private int refresh;
	public String identifier;
	public Map<String, String> lastValue = new HashMap<String, String>();

	public RelationalPlaceholder(String identifier, int refresh) {
		this.identifier = identifier;
		this.refresh = refresh;
	}
	
	public int getRefresh() {
		return refresh;
	}
	
	public boolean update(TabPlayer viewer, TabPlayer target) {
		String mapKey = viewer.getName() + "-" + target.getName();
		String newValue = get((ITabPlayer) viewer, (ITabPlayer) target);
		if (!lastValue.containsKey(mapKey) || !lastValue.get(mapKey).equals(newValue)) {
			lastValue.put(mapKey, newValue);
			return true;
		}
		return false;
	}
	
	public String getLastValue(TabPlayer viewer, TabPlayer target) {
		if (!lastValue.containsKey(viewer.getName() + "-" + target.getName())) update(viewer, target);
		return lastValue.get(viewer.getName() + "-" + target.getName());
	}

	public abstract String get(ITabPlayer viewer, ITabPlayer target);
}