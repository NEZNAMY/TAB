package me.neznamy.tab.platforms.bukkit.permission;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import net.milkbowl.vault.permission.Permission;

/**
 * Vault permission hook
 */
public class Vault implements PermissionPlugin {

	private Permission permission;

	public Vault(Permission permission) {
		this.permission = permission;
	}

	@Override
	public String getPrimaryGroup(TabPlayer p) {
		if (getName().equals("SuperPerms")) return "null";
		return permission.getPrimaryGroup(p.getBukkitEntity());
	}

	@Override
	public String[] getAllGroups(TabPlayer p) {
		if (getName().equals("SuperPerms")) return new String[] {"null"};
		return permission.getPlayerGroups(p.getBukkitEntity());
	}

	@Override
	public String getName() {
		return permission.getName();
	}
}