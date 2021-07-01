package me.neznamy.tab.platforms.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.neznamy.tab.shared.TAB;

/**
 * The core for bukkit forwarding events into all enabled features
 */
public class BukkitEventListener implements Listener {

	/**
	 * Listener to PlayerQuitEvent to remove player data and forward the event to features
	 * @param e quit event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(PlayerQuitEvent e){
		if (TAB.getInstance().isDisabled()) return;
		TAB.getInstance().getCPUManager().runTask("processing PlayerQuitEvent", () -> TAB.getInstance().getFeatureManager().onQuit(TAB.getInstance().getPlayer(e.getPlayer().getUniqueId())));
	}
	
	/**
	 * Listener to PlayerJoinEvent to create player data and forward the event to features
	 * @param e join event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent e) {
		if (TAB.getInstance().isDisabled()) return;
		TAB.getInstance().getCPUManager().runTask("processing PlayerJoinEvent", () -> {
			try {
				int i = 0;
				int version;
				while ((version = Main.getProtocolVersion(e.getPlayer())) == -1 && ++i < 100) {
					Thread.sleep(2);
				}
				if (!e.getPlayer().isOnline()) return;
				if (i == 50) {
					TAB.getInstance().getErrorManager().printError("Failed to get protocol version of " + e.getPlayer().getName() + " after 50 retries");
				}
				TAB.getInstance().getFeatureManager().onJoin(new BukkitTabPlayer(e.getPlayer(), version));
			} catch (InterruptedException pluginDisabled) {
				Thread.currentThread().interrupt();
			}
		});
	}

	/**
	 * Listener to PlayerChangedWorldEvent to forward the event to features
	 * @param e world changed event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onWorldChange(PlayerChangedWorldEvent e){
		if (TAB.getInstance().isDisabled()) return;
		TAB.getInstance().getCPUManager().runTask("processing PlayerChangedWorldEvent", () -> TAB.getInstance().getFeatureManager().onWorldChange(e.getPlayer().getUniqueId(), e.getPlayer().getWorld().getName()));
	}

	/**
	 * Listener to PlayerChangedWorldEvent to forward the event to features
	 * @param e command preprocess event
	 */
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		if (TAB.getInstance().isDisabled()) return;
		if (TAB.getInstance().getFeatureManager().onCommand(TAB.getInstance().getPlayer(e.getPlayer().getUniqueId()), e.getMessage())) e.setCancelled(true);
	}
}