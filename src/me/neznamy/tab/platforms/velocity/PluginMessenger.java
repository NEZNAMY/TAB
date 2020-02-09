package me.neznamy.tab.platforms.velocity;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;

public class PluginMessenger{

	public PluginMessenger(Main plugin) {
//		Main.server.getPluginManager().registerChannel(Shared.CHANNEL_NAME);
//		Main.server.getChannelRegistrar().register(arg0);
	}
	public void requestPlaceholder(ITabPlayer player, String placeholder) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Placeholder");
		out.writeUTF(placeholder);
//		((TabPlayer)player).player.getServer().sendData(Shared.CHANNEL_NAME, out.toByteArray());
	}
	@Subscribe
	public void on(PluginMessageEvent event){
		if (!event.getIdentifier().getId().equalsIgnoreCase(Shared.CHANNEL_NAME)) return;
		ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
		String subChannel = in.readUTF();
		if (subChannel.equalsIgnoreCase("Placeholder")){
			if (event.getTarget() instanceof Player){
				ITabPlayer receiver = Shared.getPlayer(((Player) event.getTarget()).getUniqueId());
				String placeholder = in.readUTF();
				String output = in.readUTF();
				long cpu = in.readLong();
				PlayerPlaceholder pl = Placeholders.myPlayerPlaceholders.get(placeholder);
				if (pl != null) {
					pl.lastValue.put(receiver.getName(), output);
					pl.lastRefresh.put(receiver.getName(), System.currentTimeMillis());
					Shared.cpu.addPlaceholderTime(placeholder, cpu);
				} else {
					Shared.debug("Received output for unknown placeholder " + placeholder);
				}
			}
		}
	}
}