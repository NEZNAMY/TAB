package me.neznamy.tab.platforms.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;

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
		if (Shared.disabled) return;
		Shared.cpu.runTask("processing PlayerQuitEvent", () -> Shared.featureManager.onQuit(Shared.getPlayer(e.getPlayer().getUniqueId())));
	}
	
	/**
	 * Listener to PlayerJoinEvent to create player data and forward the event to features
	 * @param e join event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent e) {
		if (Shared.disabled) return;
		Shared.cpu.runTask("processing PlayerJoinEvent", () -> Shared.featureManager.onJoin(new BukkitTabPlayer(e.getPlayer())));
	}

	/**
	 * Listener to PlayerChangedWorldEvent to forward the event to features
	 * @param e world changed event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onWorldChange(PlayerChangedWorldEvent e){
		if (Shared.disabled) return;
		TabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null || !p.isLoaded()) return;
		Shared.cpu.runTask("processing PlayerChangedWorldEvent", () -> Shared.featureManager.onWorldChange(p, e.getPlayer().getWorld().getName()));
	}

	/**
	 * Listener to PlayerChangedWorldEvent to forward the event to features
	 * @param e command preprocess event
	 */
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		if (Shared.disabled) return;
		TabPlayer sender = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (sender == null) return;
		if (e.getMessage().equalsIgnoreCase("/tab") || e.getMessage().equalsIgnoreCase("/tab:tab")) {
			Shared.sendPluginInfo(sender);
			return;
		}
		if (Shared.featureManager.onCommand(sender, e.getMessage())) e.setCancelled(true);
	}

	/**
	 * Listener to PlayerRespawnEvent to forward the event to features
	 * @param e respawn event
	 */
	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		if (Shared.disabled) return;
		TabPlayer respawned = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (respawned == null) return;
		Shared.cpu.runTask("processing PlayerRespawnEvent", () -> Shared.featureManager.onRespawn(respawned));
	}
}