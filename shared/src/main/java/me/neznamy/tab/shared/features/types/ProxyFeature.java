package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for features that hook into ProxySupport for displaying data
 * of players on other servers.
 */
public interface ProxyFeature {

    /**
     * Called when another proxy is reloaded to request all data again.
     */
    default void onProxyLoadRequest() {}

    /**
     * Called when a player joins another proxy.
     *
     * @param   player
     *          Player who joined
     */
    default void onJoin(@NotNull ProxyPlayer player) {}

    /**
     * Called when a player quits another proxy.
     *
     * @param   player
     *          Player who left
     */
    default void onQuit(@NotNull ProxyPlayer player) {}

    /**
     * Called when vanish status of a proxy player changes.
     *
     * @param   player
     *          Player with changed vanish status
     */
    default void onVanishStatusChange(@NotNull ProxyPlayer player) {}

    /**
     * Called when a proxy player switches server.
     *
     * @param   player
     *          Player who switched server
     */
    default void onServerSwitch(@NotNull ProxyPlayer player) {}
}
