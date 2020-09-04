package me.neznamy.tab.platforms.bungee.permission;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * Class to take groups from bungeecord config.yml when no permission plugin is found
 */
public class None implements PermissionPlugin{

	@Override
	public String getPrimaryGroup(TabPlayer p) {
		String[] groups = ((ProxiedPlayer) p.getPlayer()).getGroups().toArray(new String[0]);
		return groups[groups.length-1];
	}

	@Override
	public String[] getAllGroups(TabPlayer p) {
		return ((ProxiedPlayer) p.getPlayer()).getGroups().toArray(new String[0]);
	}
	
	@Override
	public String getName() {
		return "Unknown/None";
	}
}