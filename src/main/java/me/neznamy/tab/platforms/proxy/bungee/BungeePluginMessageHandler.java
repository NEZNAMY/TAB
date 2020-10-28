package me.neznamy.tab.platforms.proxy.bungee;

import com.google.common.io.ByteStreams;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.proxy.PluginMessageHandler;
import me.neznamy.tab.shared.Shared;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

/**
 * A class to request and read PlaceholderAPI values from bukkit server
 */
public class BungeePluginMessageHandler implements Listener, PluginMessageHandler {

	/**
	 * Constructs new instance with given parameter and registers events/channel
	 * @param plugin - instance of main class
	 */
	public BungeePluginMessageHandler(Plugin plugin) {
		ProxyServer.getInstance().registerChannel(Shared.CHANNEL_NAME);
		ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
	}

	/**
	 * Listener to plugin message event
	 * @param event - plugin message event
	 */
	@EventHandler
	public void on(PluginMessageEvent event){
		try {
			if (!event.getTag().equalsIgnoreCase(Shared.CHANNEL_NAME)) return;
			if (event.getReceiver() instanceof ProxiedPlayer) {
				TabPlayer receiver = Shared.getPlayer(((ProxiedPlayer) event.getReceiver()).getUniqueId());
				if (receiver == null) return;
				if (onPluginMessage(receiver, ByteStreams.newDataInput(event.getData()))) {
					event.setCancelled(true);
				}
			}
		} catch (Exception e) {
			Shared.errorManager.printError("Failed to handle incoming plugin message", e);
		}
	}

	@Override
	public void sendPluginMessage(TabPlayer player, byte[] message) {
		((ProxiedPlayer) player.getPlayer()).getServer().sendData(Shared.CHANNEL_NAME, message);
	}
}