package me.neznamy.tab.platforms.velocity;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent.ForwardResult;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;

public class PluginMessenger{

	private MinecraftChannelIdentifier mc;
	
	public PluginMessenger(Main plugin) {
		mc = MinecraftChannelIdentifier.create("tab", "placeholders");
		Main.server.getChannelRegistrar().register(mc);
		Main.server.getEventManager().register(plugin, this);
		
	}
	public void requestPlaceholder(ITabPlayer player, String placeholder) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Placeholder");
		out.writeUTF(placeholder);
		if (player.getVelocityEntity().getCurrentServer().isPresent())
			try {
				player.getVelocityEntity().getCurrentServer().get().sendPluginMessage(mc, out.toByteArray());
			} catch (IllegalStateException e) {
				// java.lang.IllegalStateException: Not connected to server!
				// this is not the best way to deal with this problem, but i could not find a better one
			}
			
	}
	@Subscribe
	public void on(PluginMessageEvent event){
		if (!event.getIdentifier().getId().equalsIgnoreCase(Shared.CHANNEL_NAME)) return;
		ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
		String subChannel = in.readUTF();
		if (subChannel.equalsIgnoreCase("Placeholder")){
			if (event.getTarget() instanceof Player){
				event.setResult(ForwardResult.handled());
				ITabPlayer receiver = Shared.getPlayer(((Player) event.getTarget()).getUniqueId());
				if (receiver == null) return;
				String placeholder = in.readUTF();
				String output = in.readUTF();
				long cpu = in.readLong();
				PlayerPlaceholder pl = (PlayerPlaceholder) Placeholders.getUsedPlaceholder(placeholder); //all bridge placeholders are marked as player
				if (pl != null) {
					pl.lastValue.put(receiver.getName(), output);
					pl.lastRefresh.put(receiver.getName(), System.currentTimeMillis());
					Shared.bukkitBridgePlaceholderCpu.addTime(placeholder, cpu);
				} else {
					Shared.debug("Received output for unknown placeholder " + placeholder);
				}
			}
		}
	}
}