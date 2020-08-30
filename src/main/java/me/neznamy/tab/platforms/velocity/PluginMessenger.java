package me.neznamy.tab.platforms.velocity;

import java.util.Set;

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
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;

/**
 * A class to request and read PlaceholderAPI values from bukkit server
 */
public class PluginMessenger{

	private MinecraftChannelIdentifier mc;
	
	public PluginMessenger(Main plugin) {
		mc = MinecraftChannelIdentifier.create("tab", "placeholders");
		plugin.server.getChannelRegistrar().register(mc);
		plugin.server.getEventManager().register(plugin, this);
		
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
		if (event.getTarget() instanceof Player && subChannel.equalsIgnoreCase("Placeholder")){
			event.setResult(ForwardResult.handled());
			ITabPlayer receiver = Shared.getPlayer(((Player) event.getTarget()).getUniqueId());
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