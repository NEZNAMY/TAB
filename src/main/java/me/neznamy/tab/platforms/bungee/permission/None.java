package me.neznamy.tab.platforms.bungee.permission;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.permission.PermissionPlugin;

/**
 * Class to take groups from bungeecord config.yml when no permission plugin is found
 */
public class None implements PermissionPlugin{

	@Override
	public String getPrimaryGroup(TabPlayer p) {
		String[] groups = p.getBungeeEntity().getGroups().toArray(new String[0]);
		return groups[groups.length-1];
	}

	@Override
	public String[] getAllGroups(TabPlayer p) {
		return p.getBungeeEntity().getGroups().toArray(new String[0]);
	}
	
	@Override
	public String getName() {
		return "Unknown/None";
	}
}