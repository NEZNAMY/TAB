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
import me.neznamy.tab.shared.features.BossBar.BossBarLine;
import me.neznamy.tab.shared.features.SimpleFeature;

public class BossBar_legacy implements Listener, SimpleFeature {

	private static final int WITHER_DISTANCE = 50;

	@Override
	public void load() {
		Bukkit.getPluginManager().registerEvents(this, Main.instance);
		Shared.featureCpu.startRepeatingMeasuredTask(200, "refreshing bossbar", CPUFeature.BOSSBAR_LEGACY, new Runnable() {
			public void run() {
				for (ITabPlayer all : Shared.getPlayers()) {
					for (BossBarLine l : all.activeBossBars) {
						all.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityTeleport(l.getEntity(), getWitherLocation(all)));
					}
				}
			}
		});
	}
	@Override
	public void unload() {
		HandlerList.unregisterAll(this);
	}
	@Override
	public void onJoin(ITabPlayer connectedPlayer) {
	}
	@Override
	public void onQuit(ITabPlayer disconnectedPlayer) {
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		p.detectBossBarsAndSend();
	}
	@EventHandler
	public void a(PlayerRespawnEvent e) {
		try {
			long time = System.nanoTime();
			ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
			if (p != null) p.detectBossBarsAndSend();
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