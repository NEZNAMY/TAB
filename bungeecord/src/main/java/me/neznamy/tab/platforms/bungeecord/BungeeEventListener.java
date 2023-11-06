package me.neznamy.tab.platforms.bungeecord;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.platform.EventListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

/**
 * The core for BungeeCord forwarding events into all enabled features
 */
public class BungeeEventListener extends EventListener<ProxiedPlayer> implements Listener {

    @EventHandler
    public void onQuit(PlayerDisconnectEvent e) {
        quit(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onSwitch(ServerSwitchEvent e) {
        TAB tab = TAB.getInstance();
        if (tab.isPluginDisabled()) return;
        tab.getCPUManager().runTask(() -> {
            TabPlayer player = tab.getPlayer(e.getPlayer().getUniqueId());
            if (player == null) {
                player = createPlayer(e.getPlayer());

                // I don't think this is actually needed, but someone said it fixed geyser warn in console
                // Doing this won't hurt, so whatever
                if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_20_2.getNetworkId()) {
                    player.getScoreboard().freeze();
                }

                tab.getFeatureManager().onJoin(player);
            } else {
                tab.getFeatureManager().onServerChange(player.getUniqueId(), e.getPlayer().getServer().getInfo().getName());
                if (player.getVersion().getNetworkId() < ProtocolVersion.V1_20_2.getNetworkId()) {
                    // For versions below 1.20.2 the tablist is already clean when this event is called
                    // For 1.20.2+ this event is called before, so we listen to Login packet instead
                    tab.getFeatureManager().onTabListClear(player);
                }
            }
        });
    }

    @EventHandler
    public void onCommand(ChatEvent e) {
        if (e.isCommand() && command(((ProxiedPlayer)e.getSender()).getUniqueId(), e.getMessage())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(PluginMessageEvent e) {
        if (!e.getTag().equals(TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME)) return;
        if (e.getReceiver() instanceof ProxiedPlayer) {
            e.setCancelled(true);
            pluginMessage(((ProxiedPlayer) e.getReceiver()).getUniqueId(), e.getData());
        }
    }

    @Override
    @NotNull
    public TabPlayer createPlayer(@NotNull ProxiedPlayer player) {
        return new BungeeTabPlayer((BungeePlatform) TAB.getInstance().getPlatform(), player);
    }
}