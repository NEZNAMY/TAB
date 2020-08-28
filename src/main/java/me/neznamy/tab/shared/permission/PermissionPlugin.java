package me.neznamy.tab.shared.permission;

import me.neznamy.tab.shared.ITabPlayer;

/**
 * An interface representing permission plugin hook
 */
public interface PermissionPlugin {

	public String getPrimaryGroup(ITabPlayer p) throws Throwable;
	public String[] getAllGroups(ITabPlayer p) throws Throwable;
	
	public default String getName() {
		return getClass().getSimpleName();
	}
}
