package me.neznamy.tab.platforms.bukkit.permission;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.permission.PermissionPlugin;

public class PermissionsEx implements PermissionPlugin {

	@Override
	public String getPrimaryGroup(ITabPlayer p) throws Exception {
		String[] groups = getAllGroups(p);
		if (groups.length == 0) return "null";
		return groups[0];
	}

	@Override
	public String[] getAllGroups(ITabPlayer p) throws Exception {
		Object user = Class.forName("ru.tehkode.permissions.bukkit.PermissionsEx").getMethod("getUser", String.class).invoke(null, p.getName());
		return (String[]) user.getClass().getMethod("getGroupNames").invoke(user);
	}
}