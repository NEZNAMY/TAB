package me.neznamy.tab.platforms.velocity.v2_0_0;

import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent.ForwardResult;
import com.velocitypowered.api.proxy.connection.Player;
import com.velocitypowered.api.proxy.messages.PluginChannelId;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.PluginMessageHandler;
import net.kyori.adventure.key.Key;

/**
 * A class to request and read PlaceholderAPI values from bukkit server
 */
public class VelocityPluginMessageHandler implements PluginMessageHandler {

	//channel identifier
	private PluginChannelId mc;

	/**
	 * Constructs new instance with given parameter and registers events/channel
	 * @param plugin - instance of main class
	 */
	public VelocityPluginMessageHandler(Main plugin) {
		mc = PluginChannelId.wrap(Key.key("tab", "placeholders"));
		plugin.server.channelRegistrar().register(mc);
		plugin.server.eventManager().register(plugin, this);
	}

	/**
	 * Listener to plugin message event
	 * @param event - plugin message event
	 */
	@Subscribe
	public void on(PluginMessageEvent event){
		if (!event.channel().equals(mc)) return;
		if (event.sink() instanceof Player) {
			long time = System.nanoTime();
			TabPlayer receiver = TAB.getInstance().getPlayer(((Player) event.sink()).id());
			if (receiver == null) return;
			onPluginMessage(receiver, ByteStreams.newDataInput(event.rawMessage()));
			event.setResult(ForwardResult.handled());
			TAB.getInstance().getCPUManager().addTime(TabFeature.PLUGIN_MESSAGE_HANDLING, UsageType.PLUGIN_MESSAGE_EVENT, System.nanoTime()-time);
		}
	}

	@Override
	public void sendPluginMessage(TabPlayer player, byte[] message) {
		Player sender = (Player) player.getPlayer();
		if (sender.connectedServer().isPresent())
			try {
				sender.connectedServer().get().sendPluginMessage(mc, message);
			} catch (IllegalStateException e) {
				// java.lang.IllegalStateException: Not connected to server!
				// this is not the best way to deal with this problem, but i could not find a better one
			}
	}
}