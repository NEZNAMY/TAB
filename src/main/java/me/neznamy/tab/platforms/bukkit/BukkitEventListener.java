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
import me.neznamy.tab.shared.features.interfaces.CommandListener;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.QuitEventListener;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;

/**
 * The core for bukkit forwarding events into all enabled features
 */
public class BukkitEventListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent e) {
		try {
			if (Shared.disabled) return;
			ITabPlayer p = new TabPlayer(e.getPlayer());
			Shared.data.put(e.getPlayer().getUniqueId(), p);
			Shared.entityIdMap.put(e.getPlayer().getEntityId(), p);
			Main.inject(e.getPlayer().getUniqueId());
			Shared.cpu.runTask("processing PlayerJoinEvent", new Runnable() {

				public void run() {
					for (JoinEventListener l : Shared.joinListeners) {
						long time = System.nanoTime();
						l.onJoin(p);
						Shared.cpu.addTime(l.getFeatureType(), UsageType.PLAYER_JOIN_EVENT, System.nanoTime()-time);
					}
					p.onJoinFinished = true;
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
				for (QuitEventListener l : Shared.quitListeners) {
					long time = System.nanoTime();
					l.onQuit(disconnectedPlayer);
					Shared.cpu.addTime(l.getFeatureType(), UsageType.PLAYER_QUIT_EVENT, System.nanoTime()-time);
				}
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
				for (WorldChangeListener l : Shared.worldChangeListeners) {
					time = System.nanoTime();
					l.onWorldChange(p, from, to);
					Shared.cpu.addTime(l.getFeatureType(), UsageType.WORLD_SWITCH_EVENT, System.nanoTime()-time);
				}
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
		for (CommandListener listener : Shared.commandListeners) {
			if (listener.onCommand(sender, e.getMessage())) e.setCancelled(true);
		}
	}
}