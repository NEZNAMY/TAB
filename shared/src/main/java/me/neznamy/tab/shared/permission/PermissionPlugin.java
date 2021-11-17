package me.neznamy.tab.shared.permission;

import me.neznamy.tab.api.TabPlayer;

/**
 * An interface representing permission plugin hook
 */
public interface PermissionPlugin {

	/**
	 * Returns primary permission group of player
	 * @param p - player to get group of
	 * @return player's primary permission group
	 * @throws ReflectiveOperationException 
	 */
	public String getPrimaryGroup(TabPlayer p) throws ReflectiveOperationException;
	
	/**
	 * Returns version of the permission plugin
	 * @return version of the permission plugin
	 */
	public String getVersion();
	
	/**
	 * Returns name of the permission plugin
	 * @return name of the permission plugin
	 */
	public default String getName() {
		return getClass().getSimpleName();
	}
}