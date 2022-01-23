package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;

/**
 * The event listener part for securing proper functionality of armor stands
 */
public class EventListener implements Listener {
	
	//the NameTag feature handler
	private final NameTagX feature;

	/**
	 * Constructs new instance with given parameters
	 * @param feature - NameTag feature handler
	 */
	public EventListener(NameTagX feature) {
		this.feature = feature;
	}
	
	/**
	 * Sneak event listener to de-spawn & spawn armor stands to skip animation
	 * @param e - sneak event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSneak(PlayerToggleSneakEvent e) {
		TabPlayer p = TAB.getInstance().getPlayer(e.getPlayer().getUniqueId());
		if (p == null || feature.isPlayerDisabled(p)) return;
		TAB.getInstance().getCPUManager().runMeasuredTask("processing PlayerToggleSneakEvent", feature, TabConstants.CpuUsageCategory.PLAYER_SNEAK, () -> {
			if (p.getArmorStandManager() != null) p.getArmorStandManager().sneak(e.isSneaking());
		});
	}
	
	/**
	 * Respawning armor stands as respawn screen destroys all entities in client
	 * @param e - respawn event
	 */
	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		TAB.getInstance().getCPUManager().runMeasuredTask("processing PlayerRespawnEvent", feature, TabConstants.CpuUsageCategory.PLAYER_RESPAWN, () -> {
			TabPlayer respawned = TAB.getInstance().getPlayer(e.getPlayer().getUniqueId());
			if (feature.isPlayerDisabled(respawned)) return;
			respawned.getArmorStandManager().teleport();
		});
	}
	
	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent e) {
		TabPlayer p = TAB.getInstance().getPlayer(e.getPlayer().getUniqueId());
		if (p == null || !p.isLoaded()) return;
		long time = System.nanoTime();
		String to = e.getPlayer().getWorld().getName();
		if (feature.isDisabled(to)) {
			feature.getPlayersInDisabledUnlimitedWorlds().add(p);
		} else {
			feature.getPlayersInDisabledUnlimitedWorlds().remove(p);
		}
		//TODO delete the block below
		TabPlayer[] nearby = p.getArmorStandManager().getNearbyPlayers();
		p.getArmorStandManager().destroy();
		feature.loadArmorStands(p);
		feature.getVehicleManager().loadPassengers(p);
		for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
			if (viewer.getArmorStandManager() != null) viewer.getArmorStandManager().destroy(p);
			if (!to.equals(viewer.getWorld())) continue;
			for (TabPlayer player : nearby) {
				if (player == viewer) feature.spawnArmorStands(p, viewer, true);
			}
		}
		TAB.getInstance().getCPUManager().addTime(feature, TabConstants.CpuUsageCategory.WORLD_SWITCH, System.nanoTime()-time);
	}
}