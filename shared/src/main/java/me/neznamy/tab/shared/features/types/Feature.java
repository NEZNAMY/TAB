package me.neznamy.tab.shared.features.types;

import java.util.List;

/**
 * The main interface for features
 */
public interface Feature {

	/**
	 * Returns name of the feature displayed in /tab cpu. Can be anything which then .toString() will be called on.
	 * @return name of the feature displayed in /tab cpu
	 */
	public Object getFeatureType();
	
	/**
	 * Returns true if world belongs in disabled worlds, false if not
	 * @param disabledWorlds - disabled worlds list
	 * @param world - world to check
	 * @return true if feature should be disabled, false if not
	 */
	public default boolean isDisabledWorld(List<String> disabledWorlds, String world) {
		if (disabledWorlds == null) return false;
		boolean contains = contains(disabledWorlds, world);
		if (disabledWorlds.contains("WHITELIST")) contains = !contains;
		return contains;
	}
	
	default boolean contains(List<String> list, String element) {
		if (element == null) return false;
		for (String s : list) {
			if (s.endsWith("*")) {
				if (element.toLowerCase().startsWith(s.substring(0, s.length()-1).toLowerCase())) return true;
			} else {
				if (element.equalsIgnoreCase(s)) return true;
			}
		}
		return false;
	}
}