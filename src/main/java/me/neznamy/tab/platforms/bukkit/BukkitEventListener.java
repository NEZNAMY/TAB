package me.neznamy.tab.platforms.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;

/**
 * The core for bukkit forwarding events into all enabled features
 */
public class BukkitEventListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent e) {
		try {
			if (Shared.disabled) return;
			ITabPlayer p = new BukkitTabPlayer(e.getPlayer());
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

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(PlayerQuitEvent e){
		if (Shared.disabled) return;
		ITabPlayer disconnectedPlayer = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (disconnectedPlayer == null) return;
		Shared.cpu.runTask("processing PlayerQuitEvent", new Runnable() {

			public void run() {
				Shared.featureManager.onQuit(disconnectedPlayer);
			}
		});
		Shared.data.remove(e.getPlayer().getUniqueId());
		Shared.entityIdMap.remove(e.getPlayer().getEntityId());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onWorldChange(PlayerChangedWorldEvent e){
		if (Shared.disabled) return;
		ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null) return;
		Shared.cpu.runTask("processing PlayerChangedWorldEvent", new Runnable() {

			@Override
			public void run() {
				long time = System.nanoTime();
				String from = e.getFrom().getName();
				String to = p.world = e.getPlayer().getWorld().getName();
				p.updateDisabledWorlds(to);
				p.updateGroupIfNeeded(false);
				Shared.cpu.addTime(TabFeature.OTHER, UsageType.WORLD_SWITCH_EVENT, System.nanoTime()-time);
				Shared.featureManager.onWorldChange(p, from, to);
			}
		});
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		if (Shared.disabled) return;
		ITabPlayer sender = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (sender == null) return;
		if (e.getMessage().equalsIgnoreCase("/tab") || e.getMessage().equalsIgnoreCase("/tab:tab")) {
			Shared.sendPluginInfo(sender);
			return;
		}
		if (Shared.featureManager.onCommand(sender, e.getMessage())) e.setCancelled(true);
	}
}