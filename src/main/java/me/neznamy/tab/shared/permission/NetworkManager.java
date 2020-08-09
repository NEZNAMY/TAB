package me.neznamy.tab.shared.permission;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

public class NetworkManager implements PermissionPlugin {

	private Object plugin;
	
	public NetworkManager(Object plugin) {
		this.plugin = plugin;
	}
	@Override
	public String getPrimaryGroup(ITabPlayer p) {
		try {
			Object user = getUser(p);
			Object group = user.getClass().getMethod("getPrimaryGroup").invoke(user);
			return getName(group);
		} catch (Exception e) {
			return Shared.errorManager.printError("null", "Failed to get permission group of " + p.getName() + " using NetworkManager", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public String[] getAllGroups(ITabPlayer p) {
		try {
			List<String> groups = new ArrayList<String>();
			Object user = getUser(p);
			Iterable<Object> i = (Iterable<Object>) user.getClass().getMethod("getGroups").invoke(user);
			for (Object group : i) {
				groups.add(getName(group));
			}
			return groups.toArray(new String[0]);
		} catch (Exception e) {
			return Shared.errorManager.printError(new String[] {"null"}, "Failed to get permission groups of " + p.getName() + " using NetworkManager", e);
		}
	}
	
	private Object getUser(ITabPlayer p) throws Exception {
		Object permissionManager = plugin.getClass().getMethod("getPermissionManager").invoke(plugin);
		return permissionManager.getClass().getMethod("getPermissionPlayer", UUID.class).invoke(permissionManager, p.getUniqueId());
	}
	
	private String getName(Object group) throws Exception {
		return (String) group.getClass().getMethod("getName").invoke(group);
	}
}