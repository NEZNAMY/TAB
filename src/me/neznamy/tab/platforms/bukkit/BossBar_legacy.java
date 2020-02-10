package me.neznamy.tab.platforms.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.BossBar.BossBarLine;
import me.neznamy.tab.shared.BossBar;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;

public class BossBar_legacy implements Listener {

	private static boolean EVENTS_REGISTERED = false;
	
	public static void load() {
		if (BossBar.enabled && ProtocolVersion.SERVER_VERSION.getMinorVersion() < 9) {
			if (!EVENTS_REGISTERED) {
				EVENTS_REGISTERED = true;
				Bukkit.getPluginManager().registerEvents(new BossBar_legacy(), Main.instance);
			}
			Shared.cpu.startRepeatingMeasuredTask(200, "refreshing bossbar", "BossBar 1.8", new Runnable() {
				public void run() {
					for (ITabPlayer all : Shared.getPlayers()) {
						for (BossBarLine l : all.activeBossBars) {
							Location to = (((TabPlayer)all).player).getEyeLocation().add((((TabPlayer)all).player).getEyeLocation().getDirection().normalize().multiply(25));
							all.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityTeleport(l.getEntity(), to));
						}
					}
				}
			});
		}
	}
	@EventHandler
	public void a(PlayerChangedWorldEvent e) {
		if (!BossBar.enabled) return;
		long time = System.nanoTime();
		ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null) return; //teleport in early login event or what
		Shared.getPlayer(e.getPlayer().getUniqueId()).detectBossBarsAndSend();
		Shared.cpu.addFeatureTime("BossBar 1.8", System.nanoTime()-time);
	}
	@EventHandler
	public void a(PlayerRespawnEvent e) {
		if (!BossBar.enabled) return;
		long time = System.nanoTime();
		Shared.getPlayer(e.getPlayer().getUniqueId()).detectBossBarsAndSend();
		Shared.cpu.addFeatureTime("BossBar 1.8", System.nanoTime()-time);
	}
}