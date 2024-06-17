package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.features.redis.RedisPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for features that hook into RedisSupport for displaying data
 * of players on other servers.
 */
public interface RedisFeature {

    /**
     * Called when another proxy is reloaded to request all data again.
     */
    default void onRedisLoadRequest() {}

    /**
     * Called when a player joins another proxy.
     *
     * @param   player
     *          Player who joined
     */
    default void onJoin(@NotNull RedisPlayer player) {};

    /**
     * Called when a player quits another proxy.
     *
     * @param   player
     *          Player who left
     */
    default void onQuit(@NotNull RedisPlayer player) {}

    /**
     * Called when vanish status of a redis player changes.
     *
     * @param   player
     *          Player with changed vanish status
     */
    default void onVanishStatusChange(@NotNull RedisPlayer player) {}

    /**
     * Called when a redis player switches server.
     *
     * @param   player
     *          Player who switched server
     */
    default void onServerSwitch(@NotNull RedisPlayer player) {}
}
