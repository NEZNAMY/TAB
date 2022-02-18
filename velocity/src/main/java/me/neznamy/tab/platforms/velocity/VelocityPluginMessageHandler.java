package me.neznamy.tab.platforms.velocity;

import java.util.Optional;

import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent.ForwardResult;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.util.Preconditions;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PluginMessageHandler;

/**
 * A class to request and read PlaceholderAPI values from bukkit server
 */
public class VelocityPluginMessageHandler extends PluginMessageHandler {

	//channel identifier
	private final MinecraftChannelIdentifier mc;

	/**
	 * Constructs new instance with given parameter and registers events/channel
	 * @param plugin - instance of main class
	 */
	public VelocityPluginMessageHandler(Main plugin) {
		Preconditions.checkNotNull(plugin, "plugin");
		String[] name = TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME.split(":");
		mc = MinecraftChannelIdentifier.create(name[0], name[1]);
		plugin.getServer().getChannelRegistrar().register(mc);
		plugin.getServer().getEventManager().register(plugin, this);

	}

	/**
	 * Listener to plugin message event
	 * @param event - plugin message event
	 */
	@Subscribe
	public void on(PluginMessageEvent event){
		if (!event.getIdentifier().getId().equalsIgnoreCase(TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME)) return;
		if (event.getTarget() instanceof Player) {
			event.setResult(ForwardResult.handled());
			TAB.getInstance().getCPUManager().runMeasuredTask("Plugin message handling",
					TabConstants.CpuUsageCategory.PLUGIN_MESSAGE, () -> {
				VelocityTabPlayer receiver = (VelocityTabPlayer) TAB.getInstance().getPlayer(((Player) event.getTarget()).getUniqueId());
				if (receiver == null) return;
				onPluginMessage(receiver, ByteStreams.newDataInput(event.getData()));
			});
		}
	}

	@Override
	public void sendPluginMessage(TabPlayer player, byte[] message) {
		Preconditions.checkNotNull(player, "player");
		Preconditions.checkNotNull(message, "message");
//		((Player) player.getPlayer()).sendPluginMessage(mc, message); //not using this short alternative because it just doesn't work
		try {
			Player sender = (Player) player.getPlayer();
			Optional<ServerConnection> server = sender.getCurrentServer();
			if (server.isPresent()) {
				server.get().sendPluginMessage(mc, message);
				TAB.getInstance().getCPUManager().packetSent("Plugin Message (" + new String(message) + ")");
			}
		} catch (IllegalStateException e) {
			//java.lang.IllegalStateException: Not connected to server!
		}
	}
}