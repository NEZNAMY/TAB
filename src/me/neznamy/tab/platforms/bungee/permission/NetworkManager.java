package me.neznamy.tab.platforms.bungee.permission;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import net.md_5.bungee.api.ProxyServer;
import nl.chimpgamer.networkmanager.api.NetworkManagerPlugin;
import nl.chimpgamer.networkmanager.api.models.permissions.Group;
import nl.chimpgamer.networkmanager.api.models.permissions.PermissionPlayer;

public class NetworkManager implements PermissionPlugin {

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
		return ((NetworkManagerPlugin)ProxyServer.getInstance().getPluginManager().getPlugin("NetworkManager")).getPermissionManager().getPermissionPlayer(p.getUniqueId());
	}
}