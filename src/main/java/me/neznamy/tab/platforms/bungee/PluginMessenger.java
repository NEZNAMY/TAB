package me.neznamy.tab.platforms.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

/**
 * A class to request and read PlaceholderAPI values from bukkit server
 */
public class PluginMessenger implements Listener {

	public PluginMessenger(Plugin plugin) {
		ProxyServer.getInstance().registerChannel(Shared.CHANNEL_NAME);
		ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
	}
	
	public void requestPlaceholder(TabPlayer player, String placeholder) {
		if (player == null) return;
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Placeholder");
		out.writeUTF(placeholder);
		((ProxiedPlayer) player.getPlayer()).getServer().sendData(Shared.CHANNEL_NAME, out.toByteArray());
	}
	
	public void requestAttribute(TabPlayer player, String attribute) {
		if (player == null) return;
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Attribute");
		out.writeUTF(attribute);
		((ProxiedPlayer) player.getPlayer()).getServer().sendData(Shared.CHANNEL_NAME, out.toByteArray());
	}
	
	@EventHandler
	public void on(PluginMessageEvent event){
		if (!event.getTag().equalsIgnoreCase(Shared.CHANNEL_NAME)) return;
		ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
		String subChannel = in.readUTF();
		if (event.getReceiver() instanceof ProxiedPlayer) {
			if (subChannel.equalsIgnoreCase("Placeholder")){
				event.setCancelled(true);
				TabPlayer receiver = Shared.getPlayer(((ProxiedPlayer) event.getReceiver()).getUniqueId());
				if (receiver == null) return;
				String placeholder = in.readUTF();
				String output = in.readUTF();
				long cpu = in.readLong();
				PlayerPlaceholder pl = (PlayerPlaceholder) Placeholders.getPlaceholder(placeholder); //all bridge placeholders are marked as player
				if (pl != null) {
					pl.lastValue.put(receiver.getName(), output);
					Shared.cpu.addBridgePlaceholderTime(pl.getIdentifier(), cpu);
				} else {
					Shared.debug("Received output for unknown placeholder " + placeholder);
				}
			}
			if (subChannel.equals("Attribute")) {
				event.setCancelled(true);
				BungeeTabPlayer receiver = (BungeeTabPlayer) Shared.getPlayer(((ProxiedPlayer) event.getReceiver()).getUniqueId());
				if (receiver == null) return;
				String attribute = in.readUTF();
				String value = in.readUTF();
				receiver.setAttribute(attribute, value);
			}
			
		}
	}
}