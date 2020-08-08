package me.neznamy.tab.shared.permission;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.shared.ITabPlayer;
import nl.chimpgamer.networkmanager.api.NetworkManagerPlugin;
import nl.chimpgamer.networkmanager.api.models.permissions.Group;
import nl.chimpgamer.networkmanager.api.models.permissions.PermissionPlayer;

public class NetworkManager implements PermissionPlugin {

	private NetworkManagerPlugin plugin;
	
	public NetworkManager(NetworkManagerPlugin plugin) {
		this.plugin = plugin;
	}
	@Override
	public String getPrimaryGroup(ITabPlayer p) {
		return getUser(p).getPrimaryGroup().getName();
	}

	@Override
	public String[] getAllGroups(ITabPlayer p) {
		List<String> groups = new ArrayList<String>();
		for (Group group : getUser(p).getGroups()) {
			groups.add(group.getName());
		}
		return groups.toArray(new String[0]);
	}
	
	private PermissionPlayer getUser(ITabPlayer p) {
		return plugin.getPermissionManager().getPermissionPlayer(p.getUniqueId());
	}
}