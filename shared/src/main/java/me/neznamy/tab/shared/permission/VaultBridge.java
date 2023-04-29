package me.neznamy.tab.shared.permission;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.platform.TabPlayer;

/**
 * Class to take groups from Vault on bukkit side
 * if no permission plugin on BungeeCord is found.
 */
public class VaultBridge extends PermissionPlugin {

    @Getter private final String name = "Vault through BukkitBridge";

    @Override
    public String getPrimaryGroup(@NonNull TabPlayer p) {
        return p.getGroup();
    }
}