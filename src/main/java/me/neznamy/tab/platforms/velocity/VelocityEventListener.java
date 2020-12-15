package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent.ChatResult;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;

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
		if (Shared.disabled) return;
		Shared.featureManager.onQuit(Shared.getPlayer(e.getPlayer().getUniqueId()));
	}
	
	/**
	 * Listener to join / server switch to forward the event to all features
	 * @param e
	 */
	@Subscribe
	public void onConnect(ServerPostConnectEvent e){
		if (Shared.disabled) return;
		try {
			if (!Shared.data.containsKey(e.getPlayer().getUniqueId())) {
				Shared.featureManager.onJoin(new VelocityTabPlayer(e.getPlayer()));
			} else {
				Shared.featureManager.onWorldChange(Shared.getPlayer(e.getPlayer().getUniqueId()), 
						e.getPlayer().getCurrentServer().get().getServerInfo().getName());
			}
		} catch (Throwable ex){
			Shared.errorManager.criticalError("An error occurred when player joined/changed server", ex);
		}
	}
	
	/**
	 * Listener to chat packets to forward the event to all features
	 * @param e
	 */
	@Subscribe
	public void onChat(PlayerChatEvent e) {
		TabPlayer sender = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (sender == null) return;
		if (Shared.featureManager.onCommand(sender, e.getMessage())) e.setResult(ChatResult.denied());
	}
}