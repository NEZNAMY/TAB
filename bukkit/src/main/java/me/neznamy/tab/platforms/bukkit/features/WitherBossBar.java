package me.neznamy.tab.platforms.bukkit.features;

import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityTeleport;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;

/**
 * An additional class with additional code for <1.9 servers due to an entity being required
 */
public class WitherBossBar extends BossBarManagerImpl implements Listener {

	//distance of wither in blocks
	private static final int WITHER_DISTANCE = 60;
	
	/**
	 * Constructs a new instance of the class
	 * @param plugin - plugin instance
	 */
	public WitherBossBar(JavaPlugin plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
		//when MC is on fullscreen, BossBar disappears after 1 second of not being seen
		//when in a small window, it's about 100ms
		TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%location%", 100, p -> ((Player)p.getPlayer()).getLocation());
		addUsedPlaceholders(Collections.singletonList("%location%"));
	}
	
	@Override
	public void load() {
		super.load();
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
			refresh(p, false);
		}
	}
	
	@Override
	public void refresh(TabPlayer p, boolean force) {
		for (BossBar line : getRegisteredBossBars().values()) {
			if (p.getVersion().getMinorVersion() > 8) continue; //sending VV packets to those
			Location loc = ((Player) p.getPlayer()).getEyeLocation().add(((Player) p.getPlayer()).getEyeLocation().getDirection().normalize().multiply(WITHER_DISTANCE));
			if (loc.getY() < 1) loc.setY(1);
			p.sendCustomPacket(new PacketPlayOutEntityTeleport(line.getUniqueId().hashCode(), loc), TabConstants.PacketCategory.BOSSBAR_WITHER_TELEPORT);
		}
	}
	
	@Override
	public void unload() {
		super.unload();
		HandlerList.unregisterAll(this);
	}
	
	/**
	 * Respawning wither as respawn screen destroys all entities in client
	 */
	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		TAB.getInstance().getCPUManager().runMeasuredTask("processing PlayerRespawnEvent", this, TabConstants.CpuUsageCategory.PLAYER_RESPAWN, () -> detectBossBarsAndSend(TAB.getInstance().getPlayer(e.getPlayer().getUniqueId())));
	}
}