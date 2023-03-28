package me.neznamy.tab.shared.backend.features.unlimitedtags;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.feature.*;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendTabPlayer;

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
@RequiredArgsConstructor
public class VehicleRefresher extends TabFeature implements JoinListener, QuitListener, Loadable, Refreshable {

    @Getter private final String featureName = "NameTags";
    @Getter private final String refreshDisplayName = "Refreshing vehicles";

    /** Map of players currently in a vehicle */
    private final WeakHashMap<TabPlayer, Object> playersInVehicle = new WeakHashMap<>();

    /** Map of vehicles carrying players */
    @Getter
    private final Map<Integer, List<Integer>> vehicles = new ConcurrentHashMap<>();

    /** set of players currently on boats */
    private final Set<TabPlayer> playersOnBoats = Collections.newSetFromMap(new WeakHashMap<>());

    /** Reference to the main feature */
    private final BackendNameTagX feature;

    @Override
    public void load() {
        TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(50,
                this, TabConstants.CpuUsageCategory.PROCESSING_PLAYER_MOVEMENT, () -> {
                    for (TabPlayer inVehicle : playersInVehicle.keySet()) {
                        if (!inVehicle.isOnline() || feature.getArmorStandManager(inVehicle) == null) continue; // not removed from WeakHashMap yet
                        feature.getArmorStandManager(inVehicle).teleport();
                    }
                    for (TabPlayer p : TabAPI.getInstance().getOnlinePlayers()) {
                        if (feature.isPreviewingNametag(p)) {
                            feature.getArmorStandManager(p).teleport((BackendTabPlayer) p);
                        }
                    }
                });
        addUsedPlaceholders(Collections.singletonList(TabConstants.Placeholder.VEHICLE));
        feature.registerVehiclePlaceholder();
        for (TabPlayer p : TabAPI.getInstance().getOnlinePlayers()) {
            Object vehicle = feature.getVehicle(p);
            if (vehicle != null) {
                vehicles.put(feature.getEntityId(vehicle), feature.getPassengers(vehicle));
                playersInVehicle.put(p, vehicle);
                if (feature.isDisableOnBoats() && feature.getEntityType(vehicle).contains("boat")) {
                    playersOnBoats.add(p);
                }
            }
        }
    }

    @Override
    public void onJoin(TabPlayer connectedPlayer) {
        Object vehicle = feature.getVehicle(connectedPlayer);
        if (vehicle != null) vehicles.put(feature.getEntityId(vehicle), feature.getPassengers(vehicle));
    }

    @Override
    public void onQuit(TabPlayer disconnectedPlayer) {
        if (playersInVehicle.containsKey(disconnectedPlayer)) vehicles.remove(feature.getEntityId(playersInVehicle.get(disconnectedPlayer)));
        for (List<Integer> entities : vehicles.values()) {
            entities.remove((Integer)feature.getEntityId(disconnectedPlayer));
        }
    }

    @Override
    public void refresh(TabPlayer p, boolean force) {
        if (feature.isPlayerDisabled(p)) return;
        Object vehicle = feature.getVehicle(p);
        if (playersInVehicle.containsKey(p) && vehicle == null) {
            //vehicle exit
            vehicles.remove(feature.getEntityId(playersInVehicle.get(p)));
            feature.getArmorStandManager(p).teleport();
            playersInVehicle.remove(p);
            if (feature.isDisableOnBoats() && playersOnBoats.contains(p)) {
                playersOnBoats.remove(p);
                feature.updateTeamData(p);
            }
        }
        if (!playersInVehicle.containsKey(p) && vehicle != null) {
            //vehicle enter
            vehicles.put(feature.getEntityId(vehicle), feature.getPassengers(vehicle));
            feature.getArmorStandManager(p).respawn(); //making teleport instant instead of showing teleport animation
            playersInVehicle.put(p, vehicle);
            if (feature.isDisableOnBoats() && feature.getEntityType(vehicle).contains("boat")) {
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
}
