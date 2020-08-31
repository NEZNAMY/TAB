package me.neznamy.tab.shared.permission;

import me.neznamy.tab.api.TabPlayer;

/**
 * An interface representing permission plugin hook
 */
public interface PermissionPlugin {

	public String getPrimaryGroup(TabPlayer p) throws Throwable;
	public String[] getAllGroups(TabPlayer p) throws Throwable;
	
	public default String getName() {
		return getClass().getSimpleName();
	}
}
