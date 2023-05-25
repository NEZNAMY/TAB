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
import me.neznamy.tab.shared.platform.EventListener;
import me.neznamy.tab.shared.platform.TabPlayer;

/**
 * The core for velocity forwarding events into all enabled features
 */
public class VelocityEventListener extends EventListener<Player> {

    @Subscribe
    public void onQuit(DisconnectEvent e) {
        quit(e.getPlayer().getUniqueId());
    }

    @Subscribe
    @SuppressWarnings("UnstableApiUsage")
    public void onConnect(ServerPostConnectEvent e) {
        serverChange(e.getPlayer(), e.getPlayer().getUniqueId(), e.getPlayer().getCurrentServer()
                .map(s -> s.getServerInfo().getName()).orElse("null"));
    }

    @Subscribe
    public void onCommand(CommandExecuteEvent e) {
        if (TAB.getInstance().isPluginDisabled()) return;
        // Imagine not allowing to cancel a command while it works completely fine on BungeeCord and Bukkit and everywhere else
        BossBarManagerImpl bossBarManager = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.BOSS_BAR);
        if (bossBarManager != null && bossBarManager.getToggleCommand().substring(1).equals(e.getCommand())) {
            e.setResult(CommandResult.command(TabConstants.COMMAND_PROXY + " bossbar"));
        }
        ScoreboardManagerImpl scoreboard = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SCOREBOARD);
        if (scoreboard != null && scoreboard.getToggleCommand().substring(1).equals(e.getCommand())) {
            e.setResult(CommandResult.command(TabConstants.COMMAND_PROXY + " scoreboard"));
        }
    }

    @Subscribe
    public void onPluginMessageEvent(PluginMessageEvent event) {
        if (!event.getIdentifier().getId().equalsIgnoreCase(TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME)) return;
        if (event.getTarget() instanceof Player) {
            event.setResult(PluginMessageEvent.ForwardResult.handled());
            pluginMessage(((Player) event.getTarget()).getUniqueId(), event.getData());
        }
    }

    @Override
    public TabPlayer createPlayer(Player player) {
        return new VelocityTabPlayer(player);
    }
}