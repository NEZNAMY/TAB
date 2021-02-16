package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.command.CommandExecuteEvent.CommandResult;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent.ChatResult;
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
		TAB.getInstance().getFeatureManager().onQuit(TAB.getInstance().getPlayer(e.getPlayer().getUniqueId()));
	}
	
	/**
	 * Listener to join / server switch to forward the event to all features
	 * @param e
	 */
	@Subscribe
	public void onConnect(ServerPostConnectEvent e){
		if (TAB.getInstance().isDisabled()) return;
		try {
			if (!TAB.getInstance().data.containsKey(e.getPlayer().getUniqueId())) {
				TAB.getInstance().getFeatureManager().onJoin(new VelocityTabPlayer(e.getPlayer()));
			} else {
				TAB.getInstance().getFeatureManager().onWorldChange(TAB.getInstance().getPlayer(e.getPlayer().getUniqueId()), e.getPlayer().getCurrentServer().get().getServerInfo().getName());
			}
		} catch (Throwable ex){
			TAB.getInstance().getErrorManager().criticalError("An error occurred when player joined/changed server", ex);
		}
	}
	
	/**
	 * Listener to chat messages to forward the event to all features
	 * @param e
	 */
	@Subscribe
	public void onChat(PlayerChatEvent e) {
		if (TAB.getInstance().isDisabled()) return;
		if (TAB.getInstance().getFeatureManager().onChat(TAB.getInstance().getPlayer(e.getPlayer().getUniqueId()), e.getMessage())) e.setResult(ChatResult.denied());
	}
	
	/**
	 * Listener to commands to forward the event to all features
	 * @param e
	 */
	@Subscribe
	public void onCommand(CommandExecuteEvent e) {
		if (TAB.getInstance().isDisabled()) return;
		if (e.getCommandSource() instanceof Player) {
			if (TAB.getInstance().getFeatureManager().onCommand(TAB.getInstance().getPlayer(((Player)e.getCommandSource()).getUniqueId()), e.getCommand())) e.setResult(CommandResult.denied());
		}
	}
}