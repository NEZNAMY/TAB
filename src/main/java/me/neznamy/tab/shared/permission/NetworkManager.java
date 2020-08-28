package me.neznamy.tab.shared.permission;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import nl.chimpgamer.networkmanager.api.NetworkManagerPlugin;
import nl.chimpgamer.networkmanager.api.models.permissions.Group;

/**
 * NetworkManager hook
 */
public class NetworkManager implements PermissionPlugin {

	private NetworkManagerPlugin plugin;

	public NetworkManager(Object plugin) {
		this.plugin = (NetworkManagerPlugin) plugin;
	}

	@Override
	public String getPrimaryGroup(ITabPlayer p) {
		Group group = plugin.getPermissionManager().getPermissionPlayer(p.getUniqueId()).getPrimaryGroup();
		if (group == null) {
			return Shared.errorManager.printError("null", "NetworkManager returned null primary group for " + p.getName());
		}
		return group.getName();
	}

	@Override
	public String[] getAllGroups(ITabPlayer p) {
		List<String> groups = new ArrayList<String>();
		for (Group group : plugin.getPermissionManager().getPermissionPlayer(p.getUniqueId()).getGroups()) {
			groups.add(group.getName());
		}
		return groups.toArray(new String[0]);
	}
}