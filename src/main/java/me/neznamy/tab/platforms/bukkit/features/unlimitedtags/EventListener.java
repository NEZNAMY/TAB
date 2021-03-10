package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;

/**
 * The event listener part for securing proper functionality of armor stands
 */
public class EventListener implements Listener {

	//entity tracking range
	private final int ENTITY_TRACKING_RANGE = 48;
	
	//the nametag feature handler
	private NameTagX feature;
	
	//list of players currently in a vehicle
	private Map<TabPlayer, Entity> playersInVehicle = new ConcurrentHashMap<TabPlayer, Entity>();

	/**
	 * Constructs new instance with given parameters
	 * @param feature - nametagx feature handler
	 */
	public EventListener(NameTagX feature) {
		this.feature = feature;
		
		//tracking vehicle enter & exit, plugins allowing to ride players/entities do not call the events so can't use those
		TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(50, "tracking vehicles", TabFeature.NAMETAGX, UsageType.TRACKING_VEHICLES, () -> {
			
			for (TabPlayer p : TAB.getInstance().getPlayers()) {
				if (feature.isDisabledWorld(p.getWorldName())) {
					playersInVehicle.remove(p);
					continue;
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
		});
	}

	/**
	 * Move event listener to track vehicles & send own packets when using nametag preview
	 * @param e - move event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMove(PlayerMoveEvent e) {
		if (e.getFrom().distance(e.getTo()) == 0) return;
		TabPlayer p = TAB.getInstance().getPlayer(e.getPlayer().getUniqueId());
		if (p == null || feature.isDisabledWorld(p.getWorldName())) return;
		TAB.getInstance().getCPUManager().runMeasuredTask("processing PlayerMoveEvent", TabFeature.NAMETAGX, UsageType.PLAYER_MOVE_EVENT, () -> processMove(p, e.getTo()));
	}
	
	/**
	 * Teleport event listener to track vehicles & send own packets when using nametag preview
	 * @param e - teleport event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onTeleport(PlayerTeleportEvent e) {
		TabPlayer p = TAB.getInstance().getPlayer(e.getPlayer().getUniqueId());
		if (p == null || feature.isDisabledWorld(p.getWorldName())) return;
		TAB.getInstance().getCPUManager().runTaskLater(100, "processing PlayerTeleportEvent", TabFeature.NAMETAGX, UsageType.PLAYER_TELEPORT_EVENT, () -> processMove(p, e.getTo()));
	}
	
	//preventing memory leak
	public void onQuit(TabPlayer p) {
		playersInVehicle.remove(p);
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
			if (getFlatDistance(((Player)other.getPlayer()).getLocation(), newLocation) < ENTITY_TRACKING_RANGE) {
				//in range
				if (!player.getArmorStandManager().getNearbyPlayers().contains(other) && ((Player)other.getPlayer()).canSee((Player)player.getPlayer())) {
					player.getArmorStandManager().spawn(other);
				}
				if (!other.getArmorStandManager().getNearbyPlayers().contains(player) && ((Player)player.getPlayer()).canSee((Player)other.getPlayer())) {
					other.getArmorStandManager().spawn(player);
				}
			} else {
				//out of range
				if (player.getArmorStandManager().getNearbyPlayers().contains(other) && !((Player)other.getPlayer()).canSee((Player)player.getPlayer())) {
					player.getArmorStandManager().destroy(other);
				}
				if (other.getArmorStandManager().getNearbyPlayers().contains(player)&& !((Player)player.getPlayer()).canSee((Player)other.getPlayer())) {
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