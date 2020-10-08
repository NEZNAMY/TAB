package me.neznamy.tab.shared.permission;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import nl.chimpgamer.networkmanager.api.NetworkManagerPlugin;
import nl.chimpgamer.networkmanager.api.models.permissions.Group;
import nl.chimpgamer.networkmanager.api.models.permissions.PermissionPlayer;

/**
 * NetworkManager hook
 */
public class NetworkManager implements PermissionPlugin {

	private NetworkManagerPlugin plugin;
	private String version;

	public NetworkManager(NetworkManagerPlugin plugin, String version) {
		this.plugin = plugin;
		this.version = version;
	}

	@Override
	public String getPrimaryGroup(TabPlayer p) {
		PermissionPlayer permissionPlayer = plugin.getPermissionManager().getPermissionPlayer(p.getUniqueId());
		if (permissionPlayer == null) {
			return Shared.errorManager.printError("null", "NetworkManager v" + version + " returned null permission player for " + p.getName());
		}
		Group group = permissionPlayer.getPrimaryGroup();
		if (group == null) {
			return Shared.errorManager.printError("null", "NetworkManager v" + version + " returned null primary group for " + p.getName());
		}
		return group.getName();
	}

	@Override
	public String[] getAllGroups(TabPlayer p) {
		List<String> groups = new ArrayList<String>();
		PermissionPlayer permissionPlayer = plugin.getPermissionManager().getPermissionPlayer(p.getUniqueId());
		if (permissionPlayer != null) {
			for (Group group : plugin.getPermissionManager().getPermissionPlayer(p.getUniqueId()).getGroups()) {
				groups.add(group.getName());
			}
		}
		return groups.toArray(new String[0]);
	}

	@Override
	public String getVersion() {
		return version;
	}
}
