package me.neznamy.tab.platforms.velocity;

import java.util.Optional;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.command.CommandExecuteEvent.CommandResult;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;

import com.velocitypowered.api.proxy.server.ServerInfo;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.FeatureManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PluginMessageHandler;

/**
 * The core for velocity forwarding events into all enabled features
 */
public class VelocityEventListener {

	private final PluginMessageHandler plm;
	
	public VelocityEventListener(PluginMessageHandler plm) {
		this.plm = plm;
	}
	
	/**
	 * Disconnect event listener to forward the event to all features
	 * @param event - disconnect event
	 */
	@Subscribe
	public void onQuit(DisconnectEvent event) {
		TAB tab = TAB.getInstance();

		if (tab.isDisabled()) {
			return;
		}

		tab.getFeatureManager().onQuit(tab.getPlayer(event.getPlayer().getUniqueId()));
	}

	/**
	 * Listener to join / server switch to forward the event to all features
	 * @param event
	 */
	@Subscribe
	public void onConnect(ServerConnectedEvent event) {
		TAB tab = TAB.getInstance();

		if (tab.isDisabled()) {
			return;
		}

		FeatureManager featureManager = tab.getFeatureManager();
		ServerInfo serverInfo = event.getServer().getServerInfo();
		Player player = event.getPlayer();

		TabPlayer oldTabPlayer = tab.getPlayer(player.getUniqueId());
		try {
			if (oldTabPlayer == null) {
				VelocityTabPlayer tabPlayer = new VelocityTabPlayer(player, plm);
				tabPlayer.setWorldName(serverInfo.getName());
				featureManager.onJoin(tabPlayer);
			} else {
				featureManager.onWorldChange(player.getUniqueId(), serverInfo.getName());
				featureManager.onJoin(oldTabPlayer);
			}

		} catch (Exception ex){
			tab.getErrorManager().criticalError("An error occurred when player joined/changed server", ex);
		}
	}
	
	/**
	 * Listener to commands to forward the event to all features
	 * @param event
	 */
	@Subscribe
	public void onCommand(CommandExecuteEvent event) {
		TAB tab = TAB.getInstance();

		if (tab.isDisabled()) {
			return;
		}

		CommandSource source = event.getCommandSource();

		if (!(source instanceof Player)) {
			return;
		}

		Player player = (Player) source;

		if (tab.getFeatureManager().onCommand(tab.getPlayer(player.getUniqueId()), event.getCommand())) {
			event.setResult(CommandResult.denied());
		}
	}
}