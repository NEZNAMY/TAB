package me.neznamy.tab.platforms.bukkit.permission;

import org.bukkit.entity.Player;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import net.milkbowl.vault.permission.Permission;

/**
 * Vault permission hook
 */
public class Vault implements PermissionPlugin {

	//permission plugi
	private Permission permission;
	
	//vault version
	private String vaultVersion;

	/**
	 * Constructs new instance with given parameters
	 * @param permission permission plugin implementation
	 * @param vaultVersion vault version
	 */
	public Vault(Permission permission, String vaultVersion) {
		this.permission = permission;
		this.vaultVersion = vaultVersion;
	}

	@Override
	public String getPrimaryGroup(TabPlayer p) {
		if (getName().equals("SuperPerms")) return "<null>";
		return permission.getPrimaryGroup((Player) p.getPlayer());
	}

	@Override
	public String[] getAllGroups(TabPlayer p) {
		if (getName().equals("SuperPerms")) return new String[] {"<null>"};
		return permission.getPlayerGroups((Player) p.getPlayer());
	}

	@Override
	public String getName() {
		return permission.getName();
	}

	@Override
	public String getVersion() {
		return "Vault " + vaultVersion;
	}
}