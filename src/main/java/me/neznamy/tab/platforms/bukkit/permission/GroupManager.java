package me.neznamy.tab.platforms.bukkit.permission;

import org.bukkit.Bukkit;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.permission.PermissionPlugin;

/**
 * GroupManager hook
 */
public class GroupManager implements PermissionPlugin {

	private String version;
	
	public GroupManager(String version) {
		this.version = version;
	}
	
	@Override
	public String getPrimaryGroup(TabPlayer p) {
		return ((org.anjocaido.groupmanager.GroupManager)Bukkit.getPluginManager().getPlugin("GroupManager")).getWorldsHolder().getWorldPermissions(p.getWorldName()).getGroup(p.getName());
	}

	@Override
	public String[] getAllGroups(TabPlayer p) {
		return ((org.anjocaido.groupmanager.GroupManager)Bukkit.getPluginManager().getPlugin("GroupManager")).getWorldsHolder().getWorldPermissions(p.getWorldName()).getGroups(p.getName());
	}

	@Override
	public String getVersion() {
		return version;
	}
}