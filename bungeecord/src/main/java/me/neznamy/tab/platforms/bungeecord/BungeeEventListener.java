package me.neznamy.tab.platforms.bungeecord;

import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.PlatformEventListener;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * The core for BungeeCord forwarding events into all enabled features
 */
public class BungeeEventListener extends PlatformEventListener implements Listener {

    @EventHandler
    public void onQuit(PlayerDisconnectEvent e) {
        quit(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onSwitch(ServerSwitchEvent e) {
        if (TAB.getInstance().getPlayer(e.getPlayer().getUniqueId()) == null) {
            join(new BungeeTabPlayer(e.getPlayer()));
        } else {
            serverChange(e.getPlayer().getUniqueId(), e.getPlayer().getServer().getInfo().getName());
        }
    }

    @EventHandler
    public void onChat(ChatEvent e) {
        if (e.isCommand() && command(((ProxiedPlayer)e.getSender()).getUniqueId(), e.getMessage())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(PluginMessageEvent event) {
        if (!event.getTag().equals(TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME)) return;
        if (event.getReceiver() instanceof ProxiedPlayer) {
            event.setCancelled(true);
            pluginMessage(((ProxiedPlayer) event.getReceiver()).getUniqueId(),
                    ((ProxiedPlayer) event.getReceiver()).getName(), event.getData());
        }
    }
}