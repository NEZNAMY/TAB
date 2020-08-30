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
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;

/**
 * The event listener part for securing proper functionality of armor stands
 */
public class EventListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(PlayerToggleSneakEvent e) {
		ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null) return;
		if (!p.disabledNametag) Shared.cpu.runMeasuredTask("processing PlayerToggleSneakEvent", TabFeature.NAMETAGX, UsageType.PLAYER_TOGGLE_SNEAK_EVENT, new Runnable() {
			
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
		if (p.previewingNametag) Shared.cpu.runMeasuredTask("processing PlayerMoveEvent", TabFeature.NAMETAGX, UsageType.PLAYER_MOVE_EVENT, new Runnable() {
			
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
		if (!p.disabledNametag) Shared.cpu.runMeasuredTask("processing PlayerRespawnEvent", TabFeature.NAMETAGX, UsageType.PLAYER_RESPAWN_EVENT, new Runnable() {
			
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
		if (!p.disabledNametag) Shared.cpu.runMeasuredTask("processing PlayerTeleportEvent", TabFeature.NAMETAGX, UsageType.PLAYER_TELEPORT_EVENT, new Runnable() {
			
			@Override
			public void run() {
				p.getArmorStandManager().teleport();
			}
		});
	}
}