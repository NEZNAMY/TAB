package me.neznamy.tab.shared.placeholders;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.placeholder.RelationalPlaceholder;
import me.neznamy.tab.shared.TAB;

/**
 * A relational placeholder (output different for every pair of players)
 */
public class RelationalPlaceholderImpl extends TabPlaceholder implements RelationalPlaceholder {
	
	private final BiFunction<TabPlayer, TabPlayer, Object> function;
	
	//last known values with key formatted as "viewer-target" to avoid extra dimension
	private final Map<String, String> lastValues = new HashMap<>();

	/**
	 * Constructs new instance with given parameters
	 * @param identifier - placeholder identifier
	 * @param refresh - refresh interval
	 */
	public RelationalPlaceholderImpl(String identifier, int refresh, BiFunction<TabPlayer, TabPlayer, Object> function) {
		super(identifier, refresh);
		if (!identifier.startsWith("%rel_")) throw new IllegalArgumentException("Relational placeholder identifiers must start with \"rel_\"");
		this.function = function;
	}
	
	/**
	 * Updates value for given players and returns true if value changed, false if not
	 * @param viewer - text viewer
	 * @param target - target who is the text displayed on
	 * @return true if value changed, false if not
	 */
	public synchronized boolean update(TabPlayer viewer, TabPlayer target) {
		String mapKey = key(viewer, target);
		String newValue = String.valueOf(request(viewer, target));
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
		if (!getLastValues().containsKey(key(viewer, target))) update(viewer, target);
		String value = getLastValues().get(key(viewer, target));
		return setPlaceholders(replacements.findReplacement(EnumChatFormat.color(value)), target);
	}
	
	@Override
	public String getLastValue(TabPlayer p) {
		return identifier;
	}

	/**
	 * Abstract method to be overridden by specific placeholders, returns new value of the placeholder
	 * @return new value
	 */
	public Object request(TabPlayer viewer, TabPlayer target) {
		try {
			return function.apply(viewer, target);
		} catch (Throwable t) {
			TAB.getInstance().getErrorManager().placeholderError("Relational placeholder " + identifier + " generated an error when setting for players " + viewer.getName() + " and " + target.getName(), t);
			return "ERROR";
		}
	}

	public Map<String, String> getLastValues() {
		return lastValues;
	}
	
	private String key(TabPlayer viewer, TabPlayer target) {
		return viewer.getName() + "-" + target.getName();
	}

	@Override
	public void updateValue(TabPlayer viewer, TabPlayer target, Object value) {
		if (lastValues.containsKey(key(viewer, target)) && lastValues.get(key(viewer, target)).equals(value)) return;
		lastValues.put(key(viewer, target), String.valueOf(value));
		Set<TabFeature> usage = TAB.getInstance().getPlaceholderManager().getPlaceholderUsage().get(identifier);
		if (usage == null) return;
		for (TabFeature f : usage) {
			long time = System.nanoTime();
			f.refresh(viewer, true);
			f.refresh(target, true);
			TAB.getInstance().getCPUManager().addTime(f.getFeatureName(), f.getRefreshDisplayName(), System.nanoTime()-time);
		}
	}
}