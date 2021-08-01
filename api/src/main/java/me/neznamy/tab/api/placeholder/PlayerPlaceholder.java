package me.neznamy.tab.api.placeholder;

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
	private Map<String, String> lastValues = new HashMap<>();
	
	//list of players with force update
	private Set<String> forceUpdate = new HashSet<>();

	/**
	 * Constructs new instance with given parameters
	 * @param identifier - placeholder's identifier
	 * @param refresh - refresh interval in milliseconds
	 */
	protected PlayerPlaceholder(String identifier, int refresh) {
		super(identifier, refresh);
	}
	
	/**
	 * Gets new value of the placeholder, saves it to map and returns true if value changed, false if not
	 * @param p - player to replace placeholder for
	 * @return true if value changed since last time, false if not
	 */
	public boolean update(TabPlayer p) {
		Object obj = get(p);
		String newValue = obj == null ? identifier : String.valueOf(setPlaceholders(obj, p));
		
		//make invalid placeholders return identifier instead of nothing
		if (identifier.equals(newValue) && !lastValues.containsKey(p.getName())) {
			lastValues.put(p.getName(), identifier);
		}
		//using String.valueOf to remove one check and fix rare NPE caused by multi thread access
		if (!"ERROR".equals(newValue) && !identifier.equals(newValue) && (!lastValues.containsKey(p.getName()) || !lastValues.get(p.getName()).equals(newValue))) {
			lastValues.put(p.getName(), newValue);
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
		if (!lastValues.containsKey(p.getName())) {
			update(p);
		}
		return lastValues.get(p.getName());
	}
	
	/**
	 * Calls the placeholder replace code and returns the output
	 * @param p - player to get placeholder value for
	 * @return value placeholder returned
	 */
	public abstract Object get(TabPlayer p);

	public Map<String, String> getLastValues() {
		return lastValues;
	}

	public Set<String> getForceUpdate() {
		return forceUpdate;
	}
}