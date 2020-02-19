package me.neznamy.tab.platforms.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PluginHooks;
import me.neznamy.tab.shared.Shared;

public class PluginMessenger implements PluginMessageListener {

	private JavaPlugin plugin;
	
	public PluginMessenger(JavaPlugin plugin) {
		this.plugin = plugin;
		Bukkit.getMessenger().registerIncomingPluginChannel(plugin, Shared.CHANNEL_NAME, this);
		Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, Shared.CHANNEL_NAME);
	}
	public void onPluginMessageReceived(String channel, Player player, byte[] bytes){
		if (!channel.equalsIgnoreCase(Shared.CHANNEL_NAME)) return;
		ITabPlayer p = Shared.getPlayer(player.getUniqueId());
		if (p == null) return;
		ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
		String subChannel = in.readUTF();
		if (subChannel.equalsIgnoreCase("Placeholder")){
			String placeholder = in.readUTF();
			long start = System.nanoTime();
			String output = PluginHooks.PlaceholderAPI_setPlaceholders(p, placeholder);
			long time = System.nanoTime() - start;

			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("Placeholder");
			out.writeUTF(placeholder);
			out.writeUTF(output);
			out.writeLong(time);
			player.sendPluginMessage(plugin, Shared.CHANNEL_NAME, out.toByteArray());
		}
	}
}