package me.neznamy.tab.shared.placeholders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.shared.TAB;

/**
 * A player placeholder (output is different for every player)
 */
public class PlayerPlaceholderImpl extends TabPlaceholder implements PlayerPlaceholder {

	/** Internal constant used to detect if placeholder threw an error */
	private static final String ERROR_VALUE = "ERROR";

	private final Function<TabPlayer, Object> function;

	//last known values
	private final Map<String, String> lastValues = new HashMap<>();

	//list of players with force update
	private final List<String> forceUpdate = new ArrayList<>();

	/**
	 * Constructs new instance with given parameters
	 * @param identifier - placeholder's identifier
	 * @param refresh - refresh interval in milliseconds
	 */
	public PlayerPlaceholderImpl(String identifier, int refresh, Function<TabPlayer, Object> function) {
		super(identifier, refresh);
		if (identifier.startsWith("%rel_")) throw new IllegalArgumentException("\"rel_\" is reserved for relational placeholder identifiers");
		this.function = function;
	}

	/**
	 * Gets new value of the placeholder, saves it to map and returns true if value changed, false if not
	 * @param p - player to replace placeholder for
	 * @return true if value changed since last time, false if not
	 */
	public boolean update(TabPlayer p) {
		String obj = String.valueOf(request(p));
		String newValue = obj == null ? identifier : setPlaceholders(obj, p);

		//make invalid placeholders return identifier instead of nothing
		if (identifier.equals(newValue) && !lastValues.containsKey(p.getName())) {
			lastValues.put(p.getName(), identifier);
		}
		if (!lastValues.containsKey(p.getName()) || (!ERROR_VALUE.equals(newValue) && !identifier.equals(newValue) && !lastValues.get(p.getName()).equals(newValue))) {
			lastValues.put(p.getName(), ERROR_VALUE.equals(newValue) ? identifier : newValue);
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
	@Override
	public Object request(TabPlayer p) {
		try {
			return function.apply(p);
		} catch (Throwable t) {
			TAB.getInstance().getErrorManager().placeholderError("Player placeholder " + identifier + " generated an error when setting for player " + p.getName(), t);
			return ERROR_VALUE;
		}
	}

	public Map<String, String> getLastValues() {
		return lastValues;
	}

	public List<String> getForceUpdate() {
		return forceUpdate;
	}

	@Override
	public void updateValue(TabPlayer player, Object value) {
		if (lastValues.containsKey(player.getName()) && lastValues.get(player.getName()).equals(value)) return;
		lastValues.put(player.getName(), String.valueOf(value));
		if (!player.isLoaded()) return;
		Set<TabFeature> usage = TAB.getInstance().getPlaceholderManager().getPlaceholderUsage().get(identifier);
		if (usage == null) return;
		for (TabFeature f : usage) {
			long time = System.nanoTime();
			f.refresh(player, false);
			TAB.getInstance().getCPUManager().addTime(f.getFeatureName(), f.getRefreshDisplayName(), System.nanoTime()-time);
		}
	}
}