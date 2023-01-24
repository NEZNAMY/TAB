package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sub-feature for unlimited name tag mode to secure
 * proper functionality when using vehicles.
 * A config option allows to disable the feature on boats,
 * where it would cause a massive de-sync due to boat movement
 * animation.
 * Additionally, when entering a vehicle, no move packet is sent
 * and therefore manual teleporting of armor stands is required.
 */
public class VehicleRefresher extends TabFeature {

    /** Map of players currently in a vehicle */
    private final WeakHashMap<TabPlayer, Entity> playersInVehicle = new WeakHashMap<>();
    
    /** Map of vehicles carrying players */
    private final Map<Integer, List<Entity>> vehicles = new ConcurrentHashMap<>();
    
    /** set of players currently on boats */
    private final Set<TabPlayer> playersOnBoats = Collections.newSetFromMap(new WeakHashMap<>());

    /** Reference to the main feature */
    private final BukkitNameTagX feature;

    /**
     * Constructs new instance with given parameter and starts tasks.
     *
     * @param   feature
     *          Main feature
     */
    public VehicleRefresher(BukkitNameTagX feature) {
        super(feature.getFeatureName(), "Refreshing vehicles");
        this.feature = feature;
        TabAPI.getInstance().getThreadManager().startRepeatingMeasuredTask(50,
                this, TabConstants.CpuUsageCategory.PROCESSING_PLAYER_MOVEMENT, () -> {
                    for (TabPlayer inVehicle : playersInVehicle.keySet()) {
                        if (!inVehicle.isOnline() || feature.getArmorStandManager(inVehicle) == null) continue; // not removed from WeakHashMap yet
                        feature.getArmorStandManager(inVehicle).teleport();
                    }
                    for (TabPlayer p : TabAPI.getInstance().getOnlinePlayers()) {
                        if (feature.isPreviewingNametag(p)) {
                            feature.getArmorStandManager(p).teleport(p);
                        }
                    }
        });
        addUsedPlaceholders(Collections.singletonList(TabConstants.Placeholder.VEHICLE));
        TabAPI.getInstance().getPlaceholderManager().registerPlayerPlaceholder(TabConstants.Placeholder.VEHICLE, 100, p -> {
            Entity v = ((Player)p.getPlayer()).getVehicle();
            //There's a bug in Bukkit 1.19.3 throwing NPE on .toString(), use default toString implementation
            return v == null ? "" : v.getClass().getName() + "@" + Integer.toHexString(v.hashCode());
        });
    }

    @Override
    public void load() {
        for (TabPlayer p : TabAPI.getInstance().getOnlinePlayers()) {
            Entity vehicle = ((Player)p.getPlayer()).getVehicle();
            if (vehicle != null) {
                vehicles.put(vehicle.getEntityId(), getPassengers(vehicle));
                playersInVehicle.put(p, vehicle);
                if (feature.isDisableOnBoats() && vehicle.getType().toString().contains("BOAT")) {
                    playersOnBoats.add(p);
                }
            }
        }
    }

    @Override
    public void onJoin(TabPlayer connectedPlayer) {
        Entity vehicle = ((Entity) connectedPlayer.getPlayer()).getVehicle();
        if (vehicle != null) vehicles.put(vehicle.getEntityId(), getPassengers(vehicle));
    }

    @Override
    public void onQuit(TabPlayer disconnectedPlayer) {
        if (playersInVehicle.containsKey(disconnectedPlayer)) vehicles.remove(playersInVehicle.get(disconnectedPlayer).getEntityId());
        for (List<Entity> entities : vehicles.values()) {
            entities.remove((Player) disconnectedPlayer.getPlayer());
        }
    }

    @Override
    public void refresh(TabPlayer p, boolean force) {
        if (feature.isPlayerDisabled(p)) return;
        Entity vehicle = ((Player)p.getPlayer()).getVehicle();
        if (playersInVehicle.containsKey(p) && vehicle == null) {
            //vehicle exit
            vehicles.remove(playersInVehicle.get(p).getEntityId());
            feature.getArmorStandManager(p).teleport();
            playersInVehicle.remove(p);
            if (feature.isDisableOnBoats() && playersOnBoats.contains(p)) {
                playersOnBoats.remove(p);
                feature.updateTeamData(p);
            }
        }
        if (!playersInVehicle.containsKey(p) && vehicle != null) {
            //vehicle enter
            vehicles.put(vehicle.getEntityId(), getPassengers(vehicle));
            feature.getArmorStandManager(p).respawn(); //making teleport instant instead of showing teleport animation
            playersInVehicle.put(p, vehicle);
            if (feature.isDisableOnBoats() && vehicle.getType().toString().contains("BOAT")) {
                playersOnBoats.add(p);
                feature.updateTeamData(p);
            }
        }
    }

    /**
     * Returns {@code true} if the player is in a boat, {@code false} if not
     *
     * @param   p
     *          Player to check
     * @return  {@code true} if in a boat, {@code false} if not
     */
    public boolean isOnBoat(TabPlayer p) {
        return playersOnBoats.contains(p);
    }

    /**
     * Returns vehicle map
     *
     * @return  vehicle map
     */
    public Map<Integer, List<Entity>> getVehicles() {
        return vehicles;
    }
    
    /**
     * Returns list of all passengers on specified vehicle
     *
     * @param   vehicle
     *          vehicle to check passengers of
     * @return  list of passengers
     */
    @SuppressWarnings("deprecation")
    public List<Entity> getPassengers(Entity vehicle){
        if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 11) {
            return vehicle.getPassengers();
        } else {
            if (vehicle.getPassenger() != null) {
                return Collections.singletonList(vehicle.getPassenger());
            } else {
                return Collections.emptyList();
            }
        }
    }
}