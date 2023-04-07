package me.neznamy.tab.shared.platform;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.proxy.ProxyPlatform;

import java.util.UUID;

/**
 * Class for methods called by platform's event listener.
 */
public class PlatformEventListener {

    /**
     * Processes player join by forwarding it to all features.
     *
     * @param   player
     *          Player who joined
     */
    public void join(TabPlayer player) {
        if (TAB.getInstance().isPluginDisabled()) return;
        TAB.getInstance().getCPUManager().runTask(() ->
                TAB.getInstance().getFeatureManager().onJoin(player));
    }

    /**
     * Processes player quit by forwarding it to all features.
     *
     * @param   player
     *          UUID of player who left
     */
    public void quit(UUID player) {
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
    public void worldChange(UUID player, String world) {
        if (TAB.getInstance().isPluginDisabled()) return;
        TAB.getInstance().getCPUManager().runTask(() ->
                TAB.getInstance().getFeatureManager().onWorldChange(player, world));
    }

    /**
     * Processes server change by forwarding it to all features.
     *
     * @param   player
     *          Player who switched server
     * @param   server
     *          New server
     */
    public void serverChange(UUID player, String server) {
        if (TAB.getInstance().isPluginDisabled()) return;
        TAB.getInstance().getCPUManager().runTask(() ->
                TAB.getInstance().getFeatureManager().onServerChange(player, server));
    }

    /**
     * Processes plugin message.
     *
     * @param   player
     *          UUID of player who received message
     * @param   playerName
     *          Name of player who received message
     * @param   message
     *          The message
     */
    public void pluginMessage(UUID player, String playerName, byte[] message) {
        TAB.getInstance().getCPUManager().runMeasuredTask("Plugin message handling",
                TabConstants.CpuUsageCategory.PLUGIN_MESSAGE, () ->
                    ((ProxyPlatform)TAB.getInstance().getPlatform()).getPluginMessageHandler().onPluginMessage(player, playerName, message));
    }

    public boolean command(UUID player, String command) {
        if (TAB.getInstance().isPluginDisabled()) return false;
        return TAB.getInstance().getFeatureManager().onCommand(TAB.getInstance().getPlayer(player), command);
    }
}
