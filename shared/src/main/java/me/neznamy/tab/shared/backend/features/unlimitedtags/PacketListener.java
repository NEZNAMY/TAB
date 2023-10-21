package me.neznamy.tab.shared.backend.features.unlimitedtags;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.QuitListener;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The packet listening part for securing proper functionality of armor stands.
 * Events are too unreliable and delayed/ahead which causes de-sync
 * if trying to listen to move event.
 * For entering/leaving tracking range there are no events and
 * periodic / move-triggered distance checks would cause high CPU usage.
 */
@RequiredArgsConstructor
public class PacketListener extends TabFeature implements JoinListener, QuitListener, Loadable {

    @Getter private final String featureName = "Unlimited NameTags";

    /** Reference to the main feature */
    protected final BackendNameTagX nameTagX;

    /** A player map by entity id, used for better performance */
    private final Map<Integer, TabPlayer> entityIdMap = new ConcurrentHashMap<>();

    @Override
    public void load() {
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            entityIdMap.put(nameTagX.getEntityId(all), all);
        }
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        entityIdMap.put(nameTagX.getEntityId(connectedPlayer), connectedPlayer);
    }

    @Override
    public void onQuit(@NotNull TabPlayer disconnectedPlayer) {
        entityIdMap.remove(nameTagX.getEntityId(disconnectedPlayer));
    }

    /**
     * Processes named entity spawn packet and spawns armor stands if
     * entity ID belongs to an online player.
     *
     * @param   receiver
     *          packet receiver
     * @param   entityId
     *          spawned entity
     */
    public void onEntitySpawn(@NotNull BackendTabPlayer receiver, int entityId) {
        TabPlayer spawnedPlayer = entityIdMap.get(entityId);
        if (spawnedPlayer != null && spawnedPlayer.isLoaded() && !nameTagX.isPlayerDisabled(spawnedPlayer)) {
            TAB.getInstance().getCPUManager().runMeasuredTask(featureName, TabConstants.CpuUsageCategory.PACKET_ENTITY_SPAWN,
                    () -> nameTagX.getArmorStandManager(spawnedPlayer).spawn(receiver));
        }
    }

    /**
     * Processes entity move packet. If entity ID belongs to a player,
     * armor stands of that player are teleported to player who received the packet.
     * If it belongs to a vehicle carrying a player, that player's armor stands are
     * teleported as well.
     *
     * @param   receiver
     *          packet receiver
     * @param   entityId
     *          entity that moved
     */
    public void onEntityMove(@NotNull BackendTabPlayer receiver, int entityId) {
        TabPlayer pl = entityIdMap.get(entityId);
        if (pl != null) {
            //player moved
            if (nameTagX.isPlayerDisabled(pl) || !pl.isLoaded()) return;
            TAB.getInstance().getCPUManager().runMeasuredTask(featureName, TabConstants.CpuUsageCategory.PACKET_ENTITY_MOVE,
                    () -> nameTagX.getArmorStandManager(pl).teleport(receiver));
        } else {
            //a vehicle carrying something moved
            for (Integer entity : nameTagX.getVehicleManager().getVehicles().getOrDefault(entityId, Collections.emptyList())) {
                TabPlayer passenger = entityIdMap.get(entity);
                if (passenger != null && nameTagX.getArmorStandManager(passenger) != null) {
                    TAB.getInstance().getCPUManager().runMeasuredTask(featureName, TabConstants.CpuUsageCategory.PACKET_ENTITY_MOVE_PASSENGER,
                            () -> nameTagX.getArmorStandManager(passenger).teleport(receiver));
                }
            }
        }
    }

    /**
     * Processes entity destroy packet and destroys armor stands if
     * entity ID belongs to an online player.
     *
     * @param   receiver
     *          packet receiver
     * @param   entities
     *          de-spawned entities
     */
    public void onEntityDestroy(@NotNull BackendTabPlayer receiver, int... entities) {
        for (int entity : entities) {
            TabPlayer deSpawnedPlayer = entityIdMap.get(entity);
            if (deSpawnedPlayer != null && deSpawnedPlayer.isLoaded() && !nameTagX.isPlayerDisabled(deSpawnedPlayer)) {
                BackendArmorStandManager asm = nameTagX.getArmorStandManager(deSpawnedPlayer);
                TAB.getInstance().getCPUManager().runMeasuredTask(featureName, TabConstants.CpuUsageCategory.PACKET_ENTITY_DESTROY,
                        () -> asm.destroy(receiver));
            }
        }
    }
}
