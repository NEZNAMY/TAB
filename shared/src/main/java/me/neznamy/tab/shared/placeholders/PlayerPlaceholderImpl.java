package me.neznamy.tab.shared.placeholders;

import java.util.*;
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
	private final WeakHashMap<TabPlayer, String> lastValues = new WeakHashMap<>();

	//list of players with force update
	private final Set<TabPlayer> forceUpdate = Collections.newSetFromMap(new WeakHashMap<>());

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
		Object output = request(p);
		if (output == null) return false; //bridge placeholders, they are updated using updateValue method
		String obj = getReplacements().findReplacement(String.valueOf(output));
		String newValue = obj == null ? identifier : setPlaceholders(obj, p);

		//make invalid placeholders return identifier instead of nothing
		if (identifier.equals(newValue) && !lastValues.containsKey(p)) {
			lastValues.put(p, identifier);
		}
		if (!lastValues.containsKey(p) || (!ERROR_VALUE.equals(newValue) && !identifier.equals(newValue) && !lastValues.get(p).equals(newValue))) {
			lastValues.put(p, ERROR_VALUE.equals(newValue) ? identifier : newValue);
			return true;
		}
		if (forceUpdate.contains(p)) {
			forceUpdate.remove(p);
			return true;
		}
		return false;
	}

	@Override
	public void updateFromNested(TabPlayer player) {
		updateValue(player, request(player), true);
	}

	@Override
	public String getLastValue(TabPlayer p) {
		if (p == null) return identifier;
		if (!lastValues.containsKey(p)) {
			lastValues.put(p, getReplacements().findReplacement(identifier));
			update(p);
		}
		return lastValues.get(p);
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

	public Map<TabPlayer, String> getLastValues() {
		return lastValues;
	}

	public Set<TabPlayer> getForceUpdate() {
		return forceUpdate;
	}

	@Override
	public void updateValue(TabPlayer player, Object value) {
		updateValue(player, value, false);
	}

	private void updateValue(TabPlayer player, Object value, boolean force) {
		String s = getReplacements().findReplacement(String.valueOf(value));
		if (lastValues.containsKey(player) && lastValues.get(player).equals(s) && !force) return;
		lastValues.put(player, s);
		if (!player.isLoaded()) return;
		Set<TabFeature> usage = TAB.getInstance().getPlaceholderManager().getPlaceholderUsage().get(identifier);
		if (usage == null) return;
		for (TabFeature f : usage) {
			long time = System.nanoTime();
			f.refresh(player, false);
			TAB.getInstance().getCPUManager().addTime(f.getFeatureName(), f.getRefreshDisplayName(), System.nanoTime()-time);
		}
		parents.stream().map(identifier -> TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier)).forEach(placeholder -> placeholder.updateFromNested(player));
	}
}