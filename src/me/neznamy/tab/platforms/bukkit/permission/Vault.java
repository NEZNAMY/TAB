package me.neznamy.tab.platforms.bukkit.permission;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import net.milkbowl.vault.permission.Permission;

public class Vault implements PermissionPlugin {

	private Permission permission;

	public Vault(Permission permission) {
		this.permission = permission;
	}

	@Override
	public String getPrimaryGroup(ITabPlayer p) {
		if (getName().equals("SuperPerms")) return "null";
		return permission.getPrimaryGroup(p.getBukkitEntity());
	}

	@Override
	public String[] getAllGroups(ITabPlayer p) {
		if (getName().equals("SuperPerms")) return new String[] {"null"};
		return permission.getPlayerGroups(p.getBukkitEntity());
	}

	@Override
	public String getName() {
		try {
			return permission.getName();
		} catch (Throwable e) {
			return Shared.errorManager.printError("Unknown/None", "Failed to get permission plugin name using Vault", e);
		}
	}
}