package me.neznamy.tab.shared.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.platform.TabPlayer;

/**
 * An abstract class representing permission plugin hook
 */
@AllArgsConstructor
public abstract class PermissionPlugin {

    /** Permission plugin's name */
    @Getter private final String name = getClass().getSimpleName();

    /**
     * Returns primary permission group of player
     *
     * @param   player
     *          player to get group of
     * @return  player's primary permission group
     */
    public abstract String getPrimaryGroup(@NonNull TabPlayer player);
}