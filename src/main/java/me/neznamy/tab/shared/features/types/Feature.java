package me.neznamy.tab.shared.features.types;

import java.util.List;

import me.neznamy.tab.shared.cpu.TabFeature;

/**
 * The main interface for features
 */
public interface Feature {

	/**
	 * Returns name of the feature displayed in /tab cpu
	 * @return name of the feature displayed in /tab cpu
	 */
	public TabFeature getFeatureType();
	
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