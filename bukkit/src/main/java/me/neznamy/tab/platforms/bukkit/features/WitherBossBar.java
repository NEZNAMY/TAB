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
import me.neznamy.tab.platforms.bukkit.BukkitPacketBuilder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.bossbar.BossBar;
import me.neznamy.tab.shared.features.types.Loadable;

/**
 * An additional class with additional code for <1.9 servers due to an entity being required
 */
public class WitherBossBar implements Listener, Loadable {

	//distance of wither in blocks
	private static final int WITHER_DISTANCE = 75;
	
	//main bossbar feature
	private BossBar mainFeature;
	
	/**
	 * Constructs a new instance of the class
	 * @param mainFeature - main bossbar feature
	 */
	public WitherBossBar(TAB tab, JavaPlugin plugin) {
		this.mainFeature = (BossBar) tab.getFeatureManager().getFeature("bossbar");
		Bukkit.getPluginManager().registerEvents(this, plugin);
		//bar disappears in client after ~1 second of not seeing boss entity
		tab.getCPUManager().startRepeatingMeasuredTask(900, "refreshing bossbar", TabFeature.BOSSBAR, UsageType.TELEPORTING_ENTITY, this::teleport);
	}
	
	@Override
	public void load() {
		teleport();
	}
	
	/**
	 * Updates wither location for all players
	 */
	private void teleport() {
		for (TabPlayer all : TAB.getInstance().getPlayers()) {
			if (all.getVersion().getMinorVersion() > 8) continue; //sending VV packets to those
			for (me.neznamy.tab.api.bossbar.BossBar l : all.getActiveBossBars()) {
				try {
					all.sendPacket(((BukkitPacketBuilder)TAB.getInstance().getPacketBuilder()).buildEntityTeleportPacket(l.getUniqueId().hashCode(), getWitherLocation(all)), TabFeature.BOSSBAR);
				} catch (Exception e) {
					TAB.getInstance().getErrorManager().printError("Failed to create PacketPlayOutEntityTeleport", e);
				}
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
		TAB.getInstance().getCPUManager().runMeasuredTask("processing PlayerRespawnEvent", TabFeature.BOSSBAR, UsageType.PLAYER_RESPAWN_EVENT, () -> mainFeature.detectBossBarsAndSend(TAB.getInstance().getPlayer(e.getPlayer().getUniqueId())));
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