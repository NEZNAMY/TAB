package me.neznamy.tab.shared.permission;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import me.neznamy.tab.api.TabPlayer;

/**
 * BungeePerms hook
 */
public class BungeePerms implements PermissionPlugin {

	//bungeeperms version
	private String version;
	
	/**
	 * Constructs new instance with given parameter
	 * @param version - bungeeperms version
	 */
	public BungeePerms(String version) {
		this.version = version;
	}
	
	@Override
	public String getPrimaryGroup(TabPlayer p) throws Exception {
		Object bungeePerms = Class.forName("net.alpenblock.bungeeperms.BungeePerms").getMethod("getInstance").invoke(null);
		Object permissionsManager = bungeePerms.getClass().getMethod("getPermissionsManager").invoke(bungeePerms);
		Object user = permissionsManager.getClass().getMethod("getUser", UUID.class).invoke(permissionsManager, p.getUniqueId());
		Object group = permissionsManager.getClass().getMethod("getMainGroup", user.getClass()).invoke(permissionsManager, user);
		return (String) group.getClass().getMethod("getName").invoke(group);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String[] getAllGroups(TabPlayer p) throws Exception {
		Object bungeePerms = Class.forName("net.alpenblock.bungeeperms.BungeePerms").getMethod("getInstance").invoke(null);
		Object permissionsManager = bungeePerms.getClass().getMethod("getPermissionsManager").invoke(bungeePerms);
		Object user = permissionsManager.getClass().getMethod("getUser", UUID.class).invoke(permissionsManager, p.getUniqueId());
		Set<String> groups = new HashSet<String>();
		for (Object group : (Iterable<Object>)user.getClass().getMethod("getGroups").invoke(user)) {
			groups.add((String) group.getClass().getMethod("getName").invoke(group));
		}
		return groups.toArray(new String[0]);
	}
	
	@Override
	public String getVersion() {
		return version;
	}
}