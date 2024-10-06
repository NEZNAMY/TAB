package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.command.CommandExecuteEvent.CommandResult;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;
import me.neznamy.tab.shared.platform.EventListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The core for Velocity forwarding events into all enabled features
 */
@SuppressWarnings("UnstableApiUsage")
public class VelocityEventListener implements EventListener<Player> {

    /** Map for tracking online players */
    private final Map<Player, UUID> players = new ConcurrentHashMap<>();

    /**
     * Listens to player disconnecting from the server.
     *
     * @param   e
     *          Disconnect event
     */
    @Subscribe
    public void onQuit(@NotNull DisconnectEvent e) {
        // Check if the player was actually connected to the server in the first place to avoid processing
        // disconnect of an existing player who is still there (because players are mapped by UUID in TAB)
        UUID id = players.remove(e.getPlayer());
        if (id != null) quit(id);
    }

    /**
     * Freezes Boss bar for 1.20.2+ players due to bug with adventure that causes disconnect
     * on 1.20.5+ with "Network Protocol Error"
     *
     * @param   e
     *          Event fired before player switches server for proper freezing
     */
    @Subscribe(order = PostOrder.LAST)
    public void preConnect(@NotNull ServerPreConnectEvent e) {
        if (e.getResult().isAllowed()) {
            TabPlayer p = TAB.getInstance().getPlayer(e.getPlayer().getUniqueId());
            if (p != null && p.getVersion().getNetworkId() >= ProtocolVersion.V1_20_2.getNetworkId()) {
                p.getBossBar().freeze();
            }
        }
    }

    /**
     * Listens to player connecting to a backend server. This handles
     * both initial connections and server switch.
     *
     * @param   e
     *          Server switch event
     */
    @Subscribe
    public void onConnect(@NotNull ServerPostConnectEvent e) {
        TAB tab = TAB.getInstance();
        if (tab.isPluginDisabled()) return;
        tab.getCPUManager().runTask(() -> {
            TabPlayer player = tab.getPlayer(e.getPlayer().getUniqueId());
            if (player == null) {
                players.put(e.getPlayer(), e.getPlayer().getUniqueId());
                tab.getFeatureManager().onJoin(createPlayer(e.getPlayer()));
            } else {
                player.getScoreboard().freeze(); // Prevent server switch listeners from sending packets before re-registering objectives in onLoginPacket
                tab.getFeatureManager().onServerChange(
                        player.getUniqueId(),
                        e.getPlayer().getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("null")
                );
                tab.getFeatureManager().onTabListClear(player);
                tab.getFeatureManager().onLoginPacket(player);
            }
        });
    }

    /**
     * Listens to command execute event to potentially cancel it.
     *
     * @param   e
     *          Command execute event
     */
    @Subscribe
    public void onCommand(@NotNull CommandExecuteEvent e) {
        BossBarManagerImpl bossBarManager = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.BOSS_BAR);
        if (bossBarManager != null && bossBarManager.getCommand().substring(1).equals(e.getCommand())) {
            e.setResult(CommandResult.command(TabConstants.COMMAND_PROXY + " bossbar"));
        }
        ScoreboardManagerImpl scoreboard = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SCOREBOARD);
        if (scoreboard != null && scoreboard.getCommand().substring(1).equals(e.getCommand())) {
            e.setResult(CommandResult.command(TabConstants.COMMAND_PROXY + " scoreboard"));
        }
    }

    /**
     * Listens to plugin messages.
     *
     * @param   e
     *          Plugin message event
     */
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
