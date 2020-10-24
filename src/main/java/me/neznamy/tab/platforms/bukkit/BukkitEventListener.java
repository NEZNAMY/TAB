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
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;

/**
 * The core for bukkit forwarding events into all enabled features
 */
public class BukkitEventListener implements Listener {

	/**
	 * Listener to PlayerJoinEvent to create player data and forward the event to features
	 * @param e join event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent e) {
		try {
			if (Shared.disabled) return;
			TabPlayer p = new BukkitTabPlayer(e.getPlayer());
			Shared.data.put(e.getPlayer().getUniqueId(), p);
			Shared.entityIdMap.put(e.getPlayer().getEntityId(), p);
			Main.inject(e.getPlayer().getUniqueId());
			Shared.cpu.runTask("processing PlayerJoinEvent", new Runnable() {

				public void run() {
					Shared.featureManager.onJoin(p);
				}
			});
		} catch (Throwable ex) {
			Shared.errorManager.criticalError("An error occurred when processing PlayerJoinEvent", ex);
		}
	}

	/**
	 * Listener to PlayerQuitEvent to remove player data and forward the event to features
	 * @param e quit event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(PlayerQuitEvent e){
		if (Shared.disabled) return;
		TabPlayer disconnectedPlayer = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (disconnectedPlayer == null) return;
		Shared.cpu.runTask("processing PlayerQuitEvent", new Runnable() {

			public void run() {
				Shared.featureManager.onQuit(disconnectedPlayer);
			}
		});
		Shared.data.remove(e.getPlayer().getUniqueId());
		Shared.entityIdMap.remove(e.getPlayer().getEntityId());
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
		Shared.cpu.runTask("processing PlayerChangedWorldEvent", new Runnable() {

			@Override
			public void run() {
				long time = System.nanoTime();
				String from = e.getFrom().getName();
				String to = e.getPlayer().getWorld().getName();
				p.setWorldName(to);
				Shared.cpu.addTime(TabFeature.OTHER, UsageType.WORLD_SWITCH_EVENT, System.nanoTime()-time);
				Shared.featureManager.onWorldChange(p, from, to);
			}
		});
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
		Shared.cpu.runTask("processing PlayerRespawnEvent", new Runnable() {

			public void run() {
				Shared.featureManager.onRespawn(respawned);
			}
		});
	}
}