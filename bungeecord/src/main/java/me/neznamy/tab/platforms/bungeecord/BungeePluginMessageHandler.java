package me.neznamy.tab.platforms.bungeecord;

import com.google.common.io.ByteStreams;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.PluginMessageHandler;
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
		ProxyServer.getInstance().registerChannel(CHANNEL_NAME);
		ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
	}

	/**
	 * Listener to plugin message event
	 * @param event - plugin message event
	 */
	@EventHandler
	public void on(PluginMessageEvent event){
		if (!event.getTag().equalsIgnoreCase(CHANNEL_NAME)) return;
		if (event.getReceiver() instanceof ProxiedPlayer) {
			long time = System.nanoTime();
			TabPlayer receiver = TAB.getInstance().getPlayer(((ProxiedPlayer) event.getReceiver()).getUniqueId());
			if (receiver == null) return;
			onPluginMessage(receiver, ByteStreams.newDataInput(event.getData()));
			event.setCancelled(true);
			TAB.getInstance().getCPUManager().addTime(TabFeature.PLUGIN_MESSAGE_HANDLING, UsageType.PLUGIN_MESSAGE_EVENT, System.nanoTime()-time);
		}
	}

	@Override
	public void sendPluginMessage(TabPlayer player, byte[] message) {
		if (((ProxiedPlayer) player.getPlayer()).getServer() == null) return; //not connected to any server yet
		((ProxiedPlayer) player.getPlayer()).getServer().sendData(CHANNEL_NAME, message);
	}
}