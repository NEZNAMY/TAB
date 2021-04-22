package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;

/**
 * The event listener part for securing proper functionality of armor stands
 */
public class EventListener implements Listener {
	
	//the nametag feature handler
	private NameTagX feature;

	/**
	 * Constructs new instance with given parameters
	 * @param feature - nametagx feature handler
	 */
	public EventListener(NameTagX feature) {
		this.feature = feature;

	}
	
	/**
	 * Teleport event listener to track vehicles & send own packets when using nametag preview
	 * @param e - teleport event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onTeleport(PlayerTeleportEvent e) {
		TabPlayer p = TAB.getInstance().getPlayer(e.getPlayer().getUniqueId());
		if (p == null || feature.isDisabledWorld(p.getWorldName()) || feature.isDisabledWorld(feature.disabledUnlimitedWorlds, p.getWorldName())) return;
		TAB.getInstance().getCPUManager().runTaskLater(100, "processing PlayerTeleportEvent", TabFeature.NAMETAGX, UsageType.PLAYER_TELEPORT_EVENT, () -> {
			if (p.getArmorStandManager() != null) p.getArmorStandManager().teleport();
		});
	}
	
	/**
	 * Sneak event listener to despawn & spawn armor stands to skip animation
	 * @param e - sneak event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSneak(PlayerToggleSneakEvent e) {
		TabPlayer p = TAB.getInstance().getPlayer(e.getPlayer().getUniqueId());
		if (p == null || !p.isLoaded() || feature.isDisabledWorld(p.getWorldName()) || feature.isDisabledWorld(feature.disabledUnlimitedWorlds, p.getWorldName())) return;
		TAB.getInstance().getCPUManager().runMeasuredTask("processing PlayerToggleSneakEvent", TabFeature.NAMETAGX, UsageType.PLAYER_TOGGLE_SNEAK_EVENT, () -> {
			if (p.getArmorStandManager() != null) p.getArmorStandManager().sneak(e.isSneaking());
		});
	}
}