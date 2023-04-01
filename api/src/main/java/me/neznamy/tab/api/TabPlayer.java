package me.neznamy.tab.api;

import java.util.UUID;

/**
 * An interface representing a player
 */
public interface TabPlayer {

    /**
     * Returns player's name
     *
     * @return  Player's name
     */
    String getName();

    /**
     * Returns player's UUID
     *
     * @return  Player's UUID
     */
    UUID getUniqueId();

    /**
     * Returns player's protocol version
     *
     * @return  player's protocol version
     */
    ProtocolVersion getVersion();

    /**
     * Returns platform-specific entity
     *
     * @return  an instance of bukkit/bungee player depending on platform
     */
    Object getPlayer();

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
    String getGroup();

    /**
     * Temporarily overrides player's group and applies all changes coming from new group.
     * This includes all properties and sorting, if used.
     *
     * @param   group
     *          New group to use
     * @see     #hasTemporaryGroup()
     * @see     #resetTemporaryGroup()
     */
    void setTemporaryGroup(String group);

    /**
     * Returns temporary group applied to the player using {@link #setTemporaryGroup(String)}.
     * If no group was set, returns {@code null}
     *
     * @return  Temporary group assigned to player or {@code null} if not set
     * @see     #setTemporaryGroup(String)
     * @see     #resetTemporaryGroup()
     */
    boolean hasTemporaryGroup();

    /**
     * Resets temporary group assigned using {@link #setTemporaryGroup(String)}.
     * If no temporary group is set, doesn't do anything.
     *
     * @see     #setTemporaryGroup(String)
     * @see     #hasTemporaryGroup()
     */
    void resetTemporaryGroup();
}
