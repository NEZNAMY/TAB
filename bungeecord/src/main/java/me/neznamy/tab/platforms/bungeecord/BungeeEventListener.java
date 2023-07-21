package me.neznamy.tab.platforms.bungeecord;

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
        serverChange(e.getPlayer(), e.getPlayer().getUniqueId(), e.getPlayer().getServer().getInfo().getName());
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
    public TabPlayer createPlayer(ProxiedPlayer player) {
        return new BungeeTabPlayer(player);
    }
}