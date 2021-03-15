package me.neznamy.tab.platforms.velocity;

import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent.ForwardResult;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.PluginMessageHandler;

/**
 * A class to request and read PlaceholderAPI values from bukkit server
 */
public class VelocityPluginMessageHandler implements PluginMessageHandler {

	//channel identifier
	private MinecraftChannelIdentifier mc;

	/**
	 * Constructs new instance with given parameter and registers events/channel
	 * @param plugin - instance of main class
	 */
	public VelocityPluginMessageHandler(Main plugin) {
		mc = MinecraftChannelIdentifier.create("tab", "placeholders");
		plugin.server.getChannelRegistrar().register(mc);
		plugin.server.getEventManager().register(plugin, this);

	}

	/**
	 * Listener to plugin message event
	 * @param event - plugin message event
	 */
	@Subscribe
	public void on(PluginMessageEvent event){
		if (!event.getIdentifier().getId().equalsIgnoreCase(CHANNEL_NAME)) return;
		if (event.getTarget() instanceof Player) {
			long time = System.nanoTime();
			TabPlayer receiver = TAB.getInstance().getPlayer(((Player) event.getTarget()).getUniqueId());
			if (receiver == null) return;
			onPluginMessage(receiver, ByteStreams.newDataInput(event.getData()));
			event.setResult(ForwardResult.handled());
			TAB.getInstance().getCPUManager().addTime(TabFeature.PLUGIN_MESSAGE_HANDLING, UsageType.PLUGIN_MESSAGE_EVENT, System.nanoTime()-time);
		}
	}

	@Override
	public void sendPluginMessage(TabPlayer player, byte[] message) {
		Player sender = (Player) player.getPlayer();
		if (sender.getCurrentServer().isPresent())
			try {
				sender.getCurrentServer().get().sendPluginMessage(mc, message);
			} catch (IllegalStateException e) {
				// java.lang.IllegalStateException: Not connected to server!
				// this is not the best way to deal with this problem, but i could not find a better one
			}
	}
}