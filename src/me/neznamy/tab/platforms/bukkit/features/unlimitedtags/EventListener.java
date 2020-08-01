package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.CPUFeature;

public class EventListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(PlayerToggleSneakEvent e) {
		ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null) return;
		if (!p.disabledNametag) Shared.featureCpu.runMeasuredTask("processing PlayerToggleSneakEvent", CPUFeature.NAMETAGX_EVENT_SNEAK, new Runnable() {
			public void run() {
				p.getArmorStands().forEach(a -> a.sneak(e.isSneaking()));
			}
		});
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(PlayerMoveEvent e) {
		ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null) return;
		if (p.previewingNametag) Shared.featureCpu.runMeasuredTask("processing PlayerMoveEvent", CPUFeature.NAMETAGX_EVENT_MOVE, new Runnable() {
			public void run() {
				p.getArmorStands().forEach(a -> p.sendPacket(a.getTeleportPacket(p)));
			}
		});
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(PlayerRespawnEvent e) {
		ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null) return;
		if (!p.disabledNametag) Shared.featureCpu.runMeasuredTask("processing PlayerRespawnEvent", CPUFeature.NAMETAGX_EVENT_RESPAWN, new Runnable() {
			public void run() {
				for (ArmorStand as : p.getArmorStands()) {
					List<ITabPlayer> nearbyPlayers = as.getNearbyPlayers();
					synchronized (nearbyPlayers){
						for (ITabPlayer nearby : nearbyPlayers) {
							nearby.sendPacket(as.getTeleportPacket(nearby));
						}
					}
				}
			}
		});
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(PlayerTeleportEvent e) {
		ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null) return;
		if (!p.disabledNametag) Shared.featureCpu.runMeasuredTask("processing PlayerTeleportEvent", CPUFeature.NAMETAGX_EVENT_TELEPORT, new Runnable() {
			public void run() {
				for (ArmorStand as : p.getArmorStands()) {
					List<ITabPlayer> nearbyPlayers = as.getNearbyPlayers();
					synchronized (nearbyPlayers){
						for (ITabPlayer nearby : nearbyPlayers) {
							nearby.sendPacket(as.getTeleportPacket(nearby));
						}
					}
				}
			}
		});
	}
}