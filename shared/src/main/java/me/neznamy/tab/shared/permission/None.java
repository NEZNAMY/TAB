package me.neznamy.tab.shared.permission;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.GroupManager;

/**
 * An instance of PermissionPlugin to be used when none is found
 */
public class None implements PermissionPlugin {

	@Override
	public String getPrimaryGroup(TabPlayer p) {
		return GroupManager.DEFAULT_GROUP;
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