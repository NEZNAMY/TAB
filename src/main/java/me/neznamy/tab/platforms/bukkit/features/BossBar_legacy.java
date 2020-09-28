package me.neznamy.tab.platforms.bukkit.features;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityTeleport;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.bossbar.BossBar;
import me.neznamy.tab.shared.features.bossbar.BossBarLine;
import me.neznamy.tab.shared.features.interfaces.RespawnEventListener;

/**
 * An additional class with additional code for <1.9 servers due to an entity being required
 */
public class BossBar_legacy implements RespawnEventListener {

	//distance of wither in blocks
	private final int WITHER_DISTANCE = 75;
	
	//main bossbar feature
	private BossBar mainFeature;
	
	/**
	 * Constructs a new instance of the class
	 * @param mainFeature - main bossbar feature
	 */
	public BossBar_legacy(BossBar mainFeature) {
		this.mainFeature = mainFeature;
		//bar disappears in client after ~1 second of not seeing boss entity
		Shared.cpu.startRepeatingMeasuredTask(900, "refreshing bossbar", TabFeature.BOSSBAR, UsageType.TELEPORTING_ENTITY, new Runnable() {
			public void run() {
				for (TabPlayer all : Shared.getPlayers()) {
					for (BossBarLine l : all.getActiveBossBars()) {
						all.sendPacket(new PacketPlayOutEntityTeleport(l.entityId, getWitherLocation(all)));
					}
				}
			}
		});
	}
	
	/**
	 * Respawning all entities as respawn screen destroys all entities in client
	 */
	@Override
	public void onRespawn(TabPlayer respawned) {
		mainFeature.detectBossBarsAndSend(respawned);
	}
	
	/**
	 * Returns location where wither should be located based on where player is looking
	 * @param p - player to get wither location for
	 * @return location of wither
	 */
	public Location getWitherLocation(TabPlayer p) {
		Player pl = (Player) p.getPlayer();
		Location loc = pl.getEyeLocation().add(pl.getEyeLocation().getDirection().normalize().multiply(WITHER_DISTANCE));
		if (loc.getY() < 1) loc.setY(1);
		return loc;
	}
	
	/**
	 * Returns name of the feature displayed in /tab cpu
	 * @return name of the feature displayed in /tab cpu
	 */
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.BOSSBAR;
	}
}