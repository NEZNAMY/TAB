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

	private final int WITHER_DISTANCE = 75;
	private BossBar mainFeature;
	
	public BossBar_legacy(BossBar mainFeature) {
		this.mainFeature = mainFeature;
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
	
	@Override
	public void onRespawn(TabPlayer respawned) {
		mainFeature.detectBossBarsAndSend(respawned);
	}
	
	public Location getWitherLocation(TabPlayer p) {
		Player pl = (Player) p.getPlayer();
		Location loc = pl.getEyeLocation().add(pl.getEyeLocation().getDirection().normalize().multiply(WITHER_DISTANCE));
		if (loc.getY() < 1) loc.setY(1);
		return loc;
	}
	
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.BOSSBAR;
	}
}