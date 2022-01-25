package me.neznamy.tab.platforms.bungeecord;

import me.neznamy.tab.shared.TAB;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * The core for BungeeCord forwarding events into all enabled features
 */
public class BungeeEventListener implements Listener {

	/**
	 * Disconnect event listener to forward the event to all features
	 * @param e - disconnect event
	 */
	@EventHandler
	public void onQuit(PlayerDisconnectEvent e){
		if (TAB.getInstance().isDisabled()) return;
		TAB.getInstance().getCPUManager().runTask("processing PlayerDisconnectEvent", () ->
				TAB.getInstance().getFeatureManager().onQuit(TAB.getInstance().getPlayer(e.getPlayer().getUniqueId())));
	}

	/**
	 * Listener to join / server switch to forward the event to all features
	 * @param	e
	 * 			switch event
	 */
	@EventHandler
	public void onSwitch(ServerSwitchEvent e){
		if (TAB.getInstance().isDisabled()) return;
		TAB.getInstance().getCPUManager().runTask("processing ServerSwitchEvent", () -> {
			if (TAB.getInstance().getPlayer(e.getPlayer().getUniqueId()) == null) {
				TAB.getInstance().getFeatureManager().onJoin(new BungeeTabPlayer(e.getPlayer()));
			} else {
				TAB.getInstance().getFeatureManager().onServerChange(e.getPlayer().getUniqueId(), e.getPlayer().getServer().getInfo().getName());
			}
		});
	}

	/**
	 * Listener to chat packets to forward the event to all features
	 * @param	e
	 * 			chat event
	 */
	@EventHandler
	public void onChat(ChatEvent e) {
		if (TAB.getInstance().isDisabled()) return;
		if (e.getMessage().startsWith("/") && TAB.getInstance().getFeatureManager().onCommand(TAB.getInstance().getPlayer(((ProxiedPlayer)e.getSender()).getUniqueId()), e.getMessage())) e.setCancelled(true);
	}
}