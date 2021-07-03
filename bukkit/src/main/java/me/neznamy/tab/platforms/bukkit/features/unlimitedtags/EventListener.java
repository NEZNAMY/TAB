package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
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
	 * Sneak event listener to despawn & spawn armor stands to skip animation
	 * @param e - sneak event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSneak(PlayerToggleSneakEvent e) {
		TabPlayer p = TAB.getInstance().getPlayer(e.getPlayer().getUniqueId());
		if (p == null || !p.isLoaded() || feature.isInDisabledWorld(p)) return;
		TAB.getInstance().getCPUManager().runMeasuredTask("processing PlayerToggleSneakEvent", TabFeature.NAMETAGX, UsageType.PLAYER_TOGGLE_SNEAK_EVENT, () -> {
			if (p.getArmorStandManager() != null) p.getArmorStandManager().sneak(e.isSneaking());
		});
	}
	
	/**
	 * Respawning armor stands as respawn screen destroys all entities in client
	 * @param e - respawn event
	 */
	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		TAB.getInstance().getCPUManager().runMeasuredTask("processing PlayerRespawnEvent", TabFeature.NAMETAGX, UsageType.PLAYER_RESPAWN_EVENT, () -> {
			TabPlayer respawned = TAB.getInstance().getPlayer(e.getPlayer().getUniqueId());
			if (feature.isInDisabledWorld(respawned)) return;
			respawned.getArmorStandManager().teleport();
		});
	}
}