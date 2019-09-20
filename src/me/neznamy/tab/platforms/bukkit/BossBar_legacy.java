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
import me.neznamy.tab.shared.Shared.Feature;

public class BossBar_legacy implements Listener {

	public static void load() {
		if (BossBar.enabled && ProtocolVersion.SERVER_VERSION.getMinorVersion() < 9) {
			Bukkit.getPluginManager().registerEvents(new BossBar_legacy(), Main.instance);
			Shared.scheduleRepeatingTask(200, "refreshing bossbar", Feature.BOSSBAR, new Runnable() {
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
		Shared.cpu(Feature.BOSSBAR, System.nanoTime()-time);
	}
	@EventHandler
	public void a(PlayerRespawnEvent e) {
		if (!BossBar.enabled) return;
		long time = System.nanoTime();
		Shared.getPlayer(e.getPlayer().getUniqueId()).detectBossBarsAndSend();
		Shared.cpu(Feature.BOSSBAR, System.nanoTime()-time);
	}
}