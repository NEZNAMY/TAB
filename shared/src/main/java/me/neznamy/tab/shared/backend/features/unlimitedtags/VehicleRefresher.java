package me.neznamy.tab.shared.backend.features.unlimitedtags;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.features.types.*;
import org.jetbrains.annotations.NotNull;

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
    private final HashMap<TabPlayer, Object> playersInVehicle = new HashMap<>();

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
                featureName, TabConstants.CpuUsageCategory.PROCESSING_PLAYER_MOVEMENT, () -> {
                    for (TabPlayer inVehicle : playersInVehicle.keySet()) {
                        feature.getArmorStandManager(inVehicle).teleport();
                    }
                    for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
                        if (feature.isPreviewingNameTag(p)) {
                            feature.getArmorStandManager(p).teleport((BackendTabPlayer) p);
                        }
                    }
                });
        addUsedPlaceholder(TabConstants.Placeholder.VEHICLE);
        TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(TabConstants.Placeholder.VEHICLE, 100, p -> {
            Object v = feature.getVehicle((TabPlayer) p);
            //There's a bug in Bukkit 1.19.3 throwing NPE on .toString(), use default toString implementation
            return v == null ? "" : v.getClass().getName() + "@" + Integer.toHexString(v.hashCode());
        });
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            Object vehicle = feature.getVehicle(p);
            if (vehicle != null) {
                updateVehicle(vehicle);
                playersInVehicle.put(p, vehicle);
                if (feature.isDisableOnBoats() && feature.getEntityType(vehicle).contains("boat")) {
                    playersOnBoats.add(p);
                }
            }
        }
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        Object vehicle = feature.getVehicle(connectedPlayer);
        if (vehicle != null) updateVehicle(vehicle);
    }

    @Override
    public void onQuit(@NotNull TabPlayer disconnectedPlayer) {
        if (playersInVehicle.containsKey(disconnectedPlayer)) vehicles.remove(feature.getEntityId(playersInVehicle.remove(disconnectedPlayer)));
        for (List<Integer> entities : vehicles.values()) {
            entities.remove((Integer)feature.getEntityId(disconnectedPlayer));
        }
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
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
            feature.getArmorStandManager(p).updateVisibility(true);
        }
        if (!playersInVehicle.containsKey(p) && vehicle != null) {
            //vehicle enter
            updateVehicle(vehicle);
            feature.getArmorStandManager(p).respawn(); //making teleport instant instead of showing teleport animation
            playersInVehicle.put(p, vehicle);
            if (feature.isDisableOnBoats() && feature.getEntityType(vehicle).contains("boat")) {
                playersOnBoats.add(p);
                feature.updateTeamData(p);
            }
            feature.getArmorStandManager(p).updateVisibility(true);
        }
    }

    /**
     * Returns {@code true} if the player is in a boat, {@code false} if not
     *
     * @param   p
     *          Player to check
     * @return  {@code true} if in a boat, {@code false} if not
     */
    public boolean isOnBoat(@NotNull TabPlayer p) {
        return playersOnBoats.contains(p);
    }

    private void updateVehicle(Object vehicle) {
        feature.runInEntityScheduler(vehicle, () -> vehicles.put(feature.getEntityId(vehicle), feature.getPassengers(vehicle)));
    }
}
