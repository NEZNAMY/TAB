package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.command.CommandExecuteEvent.CommandResult;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;

import me.neznamy.tab.shared.TAB;

/**
 * The core for velocity forwarding events into all enabled features
 */
public class VelocityEventListener {

	/**
	 * Disconnect event listener to forward the event to all features
	 * @param e - disconnect event
	 */
	@Subscribe
	public void onQuit(DisconnectEvent e){
		if (TAB.getInstance().isDisabled()) return;
		TAB.getInstance().getCPUManager().runTask("processing PlayerDisconnectEvent", () ->
				TAB.getInstance().getFeatureManager().onQuit(TAB.getInstance().getPlayer(e.getPlayer().getUniqueId())));
	}
	
	/**
	 * Listener to join / server switch to forward the event to all features
	 * @param	e
	 * 			connect event
	 */
	@Subscribe
	public void onConnect(ServerPostConnectEvent e){
		Player p = e.getPlayer();
		if (TAB.getInstance().isDisabled()) return;
		if (TAB.getInstance().getPlayer(p.getUniqueId()) == null) {
			TAB.getInstance().getCPUManager().runTask("processing ServerPostConnectEvent", () ->
					TAB.getInstance().getFeatureManager().onJoin(new VelocityTabPlayer(p)));
		} else {
			TAB.getInstance().getCPUManager().runTaskLater(300, "processing ServerPostConnectEvent", () ->
				TAB.getInstance().getFeatureManager().onServerChange(p.getUniqueId(), p.getCurrentServer().isPresent() ? p.getCurrentServer().get().getServerInfo().getName() : "null")
			);
		}
	}

	/**
	 * Listener to commands to forward the event to all features
	 * @param	e
	 * 			command event
	 */
	@Subscribe
	public void onCommand(CommandExecuteEvent e) {
		if (TAB.getInstance().isDisabled()) return;
		if (e.getCommandSource() instanceof Player && TAB.getInstance().getFeatureManager().onCommand(TAB.getInstance().getPlayer(((Player)e.getCommandSource()).getUniqueId()), e.getCommand())) e.setResult(CommandResult.denied());
	}
}