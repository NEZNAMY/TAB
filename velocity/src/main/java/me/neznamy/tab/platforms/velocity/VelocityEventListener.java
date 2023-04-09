package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.command.CommandExecuteEvent.CommandResult;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;
import me.neznamy.tab.shared.platform.PlatformEventListener;

/**
 * The core for velocity forwarding events into all enabled features
 */
public class VelocityEventListener extends PlatformEventListener {

    /**
     * Disconnect event listener to forward the event to all features
     *
     * @param   e
     *          disconnect event
     */
    @Subscribe
    public void onQuit(DisconnectEvent e) {
        quit(e.getPlayer().getUniqueId());
    }
    
    /**
     * Listener to join / server switch to forward the event to all features
     *
     * @param   e
     *          connect event
     */
    @Subscribe
    @SuppressWarnings("UnstableApiUsage")
    public void onConnect(ServerPostConnectEvent e) {
        Player p = e.getPlayer();
        if (TAB.getInstance().getPlayer(p.getUniqueId()) == null) {
            join(new VelocityTabPlayer(p));
        } else {
            serverChange(p.getUniqueId(), p.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("null"));
        }
    }

    /**
     * Listener to commands to forward the event to all features
     *
     * @param   e
     *          command event
     */
    @Subscribe
    public void onCommand(CommandExecuteEvent e) {
        if (TAB.getInstance().isPluginDisabled()) return;
        // Imagine not allowing to cancel a command while it works completely fine on BungeeCord and Bukkit and everywhere else
        BossBarManagerImpl bossbar = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.BOSS_BAR);
        if (bossbar != null && bossbar.getToggleCommand().substring(1).equals(e.getCommand())) {
            e.setResult(CommandResult.command("vtab bossbar"));
        }
        ScoreboardManagerImpl scoreboard = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SCOREBOARD);
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
            pluginMessage(((Player) event.getTarget()).getUniqueId(), ((Player) event.getTarget()).getUsername(), event.getData());
        }
    }
}