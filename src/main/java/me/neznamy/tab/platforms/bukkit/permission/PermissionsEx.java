package me.neznamy.tab.platforms.bukkit.permission;

import java.lang.reflect.InvocationTargetException;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.permission.PermissionPlugin;

/**
 * PEX hook
 */
public class PermissionsEx implements PermissionPlugin {

	@Override
	public String getPrimaryGroup(ITabPlayer p) throws Throwable {
		String[] groups = getAllGroups(p);
		if (groups.length == 0) return "null";
		return groups[0];
	}

	@Override
	public String[] getAllGroups(ITabPlayer p) throws Throwable {
		try {
			Object user = Class.forName("ru.tehkode.permissions.bukkit.PermissionsEx").getMethod("getUser", String.class).invoke(null, p.getName());
			return (String[]) user.getClass().getMethod("getGroupNames").invoke(user);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
}