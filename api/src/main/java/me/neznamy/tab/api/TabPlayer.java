package me.neznamy.tab.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * An interface representing a player
 */
@SuppressWarnings("unused") // API class
public interface TabPlayer {

    /**
     * Returns player's name
     *
     * @return  Player's name
     */
    @NotNull String getName();

    /**
     * Returns player's UUID
     *
     * @return  Player's UUID
     */
    @NotNull UUID getUniqueId();

    /**
     * Returns platform-specific entity
     *
     * @return  platform's player object
     */
    @NotNull Object getPlayer();

    /**
     * Returns true once the player is successfully loaded (onJoin method ran through all methods)
     *
     * @return  {@code true} if player is fully loaded, {@code false} otherwise
     */
    boolean isLoaded();

    /**
     * Returns player's primary permission group. If group has been changed using
     * {@link #setTemporaryGroup(String)}, returns that value. Otherwise, returns group
     * detected by standard group assign logic
     *
     * @return  player's primary permission group
     */
    @NotNull String getGroup();

    /**
     * Temporarily overrides player's group and applies all changes coming from new group.
     * This includes all properties and sorting, if used.
     * Set to {@code null} to reset back.
     *
     * @param   group
     *          New group to use
     * @see     #hasTemporaryGroup()
     */
    void setTemporaryGroup(@Nullable String group);

    /**
     * Returns {@code true} if a temporary group was applied to the player using {@link #setTemporaryGroup(String)}.
     * If no group was set, returns {@code false}
     *
     * @return  {@code true} if group is set, {@code false} if not
     * @see     #setTemporaryGroup(String)
     */
    boolean hasTemporaryGroup();
}
