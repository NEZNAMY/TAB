package me.neznamy.tab.shared.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.neznamy.tab.api.TabPlayer;

/**
 * An abstract class representing permission plugin hook
 */
@AllArgsConstructor
public abstract class PermissionPlugin {

    /** Version of the permission plugin */
    @Getter private final String version;

    /**
     * Returns primary permission group of player
     *
     * @param   player
     *          player to get group of
     * @return  player's primary permission group
     */
    public abstract String getPrimaryGroup(TabPlayer player);

    /**
     * Returns name of the permission plugin
     *
     * @return  name of the permission plugin
     */
    public String getName() {
        return getClass().getSimpleName();
    }
}