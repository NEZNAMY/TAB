package me.neznamy.tab.shared.placeholders;

import java.util.Set;
import java.util.WeakHashMap;
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
	
	//last known values with first map player viewer and second target
	private final WeakHashMap<TabPlayer, WeakHashMap<TabPlayer, String>> lastValues = new WeakHashMap<>();

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
	public boolean update(TabPlayer viewer, TabPlayer target) {
		String newValue = getReplacements().findReplacement(String.valueOf(request(viewer, target)));
		if (!lastValues.computeIfAbsent(viewer, v -> new WeakHashMap<>()).containsKey(target) || !lastValues.get(viewer).get(target).equals(newValue)) {
			lastValues.get(viewer).put(target, newValue);
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
		if (!lastValues.computeIfAbsent(viewer, v -> new WeakHashMap<>()).containsKey(target)) update(viewer, target);
		return setPlaceholders(replacements.findReplacement(EnumChatFormat.color(lastValues.get(viewer).get(target))), target);
	}

	@Override
	public void updateFromNested(TabPlayer player) {
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
			updateValue(player, all, request(player, all), true);
		}
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

	@Override
	public void updateValue(TabPlayer viewer, TabPlayer target, Object value) {
		updateValue(viewer, target, value, false);
	}

	private void updateValue(TabPlayer viewer, TabPlayer target, Object value, boolean force) {
		String s = getReplacements().findReplacement(String.valueOf(value));
		if (lastValues.computeIfAbsent(viewer, v -> new WeakHashMap<>()).containsKey(target) && lastValues.get(viewer).get(target).equals(s) && !force) return;
		lastValues.get(viewer).put(target, s);
		Set<TabFeature> usage = TAB.getInstance().getPlaceholderManager().getPlaceholderUsage().get(identifier);
		if (usage == null) return;
		for (TabFeature f : usage) {
			long time = System.nanoTime();
			f.refresh(viewer, true);
			f.refresh(target, true);
			TAB.getInstance().getCPUManager().addTime(f.getFeatureName(), f.getRefreshDisplayName(), System.nanoTime()-time);
		}
		parents.stream().map(identifier -> TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier)).forEach(placeholder -> placeholder.updateFromNested(viewer));
		parents.stream().map(identifier -> TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier)).forEach(placeholder -> placeholder.updateFromNested(target));
	}
}