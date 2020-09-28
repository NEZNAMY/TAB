package me.neznamy.tab.platforms.bukkit.permission;

import org.bukkit.Bukkit;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.permission.PermissionPlugin;

/**
 * GroupManager hook
 */
public class GroupManager implements PermissionPlugin {

	//instance of GroupManager
	private org.anjocaido.groupmanager.GroupManager plugin;
	
	//GroupManager version
	private String version;
	
	/**
	 * Constructs new instance with given version
	 * @param version GroupManager version
	 */
	public GroupManager(String version) {
		plugin = (org.anjocaido.groupmanager.GroupManager) Bukkit.getPluginManager().getPlugin("GroupManager");
		this.version = version;
	}
	
	@Override
	public String getPrimaryGroup(TabPlayer p) {
		return plugin.getWorldsHolder().getWorldPermissions(p.getWorldName()).getGroup(p.getName());
	}

	@Override
	public String[] getAllGroups(TabPlayer p) {
		return plugin.getWorldsHolder().getWorldPermissions(p.getWorldName()).getGroups(p.getName());
	}

	@Override
	public String getVersion() {
		return version;
	}
}