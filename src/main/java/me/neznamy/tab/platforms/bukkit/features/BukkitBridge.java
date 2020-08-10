package me.neznamy.tab.platforms.bukkit.features;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.neznamy.tab.platforms.bukkit.PluginHooks;
import me.neznamy.tab.shared.Shared;

public class BukkitBridge implements PluginMessageListener {

	private JavaPlugin plugin;
	private Set<String> ignored = new HashSet<String>();
	private ExpansionDownloader downloader = new ExpansionDownloader();
	
	public BukkitBridge(JavaPlugin plugin) {
		this.plugin = plugin;
		Bukkit.getMessenger().registerIncomingPluginChannel(plugin, Shared.CHANNEL_NAME, this);
		Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, Shared.CHANNEL_NAME);
	}
	
	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] bytes){
		if (!channel.equalsIgnoreCase(Shared.CHANNEL_NAME)) return;
		ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
		String subChannel = in.readUTF();
		if (subChannel.equalsIgnoreCase("Placeholder")){
			String identifier = in.readUTF();
			long start = System.nanoTime();
			String output = PluginHooks.setPlaceholders(player, identifier);
			long time = System.nanoTime() - start;
			
			if (identifier.equals(output)) {
				String expansion = identifier.split("_")[0].substring(1);
				if (!ignored.contains(expansion)) {
					ignored.add(expansion);
					downloader.download(expansion);
				}
			}
			
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("Placeholder");
			out.writeUTF(identifier);
			out.writeUTF(output);
			out.writeLong(time);
			player.sendPluginMessage(plugin, Shared.CHANNEL_NAME, out.toByteArray());
		}
	}
}