package me.neznamy.tab.platforms.bungeecord;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PluginMessageHandler;
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

	private PluginMessageHandler plm;
	
	public BungeeEventListener(PluginMessageHandler plm) {
		this.plm = plm;
	}
	
	/**
	 * Disconnect event listener to forward the event to all features
	 * @param e - disconnect event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onQuit(PlayerDisconnectEvent e){
		if (TAB.getInstance().isDisabled()) return;
		TAB.getInstance().getFeatureManager().onQuit(TAB.getInstance().getPlayer(e.getPlayer().getUniqueId()));
	}

	/**
	 * Listener to join / server switch to forward the event to all features
	 * @param e
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onSwitch(ServerSwitchEvent e){
		if (TAB.getInstance().isDisabled()) return;
		try {
			if (TAB.getInstance().getPlayer(e.getPlayer().getUniqueId()) == null) {
				TAB.getInstance().getFeatureManager().onJoin(new BungeeTabPlayer(e.getPlayer(), plm));
			} else {
				TAB.getInstance().getFeatureManager().onWorldChange(e.getPlayer().getUniqueId(), e.getPlayer().getServer().getInfo().getName());
			}
		} catch (Exception ex){
			TAB.getInstance().getErrorManager().criticalError("An error occurred when player joined/changed server", ex);
		}
	}

	/**
	 * Listener to chat packets to forward the event to all features
	 * @param e
	 */
	@EventHandler
	public void onChat(ChatEvent e) {
		if (TAB.getInstance().isDisabled()) return;
		if (e.getMessage().startsWith("/") && TAB.getInstance().getFeatureManager().onCommand(TAB.getInstance().getPlayer(((ProxiedPlayer)e.getSender()).getUniqueId()), e.getMessage())) e.setCancelled(true);
	}
}