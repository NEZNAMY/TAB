package me.neznamy.tab.shared.permission;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.PermissionsManager;

/**
 * BungeePerms hook
 */
public class BungeePerms implements PermissionPlugin {

	private String version;
	
	public BungeePerms(String version) {
		this.version = version;
	}
	@Override
	public String getPrimaryGroup(TabPlayer p) {
		PermissionsManager pm = net.alpenblock.bungeeperms.BungeePerms.getInstance().getPermissionsManager();
		return pm.getMainGroup(pm.getUser(p.getUniqueId())).getName();
	}

	@Override
	public String[] getAllGroups(TabPlayer p) {
		PermissionsManager pm = net.alpenblock.bungeeperms.BungeePerms.getInstance().getPermissionsManager();
		List<String> groups = new ArrayList<String>();
		for (Group group : pm.getUser(p.getUniqueId()).getGroups()) {
			groups.add(group.getName());
		}
		return groups.toArray(new String[0]);
	}
	@Override
	public String getVersion() {
		return version;
	}
}