package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import me.neznamy.tab.shared.TabConstants;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

public class VehicleRefresher extends TabFeature {

	//map of players currently in a vehicle
	private final WeakHashMap<TabPlayer, Entity> playersInVehicle = new WeakHashMap<>();
	
	//map of vehicles carrying players
	private final Map<Integer, List<Entity>> vehicles = new ConcurrentHashMap<>();
	
	//set of players currently on boats
	private final Set<TabPlayer> playersOnBoats = Collections.newSetFromMap(new WeakHashMap<>());
	
	private final NameTagX feature;
		
	public VehicleRefresher(NameTagX feature) {
		super(feature.getFeatureName(), "Refreshing vehicles");
		this.feature = feature;
		TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(50, "processing player movement",
				this, TabConstants.CpuUsageCategory.PROCESSING_PLAYER_MOVEMENT, () -> {
					for (TabPlayer inVehicle : playersInVehicle.keySet()) {
						inVehicle.getArmorStandManager().teleport();
//						feature.getVehicleManager().processPassengers((Entity) inVehicle.getPlayer());
					}
					for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
						if (p.isPreviewingNametag()) {
							p.getArmorStandManager().teleport(p);
						}
					}
		});
		addUsedPlaceholders(Collections.singletonList("%vehicle%"));
		TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%vehicle%", 100, p -> ((Player)p.getPlayer()).getVehicle() == null ? "" : ((Player)p.getPlayer()).getVehicle());
	}

	@Override
	public void load() {
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
			Entity vehicle = ((Player)p.getPlayer()).getVehicle();
			if (vehicle != null) {
				vehicles.put(vehicle.getEntityId(), getPassengers(vehicle));
				playersInVehicle.put(p, vehicle);
				if (feature.isDisableOnBoats() && vehicle.getType() == EntityType.BOAT) {
					playersOnBoats.add(p);
				}
			}
		}
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
			if (feature.isDisableOnBoats() && playersOnBoats.contains(p)) {
				playersOnBoats.remove(p);
				feature.updateTeamData(p);
			}
		}
		if (!playersInVehicle.containsKey(p) && vehicle != null) {
			//vehicle enter
			vehicles.put(vehicle.getEntityId(), getPassengers(vehicle));
			p.getArmorStandManager().respawn(); //making teleport instant instead of showing teleport animation
			playersInVehicle.put(p, vehicle);
			if (feature.isDisableOnBoats() && vehicle.getType() == EntityType.BOAT) {
				playersOnBoats.add(p);
				feature.updateTeamData(p);
			}
		}
	}

	public boolean isOnBoat(TabPlayer p) {
		return playersOnBoats.contains(p);
	}

	@Override
	public void onQuit(TabPlayer disconnectedPlayer) {
		if (playersInVehicle.containsKey(disconnectedPlayer)) vehicles.remove(playersInVehicle.get(disconnectedPlayer).getEntityId());
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
				return Collections.singletonList(vehicle.getPassenger());
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
		Entity vehicle = ((Entity) p.getPlayer()).getVehicle();
		if (vehicle == null) return;
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
}