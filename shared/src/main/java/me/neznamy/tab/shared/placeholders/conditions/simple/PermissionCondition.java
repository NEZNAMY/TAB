package me.neznamy.tab.shared.placeholders.conditions.simple;

import me.neznamy.tab.shared.player.TabPlayer;

/**
 * "permission:permission.node" condition where "permission.node" is the permission
 */
public class PermissionCondition extends SimpleCondition {

    /** Permission requirement */
    private final String permission;

    /**
     * Constructs new instance with given condition line
     *
     * @param   line
     *          configured condition line
     */
    public PermissionCondition(String line) {
        permission = line.split(":")[1];
    }

    @Override
    public boolean isMet(TabPlayer p) {
        return p.hasPermission(permission);
    }
}