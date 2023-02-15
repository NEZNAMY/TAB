package me.neznamy.tab.platforms.bukkit.permission;

import lombok.Getter;
import me.neznamy.tab.api.TabConstants;
import org.bukkit.entity.Player;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import net.milkbowl.vault.permission.Permission;

/**
 * Vault permission hook
 */
public class Vault extends PermissionPlugin {

    /** Permission plugin */
    private final Permission permission;

    @Getter private final String version = "Vault " + super.getVersion();

    /**
     * Constructs new instance with given parameters
     *
     * @param   permission
     *          permission plugin implementation
     * @param   vaultVersion
     *          vault version
     */
    public Vault(Permission permission, String vaultVersion) {
        super(vaultVersion);
        this.permission = permission;
    }

    @Override
    public String getPrimaryGroup(TabPlayer p) {
        if (getName().equals("SuperPerms")) return TabConstants.NO_GROUP; //Vault's dummy implementation throws exception on every request
        return permission.getPrimaryGroup((Player) p.getPlayer());
    }

    @Override
    public String getName() {
        return permission.getName();
    }
}