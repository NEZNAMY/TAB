package me.neznamy.tab.platforms.bungee;

import java.util.Set;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
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
	public void requestPlaceholder(ITabPlayer player, String placeholder) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Placeholder");
		out.writeUTF(placeholder);
		ProxiedPlayer sender;
		if (player == null) {
			if (ProxyServer.getInstance().getPlayers().isEmpty()) return;
			sender = ProxyServer.getInstance().getPlayers().toArray(new ProxiedPlayer[0])[0];
		} else {
			sender = (ProxiedPlayer) player.getPlayer();
		}
		sender.getServer().sendData(Shared.CHANNEL_NAME, out.toByteArray());
	}
	@EventHandler
	public void on(PluginMessageEvent event){
		if (!event.getTag().equalsIgnoreCase(Shared.CHANNEL_NAME)) return;
		ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
		String subChannel = in.readUTF();
		if (event.getReceiver() instanceof ProxiedPlayer && subChannel.equalsIgnoreCase("Placeholder")){
			event.setCancelled(true);
			ITabPlayer receiver = Shared.getPlayer(((ProxiedPlayer) event.getReceiver()).getUniqueId());
			if (receiver == null) return;
			String placeholder = in.readUTF();
			String output = in.readUTF();
			long cpu = in.readLong();
			PlayerPlaceholder pl = (PlayerPlaceholder) Placeholders.getPlaceholder(placeholder); //all bridge placeholders are marked as player
			if (pl != null) {
				pl.lastValue.put(receiver.getName(), output);
				pl.lastValue.put("null", output);
				Set<Refreshable> update = PlaceholderManager.getPlaceholderUsage(pl.getIdentifier());
				for (Refreshable r : update) {
					long startTime = System.nanoTime();
					r.refresh(receiver, false);
					Shared.cpu.addTime(r.getFeatureType(), UsageType.REFRESHING, System.nanoTime()-startTime);
				}
				Shared.cpu.addBridgePlaceholderTime(pl.getIdentifier(), cpu);
			} else {
				Shared.debug("Received output for unknown placeholder " + placeholder);
			}
		}
	}
}