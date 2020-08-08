package me.neznamy.tab.shared.permission;

import me.neznamy.tab.shared.ITabPlayer;

public class None implements PermissionPlugin {

	@Override
	public String getPrimaryGroup(ITabPlayer p) {
		return "null";
	}

	@Override
	public String[] getAllGroups(ITabPlayer p) {
		return new String[] {"null"};
	}
	
	@Override
	public String getName() {
		return "Unknown/None";
	}
}