package me.neznamy.tab.platforms.bungeecord;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
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
public class BungeeEventListener implements Listener {

    /**
     * Disconnect event listener to forward the event to all features
     *
     * @param   e
     *          disconnect event
     */
    @EventHandler
    public void onQuit(PlayerDisconnectEvent e) {
        if (TabAPI.getInstance().isPluginDisabled()) return;
        TAB.getInstance().getCPUManager().runTask(() ->
                TabAPI.getInstance().getFeatureManager().onQuit(TabAPI.getInstance().getPlayer(e.getPlayer().getUniqueId())));
    }

    /**
     * Listener to join / server switch to forward the event to all features
     *
     * @param   e
     *          switch event
     */
    @EventHandler
    public void onSwitch(ServerSwitchEvent e) {
        if (TabAPI.getInstance().isPluginDisabled()) return;
        TAB.getInstance().getCPUManager().runTask(() -> {
            if (TabAPI.getInstance().getPlayer(e.getPlayer().getUniqueId()) == null) {
                TabAPI.getInstance().getFeatureManager().onJoin(new BungeeTabPlayer(e.getPlayer()));
            } else {
                TabAPI.getInstance().getFeatureManager().onServerChange(e.getPlayer().getUniqueId(), e.getPlayer().getServer().getInfo().getName());
            }
        });
    }

    /**
     * Listener to chat packets to forward the event to all features
     *
     * @param   e
     *          chat event
     */
    @EventHandler
    public void onChat(ChatEvent e) {
        if (TabAPI.getInstance().isPluginDisabled()) return;
        if (e.isCommand() && TabAPI.getInstance().getFeatureManager().onCommand(TabAPI.getInstance().getPlayer(
                ((ProxiedPlayer)e.getSender()).getUniqueId()), e.getMessage())) e.setCancelled(true);
    }

    /**
     * Listener to plugin message event
     *
     * @param   event
     *          plugin message event
     */
    @EventHandler
    public void on(PluginMessageEvent event) {
        if (!event.getTag().equals(TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME)) return;
        if (event.getReceiver() instanceof ProxiedPlayer) {
            event.setCancelled(true);
            ((ProxyPlatform)TAB.getInstance().getPlatform()).getPluginMessageHandler().onPluginMessage(
                    ((ProxiedPlayer) event.getReceiver()).getUniqueId(), ((ProxiedPlayer) event.getReceiver()).getName(), event.getData());
        }
    }
}