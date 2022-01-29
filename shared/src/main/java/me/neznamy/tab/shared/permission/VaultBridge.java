package me.neznamy.tab.shared.permission;

import me.neznamy.tab.api.TabPlayer;

/**
 * Class to take groups from Vault on bukkit side if no permission plugin on BungeeCord is found
 */
public class VaultBridge extends PermissionPlugin {
	
	/**
	 * Constructs new instance with given parameter
	 */
	public VaultBridge() {
		super(null);
	}
	
	@Override
	public String getPrimaryGroup(TabPlayer p) {
		return p.getGroup();
	}

	@Override
	public String getName() {
		return "Vault through BukkitBridge";
	}
}