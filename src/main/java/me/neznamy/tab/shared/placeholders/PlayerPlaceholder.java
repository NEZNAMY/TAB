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

	//last known values
	public Map<String, String> lastValue = new HashMap<String, String>();
	
	//list of players with force update
	public Set<String> forceUpdate = new HashSet<String>();

	/**
	 * Constructs new instance with given parameters
	 * @param identifier - placeholder's identifier
	 * @param refresh - refresh interval in milliseconds
	 */
	public PlayerPlaceholder(String identifier, int refresh) {
		super(identifier, refresh);
	}
	
	/**
	 * Gets new value of the placeholder, saves it to map and returns true if value changed, false if not
	 * @param p - player to replace placeholder for
	 * @return true if value changed since last time, false if not
	 */
	public boolean update(TabPlayer p) {
		String newValue = get(p);
		if (newValue == null) newValue = "";
		
		//make invalid placeholders return identifier instead of nothing
		if (newValue.equals(identifier) && !lastValue.containsKey(p.getName())) {
			lastValue.put(p.getName(), identifier);
		}
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

	@Override
	public String getLastValue(TabPlayer p) {
		if (p == null) return identifier;
		if (!lastValue.containsKey(p.getName())) {
			update(p);
		}
		return lastValue.get(p.getName());
	}
	
	/**
	 * Calls the placeholder replace code and returns the output
	 * @param p - player to get placeholder value for
	 * @return value placeholder returned
	 */
	public abstract String get(TabPlayer p);
}