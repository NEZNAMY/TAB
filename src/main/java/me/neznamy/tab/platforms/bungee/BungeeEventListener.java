package me.neznamy.tab.platforms.bungee;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

/**
 * The core for bungeecord forwarding events into all enabled features
 */
public class BungeeEventListener implements Listener {

	/**
	 * Disconnect event listener to forward the event to all features
	 * @param e - disconnect event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onQuit(PlayerDisconnectEvent e){
		if (Shared.disabled) return;
		Shared.featureManager.onQuit(Shared.getPlayer(e.getPlayer().getUniqueId()));
	}

	/**
	 * Listener to join / server switch to forward the event to all features
	 * @param e
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onSwitch(ServerSwitchEvent e){
		if (Shared.disabled) return;
		try {
			if (!Shared.data.containsKey(e.getPlayer().getUniqueId())) {
				Shared.featureManager.onJoin(new BungeeTabPlayer(e.getPlayer()));
			} else {
				Shared.featureManager.onWorldChange(Shared.getPlayer(e.getPlayer().getUniqueId()), 
						e.getPlayer().getServer().getInfo().getName());
			}
		} catch (Throwable ex){
			Shared.errorManager.criticalError("An error occurred when player joined/changed server", ex);
		}
	}

	/**
	 * Listener to chat packets to forward the event to all features
	 * @param e
	 */
	@EventHandler
	public void onChat(ChatEvent e) {
		TabPlayer sender = Shared.getPlayer(((ProxiedPlayer)e.getSender()).getUniqueId());
		if (sender == null) return;
		if (Shared.featureManager.onCommand(sender, e.getMessage())) e.setCancelled(true);
	}
}