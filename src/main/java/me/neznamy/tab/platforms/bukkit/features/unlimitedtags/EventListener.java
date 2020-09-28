package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;

/**
 * The event listener part for securing proper functionality of armor stands
 */
public class EventListener implements Listener {

	private NameTagX feature;
	
	public EventListener(NameTagX feature) {
		this.feature = feature;
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(PlayerToggleSneakEvent e) {
		TabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null) return;
		if (!feature.isDisabledWorld(p.getWorldName())) Shared.cpu.runMeasuredTask("processing PlayerToggleSneakEvent", TabFeature.NAMETAGX, UsageType.PLAYER_TOGGLE_SNEAK_EVENT, new Runnable() {
			
			@Override
			public void run() {
				p.getArmorStandManager().sneak(e.isSneaking());
			}
		});
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(PlayerMoveEvent e) {
		TabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null) return;
		if (p.isPreviewingNametag()) Shared.cpu.runMeasuredTask("processing PlayerMoveEvent", TabFeature.NAMETAGX, UsageType.PLAYER_MOVE_EVENT, new Runnable() {
			
			@Override
			public void run() {
				p.getArmorStandManager().teleport(p);
			}
		});
	}
}