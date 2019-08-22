package me.neznamy.tab.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import me.neznamy.tab.bukkit.unlimitedtags.PacketPlayOutEntityTeleport;
import me.neznamy.tab.shared.BossBar;
import me.neznamy.tab.shared.BossBar.BossBarLine;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.Shared.Feature;

public class BossBar1_8 implements Listener {

	public static void load() {
		if (!BossBar.enable) return;
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() == 8) {
			Bukkit.getPluginManager().registerEvents(new BossBar1_8(), Main.instance);
			Shared.scheduleRepeatingTask(500, "refreshing bossbar", Feature.BOSSBAR, new Runnable() {

				
				public void run() {
					for (BossBarLine l : BossBar.lines) {
						for (ITabPlayer all : Shared.getPlayers()) {
							Location to = ((Player) all.getPlayer()).getEyeLocation().add(((Player) all.getPlayer()).getEyeLocation().getDirection().normalize().multiply(25));
							new PacketPlayOutEntityTeleport(l.getBossBar().getEntityId(), to).send(all);
						};
					}
				}
			});
		}
	}
	@EventHandler
	public void a(PlayerChangedWorldEvent e) {
		if (!BossBar.enable) return;
		long time = System.nanoTime();
		ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null) return;
		for (BossBarLine line : BossBar.lines) BossBar.sendBar(p, line);
		Shared.cpu(Feature.BOSSBAR, System.nanoTime()-time);
	}
	@EventHandler
	public void a(PlayerRespawnEvent e) {
		if (!BossBar.enable) return;
		long time = System.nanoTime();
		for (BossBarLine line : BossBar.lines) BossBar.sendBar(Shared.getPlayer(e.getPlayer().getUniqueId()), line);
		Shared.cpu(Feature.BOSSBAR, System.nanoTime()-time);
	}
}