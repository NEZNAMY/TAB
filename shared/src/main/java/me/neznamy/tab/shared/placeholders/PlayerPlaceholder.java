package me.neznamy.tab.shared.placeholders;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

/**
 * A player placeholder (output is different for every player)
 */
public class PlayerPlaceholder extends Placeholder {

	private Function<TabPlayer, Object> function;
	
	//last known values
	private Map<String, String> lastValues = new HashMap<>();
	
	//list of players with force update
	private Set<String> forceUpdate = new HashSet<>();

	/**
	 * Constructs new instance with given parameters
	 * @param identifier - placeholder's identifier
	 * @param refresh - refresh interval in milliseconds
	 */
	public PlayerPlaceholder(String identifier, int refresh, Function<TabPlayer, Object> function) {
		super(identifier, refresh);
		this.function = function;
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
		if (!lastValues.containsKey(p.getName()) || (!"ERROR".equals(newValue) && !identifier.equals(newValue) && !lastValues.get(p.getName()).equals(newValue))) {
			lastValues.put(p.getName(), "ERROR".equals(newValue) ? identifier : newValue);
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
	public Object get(TabPlayer p) {
		try {
			return function.apply(p);
		} catch (Throwable t) {
			TAB.getInstance().getErrorManager().placeholderError("Player placeholder " + identifier + " generated an error when setting for player " + p.getName(), t);
			return "ERROR";
		}
	}

	public Map<String, String> getLastValues() {
		return lastValues;
	}

	public Set<String> getForceUpdate() {
		return forceUpdate;
	}
}