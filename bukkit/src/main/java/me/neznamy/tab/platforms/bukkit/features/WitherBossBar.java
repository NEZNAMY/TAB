package me.neznamy.tab.platforms.bukkit.features;

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
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;

/**
 * An additional class with additional code for <1.9 servers due to an entity being required
 */
public class WitherBossBar extends BossBarManagerImpl implements Listener {

	//distance of wither in blocks
	private static final int WITHER_DISTANCE = 75;
	
	/**
	 * Constructs a new instance of the class
	 * @param mainFeature - main bossbar feature
	 */
	public WitherBossBar(JavaPlugin plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
		//bar disappears in client after ~1 second of not seeing boss entity
		TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(900, "refreshing bossbar", this, "Teleporting Wither entity", this::teleport);
	}
	
	@Override
	public void load() {
		teleport();
	}
	
	/**
	 * Updates wither location for all players
	 */
	private void teleport() {
		for (BossBar line : getRegisteredBossBars().values()) {
			for (TabPlayer all : line.getPlayers()) {
				if (all.getVersion().getMinorVersion() > 8) continue; //sending VV packets to those
				all.sendCustomPacket(new PacketPlayOutEntityTeleport(line.getUniqueId().hashCode(), getWitherLocation(all)), "BossBar - Teleporting entity");
			}
		}
	}

	@Override
	public void unload() {
		HandlerList.unregisterAll(this);
	}
	
	/**
	 * Respawning wither as respawn screen destroys all entities in client
	 */
	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		TAB.getInstance().getCPUManager().runMeasuredTask("processing PlayerRespawnEvent", this, "PlayerRespawnEvent", () -> detectBossBarsAndSend(TAB.getInstance().getPlayer(e.getPlayer().getUniqueId())));
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
}