package me.neznamy.tab.shared.permission;

import me.neznamy.tab.shared.ITabPlayer;

public interface PermissionPlugin {

	public String getPrimaryGroup(ITabPlayer p);
	public String[] getAllGroups(ITabPlayer p);
	
	public default String getName() {
		return getClass().getSimpleName();
	}
}
