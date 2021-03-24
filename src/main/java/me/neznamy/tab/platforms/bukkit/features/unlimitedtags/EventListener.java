package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;

/**
 * The event listener part for securing proper functionality of armor stands
 */
public class EventListener implements Listener {
	
	//the nametag feature handler
	private NameTagX feature;
	
	//list of players currently in a vehicle
	private Map<TabPlayer, Entity> playersInVehicle = new ConcurrentHashMap<TabPlayer, Entity>();
	
	private Map<TabPlayer, Location> playerLocations = new ConcurrentHashMap<TabPlayer, Location>();

	/**
	 * Constructs new instance with given parameters
	 * @param feature - nametagx feature handler
	 */
	public EventListener(NameTagX feature) {
		this.feature = feature;
		TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(50, "ticking unlimited nametag mode", TabFeature.NAMETAGX, UsageType.TICKING_UNLIMITED_NAMETAGS, () -> {
			
			for (TabPlayer p : TAB.getInstance().getPlayers()) {
				if (!p.isLoaded()) continue;
				processVehicles(p);
				if (feature.isDisabledWorld(p.getWorldName())) {
					playerLocations.remove(p);
					continue;
				}
				if (!playerLocations.containsKey(p) || !playerLocations.get(p).equals(((Player)p.getPlayer()).getLocation())) {
					playerLocations.put(p, ((Player)p.getPlayer()).getLocation());
					processMove(p, ((Player)p.getPlayer()).getLocation());
				}
				//vanishing
				Set<TabPlayer> nearby = p.getArmorStandManager().getNearbyPlayers();
				for (TabPlayer other : TAB.getInstance().getPlayers()) {
					if (nearby.contains(other) && !((Player)other.getPlayer()).canSee((Player)p.getPlayer())) {
						p.getArmorStandManager().destroy(other);
					}
					if (!nearby.contains(other) && ((Player)other.getPlayer()).canSee((Player)p.getPlayer())) {
						p.getArmorStandManager().spawn(other);
					}
				}
			}
		});
	}
	
	/**
	 * Checks for vehicle changes of player and sends packets if needed
	 * @param p - player to check
	 */
	private void processVehicles(TabPlayer p) {
		if (feature.isDisabledWorld(p.getWorldName())) {
			playersInVehicle.remove(p);
			return;
		}
		Entity vehicle = ((Player)p.getPlayer()).getVehicle();
		if (playersInVehicle.containsKey(p) && vehicle == null) {
			//vehicle exit
			feature.vehicles.remove(playersInVehicle.get(p).getEntityId());
			p.getArmorStandManager().teleport();
			playersInVehicle.remove(p);
		}
		if (!playersInVehicle.containsKey(p) && vehicle != null) {
			//vehicle enter
			feature.vehicles.put(vehicle.getEntityId(), feature.getPassengers(vehicle));
			p.getArmorStandManager().teleport();
			playersInVehicle.put(p, vehicle);
		}
	}
	
	/**
	 * Teleport event listener to track vehicles & send own packets when using nametag preview
	 * @param e - teleport event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onTeleport(PlayerTeleportEvent e) {
		TabPlayer p = TAB.getInstance().getPlayer(e.getPlayer().getUniqueId());
		if (p == null || feature.isDisabledWorld(p.getWorldName())) return;
		TAB.getInstance().getCPUManager().runTaskLater(100, "processing PlayerTeleportEvent", TabFeature.NAMETAGX, UsageType.PLAYER_TELEPORT_EVENT, () -> p.getArmorStandManager().teleport());
	}
	
	/**
	 * Death event listener to destroy armor stands for players in range of dead player
	 * @param e - death event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDeath(PlayerDeathEvent e) {
		TAB.getInstance().getCPUManager().runMeasuredTask("processing PlayerDeathEvent", TabFeature.NAMETAGX, UsageType.PLAYER_DEATH_EVENT, () -> {
			
			TabPlayer player = TAB.getInstance().getPlayer(e.getEntity().getUniqueId());
			for (TabPlayer other : player.getArmorStandManager().getNearbyPlayers()) {
				player.getArmorStandManager().destroy(other);
				other.getArmorStandManager().destroy(player);
			}
		});
	}
	
	//preventing memory leak
	public void onQuit(TabPlayer p) {
		playersInVehicle.remove(p);
		playerLocations.remove(p);
	}
	
	/**
	 * Checks player for entity tracking range, processes packets of passengers and sends own
	 * armor stand move if preview is used
	 * @param player - player to process move of
	 * @param newLocation - location player is moving/teleporting to
	 */
	private void processMove(TabPlayer player, Location newLocation) {
		checkForTrackingRange(player, newLocation);
		processPassengers((Entity) player.getPlayer());
		if (player.isPreviewingNametag() && player.getArmorStandManager() != null) {
			player.getArmorStandManager().teleport(player);
		}
	}
	
	/**
	 * Checks for tracking range and spawns/despawns armor stands if needed
	 * @param player - player to check tracking range of
	 * @param newLocation - location player moved/teleported to
	 */
	private void checkForTrackingRange(TabPlayer player, Location newLocation) {
		for (TabPlayer other : TAB.getInstance().getPlayers()) {
			if (other == player || !other.getWorldName().equals(player.getWorldName()) || !other.isLoaded()) continue;
			if (getFlatDistance(((Player)other.getPlayer()).getLocation(), newLocation) < feature.entityTrackingRange &&
					!((Player)player.getPlayer()).isDead() && !((Player)other.getPlayer()).isDead()) {
				//in range
				if (!player.getArmorStandManager().getNearbyPlayers().contains(other) && ((Player)other.getPlayer()).canSee((Player)player.getPlayer())) {
					player.getArmorStandManager().spawn(other);
				}
				if (!other.getArmorStandManager().getNearbyPlayers().contains(player) && ((Player)player.getPlayer()).canSee((Player)other.getPlayer())) {
					other.getArmorStandManager().spawn(player);
				}
			} else {
				//out of range
				if (player.getArmorStandManager().getNearbyPlayers().contains(other)) {
					player.getArmorStandManager().destroy(other);
				}
				if (other.getArmorStandManager().getNearbyPlayers().contains(player)) {
					other.getArmorStandManager().destroy(player);
				}
			}
		}
	}
	
	/**
	 * Returns flat distance between two locations ignoring Y value
	 * @param loc1 - first location
	 * @param loc2 - second location
	 * @return distance in blocks
	 */
	private double getFlatDistance(Location loc1, Location loc2) {
		return Math.sqrt(Math.pow(loc1.getX()-loc2.getX(), 2) + Math.pow(loc1.getZ()-loc2.getZ(), 2));
	}
	
	/**
	 * Teleports armor stands of all passengers on specified vehicle
	 * @param vehicle - entity to check passengers of
	 */
	private void processPassengers(Entity vehicle) {
		for (Entity passenger : feature.getPassengers(vehicle)) {
			if (passenger instanceof Player) {
				TabPlayer pl = TAB.getInstance().getPlayer(passenger.getUniqueId());
				pl.getArmorStandManager().teleport();
			} else {
				processPassengers(passenger);
			}
		}
	}
}