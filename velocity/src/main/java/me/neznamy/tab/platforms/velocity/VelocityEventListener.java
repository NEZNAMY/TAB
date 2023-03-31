package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.command.CommandExecuteEvent.CommandResult;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;
import me.neznamy.tab.shared.proxy.ProxyPlatform;

/**
 * The core for velocity forwarding events into all enabled features
 */
public class VelocityEventListener {

    /**
     * Disconnect event listener to forward the event to all features
     *
     * @param   e
     *          disconnect event
     */
    @Subscribe
    public void onQuit(DisconnectEvent e) {
        if (TabAPI.getInstance().isPluginDisabled()) return;
        TAB.getInstance().getCPUManager().runTask(() ->
                TabAPI.getInstance().getFeatureManager().onQuit(TabAPI.getInstance().getPlayer(e.getPlayer().getUniqueId())));
    }
    
    /**
     * Listener to join / server switch to forward the event to all features
     *
     * @param   e
     *          connect event
     */
    @Subscribe
    public void onConnect(ServerPostConnectEvent e) {
        if (TabAPI.getInstance().isPluginDisabled()) return;
        Player p = e.getPlayer();
        TAB.getInstance().getCPUManager().runTask(() -> {
            if (TabAPI.getInstance().getPlayer(p.getUniqueId()) == null) {
                TabAPI.getInstance().getFeatureManager().onJoin(new VelocityTabPlayer(p));
            } else {
                TabAPI.getInstance().getFeatureManager().onServerChange(p.getUniqueId(), p.getCurrentServer().get().getServerInfo().getName());
            }
        });
    }

    /**
     * Listener to commands to forward the event to all features
     *
     * @param   e
     *          command event
     */
    @Subscribe
    public void onCommand(CommandExecuteEvent e) {
        if (TabAPI.getInstance().isPluginDisabled()) return;
        // Imagine not allowing to cancel a command while it works completely fine on BungeeCord and Bukkit and everywhere else
        BossBarManagerImpl bossbar = (BossBarManagerImpl) TabAPI.getInstance().getFeatureManager().getFeature(TabConstants.Feature.BOSS_BAR);
        if (bossbar != null && bossbar.getToggleCommand().substring(1).equals(e.getCommand())) {
            e.setResult(CommandResult.command("vtab bossbar"));
        }
        ScoreboardManagerImpl scoreboard = (ScoreboardManagerImpl) TabAPI.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SCOREBOARD);
        if (scoreboard != null && scoreboard.getToggleCommand().substring(1).equals(e.getCommand())) {
            e.setResult(CommandResult.command("vtab scoreboard"));
        }
    }

    /**
     * Listener to plugin message event to process messages coming from bridge
     *
     * @param   event
     *          plugin message event
     */
    @Subscribe
    public void onPluginMessageEvent(PluginMessageEvent event) {
        if (!event.getIdentifier().getId().equalsIgnoreCase(TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME)) return;
        if (event.getTarget() instanceof Player) {
            event.setResult(PluginMessageEvent.ForwardResult.handled());
            ((ProxyPlatform)TAB.getInstance().getPlatform()).getPluginMessageHandler().onPluginMessage(
                    ((Player) event.getTarget()).getUniqueId(), ((Player) event.getTarget()).getUsername(), event.getData());
        }
    }
}