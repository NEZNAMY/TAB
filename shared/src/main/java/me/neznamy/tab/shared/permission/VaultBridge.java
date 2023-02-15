package me.neznamy.tab.shared.permission;

import lombok.Getter;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.TabConstants;

/**
 * Class to take groups from Vault on bukkit side
 * if no permission plugin on BungeeCord is found.
 */
public class VaultBridge extends PermissionPlugin {

    @Getter private final String name = "Vault through BukkitBridge";
    /**
     * Constructs new instance
     */
    public VaultBridge() {
        super(null);
    }

    @Override
    public String getPrimaryGroup(TabPlayer p) {
        return p.getGroup() == null ? TabConstants.NO_GROUP : p.getGroup();
    }
}