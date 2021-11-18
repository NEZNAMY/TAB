package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

public class VehicleRefresher extends TabFeature {

	//list of players currently in a vehicle
	private Map<TabPlayer, Entity> playersInVehicle = new ConcurrentHashMap<>();
	
	//map of vehicles carrying players
	private Map<Integer, List<Entity>> vehicles = new ConcurrentHashMap<>();
	
	private NameTagX feature;
		
	public VehicleRefresher(NameTagX feature) {
		super(feature.getFeatureName());
		setRefreshDisplayName("Refreshing vehicles");
		this.feature = feature;
		addUsedPlaceholders(Arrays.asList("%vehicle%"));
		TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%vehicle%", 200, p -> String.valueOf(((Player)p.getPlayer()).getVehicle()));
	}
	
	@Override
	public void refresh(TabPlayer p, boolean force) {
		if (feature.isPlayerDisabled(p)) return;
		Entity vehicle = ((Player)p.getPlayer()).getVehicle();
		if (playersInVehicle.containsKey(p) && vehicle == null) {
			//vehicle exit
			vehicles.remove(playersInVehicle.get(p).getEntityId());
			p.getArmorStandManager().teleport();
			playersInVehicle.remove(p);
		}
		if (!playersInVehicle.containsKey(p) && vehicle != null) {
			//vehicle enter
			vehicles.put(vehicle.getEntityId(), getPassengers(vehicle));
			p.getArmorStandManager().teleport();
			playersInVehicle.put(p, vehicle);
		}
	}

	@Override
	public void onQuit(TabPlayer disconnectedPlayer) {
		playersInVehicle.remove(disconnectedPlayer);
	}
	
	public Map<Integer, List<Entity>> getVehicles() {
		return vehicles;
	}
	
	/**
	 * Returns list of all passengers on specified vehicle
	 * @param vehicle - vehicle to check passengers of
	 * @return list of passengers
	 */
	@SuppressWarnings("deprecation")
	public List<Entity> getPassengers(Entity vehicle){
		if (TAB.getInstance().getServerVersion().getMinorVersion() >= 11) {
			return vehicle.getPassengers();
		} else {
			if (vehicle.getPassenger() != null) {
				return Arrays.asList(vehicle.getPassenger());
			} else {
				return new ArrayList<>();
			}
		}
	}
	
	/**
	 * Loads all passengers riding this player and adds them to vehicle list
	 * @param p - player to load passengers of
	 */
	public void loadPassengers(TabPlayer p) {
		if (((Entity) p.getPlayer()).getVehicle() == null) return;
		Entity vehicle = ((Entity) p.getPlayer()).getVehicle();
		vehicles.put(vehicle.getEntityId(), getPassengers(vehicle));
	}
	
	/**
	 * Teleports armor stands of all passengers on specified vehicle
	 * @param vehicle - entity to check passengers of
	 */
	public void processPassengers(Entity vehicle) {
		for (Entity passenger : getPassengers(vehicle)) {
			if (passenger instanceof Player) {
				TAB.getInstance().getPlayer(passenger.getUniqueId()).getArmorStandManager().teleport();
			}
			processPassengers(passenger);
		}
	}

	public Map<TabPlayer, Entity> getPlayersInVehicle() {
		return playersInVehicle;
	}
}