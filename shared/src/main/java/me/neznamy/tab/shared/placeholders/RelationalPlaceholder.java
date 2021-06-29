package me.neznamy.tab.shared.placeholders;

import java.util.HashMap;
import java.util.Map;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

/**
 * A relational placeholder (output different for every pair of players)
 */
public abstract class RelationalPlaceholder extends Placeholder {
	
	//last known values with key formatted as "viewer-target" to avoid extra dimension
	private Map<String, String> lastValue = new HashMap<>();

	/**
	 * Constructs new instance with given parameters
	 * @param identifier - placeholder identifier
	 * @param refresh - refresh interval
	 */
	protected RelationalPlaceholder(String identifier, int refresh) {
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
		if (!getLastValues().containsKey(mapKey) || !getLastValues().get(mapKey).equals(newValue)) {
			getLastValues().put(mapKey, newValue);
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
		if (!getLastValues().containsKey(viewer.getName() + "-" + target.getName())) update(viewer, target);
		String value = getLastValues().get(viewer.getName() + "-" + target.getName());
		String newValue = setPlaceholders(findReplacement(replacements, TAB.getInstance().getPlaceholderManager().color(value)), target);
		if (newValue.contains("%value%")) {
			newValue = newValue.replace("%value%", value);
		}
		return newValue;
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

	public Map<String, String> getLastValues() {
		return lastValue;
	}
}