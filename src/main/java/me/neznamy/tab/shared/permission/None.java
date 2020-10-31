package me.neznamy.tab.shared.permission;

import me.neznamy.tab.api.TabPlayer;

/**
 * An instance of PermissionPlugin to be used when none is found
 */
public class None implements PermissionPlugin {

	@Override
	public String getPrimaryGroup(TabPlayer p) {
		return "<null>";
	}

	@Override
	public String[] getAllGroups(TabPlayer p) {
		return new String[] {"<null>"};
	}
	
	@Override
	public String getName() {
		return "Unknown/None";
	}

	@Override
	public String getVersion() {
		return "-";
	}
}