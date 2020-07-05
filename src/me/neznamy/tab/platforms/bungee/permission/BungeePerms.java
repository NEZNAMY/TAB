package me.neznamy.tab.platforms.bungee.permission;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.PermissionsManager;

public class BungeePerms implements PermissionPlugin {

	@Override
	public String getPrimaryGroup(ITabPlayer p) {
		PermissionsManager pm = net.alpenblock.bungeeperms.BungeePerms.getInstance().getPermissionsManager();
		return pm.getMainGroup(pm.getUser(p.getUniqueId())).getName();
	}

	@Override
	public String[] getAllGroups(ITabPlayer p) {
		PermissionsManager pm = net.alpenblock.bungeeperms.BungeePerms.getInstance().getPermissionsManager();
		List<String> groups = new ArrayList<String>();
		for (Group group : pm.getUser(p.getUniqueId()).getGroups()) {
			groups.add(group.getName());
		}
		return groups.toArray(new String[0]);
	}
}