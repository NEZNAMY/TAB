package me.neznamy.tab.shared.backend.features.unlimitedtags;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.backend.Location;
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

    /** Reference to the main feature */
    protected final BackendNameTagX nameTagX;

    /** A player map by entity id, used for better performance */
    private final Map<Integer, TabPlayer> entityIdMap = new ConcurrentHashMap<>();

    @Override
    public void load() {
        for (TabPlayer all : TAB.getInstance().onlinePlayers()) {
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
     * Processes raw packet send.
     *
     * @param   receiver
     *          Player who received the packet
     * @param   packet
     *          Received packet
     */
    public void onPacketSend(@NotNull BackendTabPlayer receiver, @NotNull Object packet) {
        if (receiver.getEntityView().isBundlePacket(packet)) {
            for (Object wrappedPacket : receiver.getEntityView().getPackets(packet)) {
                checkPacket(receiver, wrappedPacket);
            }
        } else {
            checkPacket(receiver, packet);
        }
    }

    private void checkPacket(@NotNull BackendTabPlayer player, @NotNull Object packet) {
        if (player.getEntityView().isMovePacket(packet) && !player.getEntityView().isLookPacket(packet)) { //ignoring head rotation only packets
            onEntityMove(player, player.getEntityView().getMoveEntityId(packet), player.getEntityView().getMoveDiff(packet));
        } else if (player.getEntityView().isTeleportPacket(packet)) {
            onEntityTeleport(player, player.getEntityView().getTeleportEntityId(packet));
        } else if (player.getEntityView().isNamedEntitySpawnPacket(packet)) {
            onEntitySpawn(player, player.getEntityView().getSpawnedPlayer(packet));
        } else if (player.getEntityView().isDestroyPacket(packet)) {
            onEntityDestroy(player, player.getEntityView().getDestroyedEntities(packet));
        }
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
            TAB.getInstance().getCPUManager().runMeasuredTask(getFeatureName(), TabConstants.CpuUsageCategory.PACKET_ENTITY_SPAWN,
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
     * @param   positionDiff
     *          Position difference
     */
    public void onEntityMove(@NotNull BackendTabPlayer receiver, int entityId, Location positionDiff) {
        TabPlayer pl = entityIdMap.get(entityId);
        if (pl != null) {
            // player moved
            if (nameTagX.isPlayerDisabled(pl) || !pl.isLoaded()) return;
            BackendArmorStandManager asm = nameTagX.getArmorStandManager(pl);
            TAB.getInstance().getCPUManager().runMeasuredTask(getFeatureName(), TabConstants.CpuUsageCategory.PACKET_PLAYER_MOVE,
                    () -> asm.move(receiver, positionDiff));
        } else {
            // a non-player entity moved
            for (Integer entity : nameTagX.getVehicleManager().getVehicles().getOrDefault(entityId, Collections.emptyList())) {
                TabPlayer passenger = entityIdMap.get(entity);
                if (passenger != null) {
                    BackendArmorStandManager asm = nameTagX.getArmorStandManager(passenger);
                    if (asm != null) {
                        TAB.getInstance().getCPUManager().runMeasuredTask(getFeatureName(), TabConstants.CpuUsageCategory.PACKET_ENTITY_MOVE_PASSENGER,
                                () -> asm.move(receiver, positionDiff));
                    }
                }
            }
        }
    }

    /**
     * Processes entity teleport packet. If entity ID belongs to a player,
     * armor stands of that player are teleported to player who received the packet.
     * If it belongs to a vehicle carrying a player, that player's armor stands are
     * teleported as well.
     *
     * @param   receiver
     *          packet receiver
     * @param   entityId
     *          entity that moved
     */
    public void onEntityTeleport(@NotNull BackendTabPlayer receiver, int entityId) {
        TabPlayer pl = entityIdMap.get(entityId);
        if (pl != null) {
            // player teleported
            if (nameTagX.isPlayerDisabled(pl) || !pl.isLoaded()) return;
            BackendArmorStandManager asm = nameTagX.getArmorStandManager(pl);
            TAB.getInstance().getCPUManager().runMeasuredTask(getFeatureName(), TabConstants.CpuUsageCategory.PACKET_PLAYER_MOVE,
                    () -> asm.teleport(receiver));
        } else {
            // a non-player entity teleported
            for (Integer entity : nameTagX.getVehicleManager().getVehicles().getOrDefault(entityId, Collections.emptyList())) {
                TabPlayer passenger = entityIdMap.get(entity);
                if (passenger != null) {
                    BackendArmorStandManager asm = nameTagX.getArmorStandManager(passenger);
                    if (asm != null) {
                        TAB.getInstance().getCPUManager().runMeasuredTask(getFeatureName(), TabConstants.CpuUsageCategory.PACKET_ENTITY_MOVE_PASSENGER,
                                () -> asm.teleport(receiver));
                    }
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
                TAB.getInstance().getCPUManager().runMeasuredTask(getFeatureName(), TabConstants.CpuUsageCategory.PACKET_ENTITY_DESTROY,
                        () -> asm.destroy(receiver));
            }
        }
    }

    @Override
    @NotNull
    public String getFeatureName() {
        return nameTagX.getExtraFeatureName();
    }
}
