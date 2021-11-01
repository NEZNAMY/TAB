package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.command.CommandExecuteEvent.CommandResult;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PluginMessageHandler;

/**
 * The core for velocity forwarding events into all enabled features
 */
public class VelocityEventListener {

	private PluginMessageHandler plm;
	
	public VelocityEventListener(PluginMessageHandler plm) {
		this.plm = plm;
	}
	
	/**
	 * Disconnect event listener to forward the event to all features
	 * @param e - disconnect event
	 */
	@Subscribe
	public void onQuit(DisconnectEvent e){
		if (TAB.getInstance().isDisabled()) return;
		TAB.getInstance().getFeatureManager().onQuit(TAB.getInstance().getPlayer(e.getPlayer().getUniqueId()));
	}
	
	/**
	 * Listener to join / server switch to forward the event to all features
	 * @param e
	 */
	@Subscribe
	public void onConnect(ServerPostConnectEvent e){
		if (TAB.getInstance().isDisabled()) return;
		if (TAB.getInstance().getPlayer(e.getPlayer().getUniqueId()) == null) {
			TAB.getInstance().getFeatureManager().onJoin(new VelocityTabPlayer(e.getPlayer(), plm));
		} else {
			TAB.getInstance().getCPUManager().runTaskLater(100, "processing server switch", () -> {
				TAB.getInstance().getFeatureManager().onServerChange(e.getPlayer().getUniqueId(), e.getPlayer().getCurrentServer().isPresent() ? e.getPlayer().getCurrentServer().get().getServerInfo().getName() : "null");
			});
		}
	}
	
	/**
	 * Listener to commands to forward the event to all features
	 * @param e
	 */
	@Subscribe
	public void onCommand(CommandExecuteEvent e) {
		if (TAB.getInstance().isDisabled()) return;
		if (e.getCommandSource() instanceof Player && TAB.getInstance().getFeatureManager().onCommand(TAB.getInstance().getPlayer(((Player)e.getCommandSource()).getUniqueId()), e.getCommand())) e.setResult(CommandResult.denied());
	}
}