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
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.interfaces.CommandListener;

public class BukkitEventListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent e) {
		try {
			if (Shared.disabled) return;
			if (Configs.bukkitBridgeMode) return;
			ITabPlayer p = new TabPlayer(e.getPlayer());
			Shared.data.put(e.getPlayer().getUniqueId(), p);
			Shared.entityIdMap.put(e.getPlayer().getEntityId(), p);
			Main.inject(e.getPlayer().getUniqueId());
			Shared.featureCpu.runMeasuredTask("processing player join", CPUFeature.OTHER, new Runnable() {

				public void run() {
					Shared.joinListeners.forEach(f -> f.onJoin(p));
					p.onJoinFinished = true;
				}
			});
		} catch (Throwable ex) {
			Shared.errorManager.criticalError("An error occurred when processing PlayerJoinEvent", ex);
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(PlayerQuitEvent e){
		try {
			if (Shared.disabled) return;
			if (Configs.bukkitBridgeMode) return;
			ITabPlayer disconnectedPlayer = Shared.getPlayer(e.getPlayer().getUniqueId());
			if (disconnectedPlayer == null) return;
			Shared.quitListeners.forEach(f -> f.onQuit(disconnectedPlayer));
		} catch (Throwable t) {
			Shared.errorManager.printError("An error occurred when processing PlayerQuitEvent", t);
		}
		Shared.data.remove(e.getPlayer().getUniqueId());
		Shared.entityIdMap.remove(e.getPlayer().getEntityId());
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void onWorldChange(PlayerChangedWorldEvent e){
		try {
			if (Shared.disabled) return;
			if (Configs.bukkitBridgeMode) return;
			ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
			if (p == null) return;
			p.onWorldChange(e.getFrom().getName(), p.world = e.getPlayer().getWorld().getName());
		} catch (Throwable t) {
			Shared.errorManager.printError("An error occurred when processing PlayerChangedWorldEvent", t);
		}
	}
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		if (Shared.disabled) return;
		if (Configs.bukkitBridgeMode) return;
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