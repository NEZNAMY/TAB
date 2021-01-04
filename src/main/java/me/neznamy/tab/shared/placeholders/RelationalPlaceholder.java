package me.neznamy.tab.shared.placeholders;

import java.util.HashMap;
import java.util.Map;

import me.neznamy.tab.api.TabPlayer;

/**
 * A relational placeholder (output different for every pair of players)
 */
public abstract class RelationalPlaceholder extends Placeholder {
	
	//last known values
	public Map<String, String> lastValue = new HashMap<String, String>();

	/**
	 * Constructs new instance with given parameters
	 * @param identifier - placeholder identifier
	 * @param refresh - refresh interval
	 */
	public RelationalPlaceholder(String identifier, int refresh) {
		super(identifier, refresh);
		if (!identifier.startsWith("%rel_")) throw new IllegalArgumentException("Relational placeholder identifiers must start with \"rel_\"");
	}
	
	/**
	 * Updates value for given players and returns true if value changed, false if not
	 * @param viewer - text viewer
	 * @param target - target who is the text displayed on
	 * @return true if value changed, false if not
	 */
	public synchronized boolean update(TabPlayer viewer, TabPlayer target) {
		String mapKey = viewer.getName() + "-" + target.getName();
		String newValue = get(viewer, target);
		if (!lastValue.containsKey(mapKey) || !lastValue.get(mapKey).equals(newValue)) {
			lastValue.put(mapKey, newValue);
			return true;
		}
		return false;
	}
	
	/**
	 * Returns last known value for given players
	 * @param viewer - text viewer
	 * @param target - target who is the text displayed on
	 * @return last known value
	 */
	public String getLastValue(TabPlayer viewer, TabPlayer target) {
		if (!lastValue.containsKey(viewer.getName() + "-" + target.getName())) update(viewer, target);
		return lastValue.get(viewer.getName() + "-" + target.getName());
	}
	
	@Override
	public String getLastValue(TabPlayer p) {
		throw new IllegalStateException("Not supported for relational placeholders");
	}

	/**
	 * Abstract method to be overridden by specific placeholders, returns new value of the placeholder
	 * @return new value
	 */
	public abstract String get(TabPlayer viewer, TabPlayer target);
}