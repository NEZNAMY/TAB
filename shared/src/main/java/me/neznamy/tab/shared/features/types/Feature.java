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
		if (disabledWorlds.contains("WHITELIST")) {
			for (String enabled : disabledWorlds) {
				if (enabled != null && enabled.equalsIgnoreCase(world)) return false;
			}
			return true;
		} else {
			for (String disabled : disabledWorlds) {
				if (disabled != null && disabled.equalsIgnoreCase(world)) return true;
			}
			return false;
		}
	}
}