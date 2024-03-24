package me.neznamy.tab.shared.platform;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Class for methods called by platform's event listener.
 *
 * @param   <T>
 *          Platform's player class
 */
public interface EventListener<T> {

    /**
     * Processes player join by forwarding it to all features.
     *
     * @param   player
     *          Player who joined
     */
    default void join(@NotNull T player) {
        if (TAB.getInstance().isPluginDisabled()) return;
        TAB.getInstance().getCPUManager().runTask(() ->
                TAB.getInstance().getFeatureManager().onJoin(createPlayer(player)));
    }

    /**
     * Processes player quit by forwarding it to all features.
     *
     * @param   player
     *          UUID of player who left
     */
    default void quit(@NotNull UUID player) {
        if (TAB.getInstance().isPluginDisabled()) return;
        TAB.getInstance().getCPUManager().runTask(() ->
                TAB.getInstance().getFeatureManager().onQuit(TAB.getInstance().getPlayer(player)));
    }

    /**
     * Processes world change by forwarding it to all features.
     *
     * @param   player
     *          Player who switched world
     * @param   world
     *          New world
     */
    default void worldChange(@NotNull UUID player, @NotNull String world) {
        if (TAB.getInstance().isPluginDisabled()) return;
        TAB.getInstance().getCPUManager().runTask(() ->
                TAB.getInstance().getFeatureManager().onWorldChange(player, world));
    }

    /**
     * Processes plugin message.
     *
     * @param   player
     *          UUID of player who received message
     * @param   message
     *          The message
     */
    default void pluginMessage(@NotNull UUID player, byte[] message) {
        TAB.getInstance().getCPUManager().runMeasuredTask("Plugin message handling",
                TabConstants.CpuUsageCategory.PLUGIN_MESSAGE, () ->
                    ((ProxyPlatform)TAB.getInstance().getPlatform()).onPluginMessage(player, message));
    }

    /**
     * Replaces player object reference. Needed on modded servers,
     * as vanilla replaces player object on respawn, and they
     * preserve that behavior.
     *
     * @param   player
     *          UUID of affected player
     * @param   newPlayer
     *          New player object
     */
    default void replacePlayer(UUID player, T newPlayer) {
        if (TAB.getInstance().isPluginDisabled()) return;
        TabPlayer p = TAB.getInstance().getPlayer(player);
        if (p == null) return;
        p.setPlayer(newPlayer);
    }

    /**
     * Forwards command preprocess to all features. Returns {@code true}
     * if the event should be cancelled, {@code false} if not.
     *
     * @param   player
     *          Player who ran the command
     * @param   command
     *          Executed command including /
     * @return  {@code true} if event should be cancelled, {@code false} if not.
     */
    default boolean command(@NotNull UUID player, @NotNull String command) {
        if (TAB.getInstance().isPluginDisabled()) return false;
        return TAB.getInstance().getFeatureManager().onCommand(TAB.getInstance().getPlayer(player), command);
    }

    /**
     * Creates new TabPlayer instance from given player object.
     *
     * @param   player
     *          Platform's player object
     * @return  New TabPlayer from given player object
     */
    @NotNull
    TabPlayer createPlayer(@NotNull T player);
}
