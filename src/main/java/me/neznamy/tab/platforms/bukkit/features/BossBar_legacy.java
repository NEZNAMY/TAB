package me.neznamy.tab.platforms.bukkit.features;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityTeleport;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.bossbar.BossBar;
import me.neznamy.tab.shared.features.bossbar.BossBarLine;
import me.neznamy.tab.shared.features.interfaces.Loadable;

/**
 * An additional class with additional code for <1.9 servers due to an entity being required
 */
public class BossBar_legacy implements Listener, Loadable {

	private final int WITHER_DISTANCE = 75;
	private BossBar mainFeature;
	private JavaPlugin plugin;
	
	public BossBar_legacy(BossBar mainFeature, JavaPlugin plugin) {
		this.mainFeature = mainFeature;
		this.plugin = plugin;
	}
	@Override
	public void load() {
		Bukkit.getPluginManager().registerEvents(this, plugin);
		//bar disappears in client after ~1 second of not seeing boss entity
		Shared.cpu.startRepeatingMeasuredTask(900, "refreshing bossbar", TabFeature.BOSSBAR, UsageType.REFRESHING, new Runnable() {
			public void run() {
				for (ITabPlayer all : Shared.getPlayers()) {
					for (BossBarLine l : all.activeBossBars) {
						all.sendPacket(new PacketPlayOutEntityTeleport(l.entityId, getWitherLocation(all)));
					}
				}
			}
		});
	}
	@Override
	public void unload() {
		HandlerList.unregisterAll(this);
	}
	@EventHandler
	public void a(PlayerRespawnEvent e) {
		try {
			long time = System.nanoTime();
			ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
			if (p != null) mainFeature.detectBossBarsAndSend(p);
			Shared.cpu.addTime(TabFeature.BOSSBAR, UsageType.PLAYER_RESPAWN_EVENT, System.nanoTime()-time);
		} catch (Throwable t) {
			Shared.errorManager.printError("An error occurred when processing PlayerRespawnEvent", t);
		}
	}
	public Location getWitherLocation(ITabPlayer p) {
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