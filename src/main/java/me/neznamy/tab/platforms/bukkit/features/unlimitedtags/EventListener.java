package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

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

/**
 * The event listener part for securing proper functionality of armor stands
 */
public class EventListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(PlayerToggleSneakEvent e) {
		ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null) return;
		if (!p.disabledNametag) Shared.featureCpu.runMeasuredTask("processing PlayerToggleSneakEvent", CPUFeature.NAMETAGX_EVENT_SNEAK, new Runnable() {
			
			@Override
			public void run() {
				p.getArmorStandManager().sneak(e.isSneaking());
			}
		});
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(PlayerMoveEvent e) {
		ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null) return;
		if (p.previewingNametag) Shared.featureCpu.runMeasuredTask("processing PlayerMoveEvent", CPUFeature.NAMETAGX_EVENT_MOVE, new Runnable() {
			
			@Override
			public void run() {
				p.getArmorStandManager().teleport(p);
			}
		});
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(PlayerRespawnEvent e) {
		ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null) return;
		if (!p.disabledNametag) Shared.featureCpu.runMeasuredTask("processing PlayerRespawnEvent", CPUFeature.NAMETAGX_EVENT_RESPAWN, new Runnable() {
			
			@Override
			public void run() {
				p.getArmorStandManager().teleport();
			}
		});
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(PlayerTeleportEvent e) {
		ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null || !p.onJoinFinished) return;
		if (!p.disabledNametag) Shared.featureCpu.runMeasuredTask("processing PlayerTeleportEvent", CPUFeature.NAMETAGX_EVENT_TELEPORT, new Runnable() {
			
			@Override
			public void run() {
				p.getArmorStandManager().teleport();
			}
		});
	}
}