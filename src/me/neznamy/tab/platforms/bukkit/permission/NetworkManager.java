package me.neznamy.tab.platforms.bukkit.permission;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import nl.chimpgamer.networkmanager.api.NetworkManagerPlugin;
import nl.chimpgamer.networkmanager.api.models.permissions.Group;
import nl.chimpgamer.networkmanager.api.models.permissions.PermissionPlayer;

public class NetworkManager implements PermissionPlugin {

	@Override
	public String getPrimaryGroup(ITabPlayer p) {
		PermissionPlayer user = ((NetworkManagerPlugin)Bukkit.getPluginManager().getPlugin("NetworkManager")).getPermissionManager().getPermissionPlayer(p.getUniqueId());
		return user.getPrimaryGroup().getName();
	}

	@Override
	public String[] getAllGroups(ITabPlayer p) {
		PermissionPlayer user = ((NetworkManagerPlugin)Bukkit.getPluginManager().getPlugin("NetworkManager")).getPermissionManager().getPermissionPlayer(p.getUniqueId());
		List<String> groups = new ArrayList<String>();
		for (Group group : user.getGroups()) {
			groups.add(group.getName());
		}
		return groups.toArray(new String[0]);
	}
}