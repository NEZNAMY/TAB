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
import org.jetbrains.annotations.NotNull;

/**
 * The core for Velocity forwarding events into all enabled features
 */
@SuppressWarnings("UnstableApiUsage")
public class VelocityEventListener extends EventListener<Player> {

    @Subscribe
    public void onQuit(@NotNull DisconnectEvent e) {
        quit(e.getPlayer().getUniqueId());
    }

    @Subscribe
    public void onConnect(@NotNull ServerPostConnectEvent e) {
        serverChange(
                e.getPlayer(), e.getPlayer().getUniqueId(),
                e.getPlayer().getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("null")
        );
    }

    @Subscribe
    public void onCommand(@NotNull CommandExecuteEvent e) {
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
    public void onPluginMessageEvent(@NotNull PluginMessageEvent e) {
        if (!e.getIdentifier().getId().equals(TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME)) return;
        if (e.getTarget() instanceof Player) {
            e.setResult(PluginMessageEvent.ForwardResult.handled());
            pluginMessage(((Player) e.getTarget()).getUniqueId(), e.getData());
        }
    }

    @Override
    @NotNull
    public TabPlayer createPlayer(@NotNull Player player) {
        return new VelocityTabPlayer((VelocityPlatform) TAB.getInstance().getPlatform(), player);
    }
}