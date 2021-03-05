package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

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

	private final int ENTITY_TRACKING_RANGE = 48;
	
	//the nametag feature handler
	private NameTagX feature;

	/**
	 * Constructs new instance with given parameters
	 * @param feature - nametagx feature handler
	 */
	public EventListener(NameTagX feature) {
		this.feature = feature;
	}

	/**
	 * Move event listener to send packets when /tab ntpreview is used
	 * @param e - move event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(PlayerMoveEvent e) {
		if (e.getFrom().distance(e.getTo()) == 0) return;
		TabPlayer p = TAB.getInstance().getPlayer(e.getPlayer().getUniqueId());
		if (p == null) return;
		TAB.getInstance().getCPUManager().runMeasuredTask("processing PlayerMoveEvent", TabFeature.NAMETAGX, UsageType.PLAYER_MOVE_EVENT, () -> processMove(p, e.getTo()));
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(PlayerTeleportEvent e) {
		TabPlayer p = TAB.getInstance().getPlayer(e.getPlayer().getUniqueId());
		if (p == null) return;
		TAB.getInstance().getCPUManager().runTaskLater(100, "processing PlayerTeleportEvent", TabFeature.NAMETAGX, UsageType.PLAYER_TELEPORT_EVENT, () -> processMove(p, e.getTo()));
	}
	
	private void processMove(TabPlayer player, Location newLocation) {
		checkForTrackingRange(player, newLocation);
		processPassengers((Entity) player.getPlayer());
		if (player.isPreviewingNametag()) {
			player.getArmorStandManager().teleport(player);
		}
	}
	
	private void checkForTrackingRange(TabPlayer player, Location newLocation) {
		for (TabPlayer other : TAB.getInstance().getPlayers()) {
			if (other == player || !other.getWorldName().equals(player.getWorldName())) continue;
			if (getFlatDistance(((Player)other.getPlayer()).getLocation(), newLocation) < ENTITY_TRACKING_RANGE) {
				//in range
				if (!player.getArmorStandManager().getNearbyPlayers().contains(other)) {
					player.getArmorStandManager().spawn(other);
				}
				if (!other.getArmorStandManager().getNearbyPlayers().contains(player)) {
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
	
	private double getFlatDistance(Location loc1, Location loc2) {
		return Math.sqrt(Math.pow(loc1.getX()-loc2.getX(), 2) + Math.pow(loc1.getZ()-loc2.getZ(), 2));
	}
	
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