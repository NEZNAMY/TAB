package me.neznamy.tab.platforms.bukkit.features;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import me.neznamy.tab.platforms.bukkit.Main;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.bossbar.BossBar;
import me.neznamy.tab.shared.features.bossbar.BossBarLine;
import me.neznamy.tab.shared.features.interfaces.Loadable;

public class BossBar_legacy implements Listener, Loadable {

	private final int WITHER_DISTANCE = 100;

	private BossBar mainFeature;
	
	public BossBar_legacy(BossBar mainFeature) {
		this.mainFeature = mainFeature;
	}
	@Override
	public void load() {
		Bukkit.getPluginManager().registerEvents(this, Main.instance);
		//bar disappears in client after ~1 second of not seeing boss entity
		Shared.featureCpu.startRepeatingMeasuredTask(900, "refreshing bossbar", CPUFeature.BOSSBAR_LEGACY, new Runnable() {
			public void run() {
				for (ITabPlayer all : Shared.getPlayers()) {
					for (BossBarLine l : all.activeBossBars) {
						all.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityTeleport(l.nmsEntity, getWitherLocation(all)));
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
			Shared.featureCpu.addTime(CPUFeature.BOSSBAR_LEGACY, System.nanoTime()-time);
		} catch (Throwable t) {
			Shared.errorManager.printError("An error occurred when processing PlayerRespawnEvent", t);
		}
	}
	public Location getWitherLocation(ITabPlayer p) {
		Location loc = p.getBukkitEntity().getEyeLocation().add(p.getBukkitEntity().getEyeLocation().getDirection().normalize().multiply(WITHER_DISTANCE));
		if (loc.getY() < 1) loc.setY(1);
		return loc;
	}
}