package me.neznamy.tab.shared.backend.entityview;

import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Class for manipulating entity view.
 * Used by unlimited nametags and 1.8 bossbar.
 */
public interface EntityView {

    /**
     * Sends spawn entity packet.
     *
     * @param   entityId
     *          Entity's id
     * @param   id
     *          Entity's UUID
     * @param   entityType
     *          Entity type
     * @param   location
     *          Spawn location
     * @param   data
     *          Entity metadata
     */
    void spawnEntity(int entityId, @NotNull UUID id, @NotNull Object entityType, @NotNull Location location, @NotNull EntityData data);

    /**
     * Sends update metadata packet.
     *
     * @param   entityId
     *          Entity's id
     * @param   data
     *          Metadata to update
     */
    void updateEntityMetadata(int entityId, @NotNull EntityData data);

    /**
     * Teleports entity to given location.
     *
     * @param   entityId
     *          Entity's id
     * @param   location
     *          Teleport location
     */
    void teleportEntity(int entityId, @NotNull Location location);

    /**
     * Destroys entities.
     *
     * @param   entities
     *          Entities to destroy
     */
    void destroyEntities(int... entities);

    /**
     * Returns {@code true} if given packet is entity destroy packet, {@code false} if not.
     *
     * @param   packet
     *          Packet to check
     * @return  {@code true} if packet is entity destroy packet, {@code false} if not.
     */
    boolean isDestroyPacket(@NotNull Object packet);

    /**
     * Returns {@code true} if given packet is entity teleport packet, {@code false} if not.
     *
     * @param   packet
     *          Packet to check
     * @return  {@code true} if packet is entity teleport packet, {@code false} if not.
     */
    boolean isTeleportPacket(@NotNull Object packet);

    /**
     * Returns {@code true} if given packet is spawn player packet, {@code false} if not.
     *
     * @param   packet
     *          Packet to check
     * @return  {@code true} if packet is spawn player packet, {@code false} if not.
     */
    boolean isNamedEntitySpawnPacket(@NotNull Object packet);

    /**
     * Returns {@code true} if given packet is entity move packet, {@code false} if not.
     *
     * @param   packet
     *          Packet to check
     * @return  {@code true} if packet is entity move packet, {@code false} if not.
     */
    boolean isMovePacket(@NotNull Object packet);

    /**
     * Returns {@code true} if given packet is entity look packet, {@code false} if not.
     *
     * @param   packet
     *          Packet to check
     * @return  {@code true} if packet is entity look packet, {@code false} if not.
     */
    boolean isLookPacket(@NotNull Object packet);

    /**
     * Gets entity id from teleport packet.
     *
     * @param   teleportPacket
     *          Entity teleport packet
     * @return  Entity id from packet
     */
    int getTeleportEntityId(@NotNull Object teleportPacket);

    /**
     * Gets entity id from move packet.
     *
     * @param   movePacket
     *          Entity move packet
     * @return  Entity id from packet
     */
    int getMoveEntityId(@NotNull Object movePacket);

    /**
     * Gets entity id from spawn entity packet.
     *
     * @param   playerSpawnPacket
     *          Entity spawn packet
     * @return  Entity id from packet
     */
    int getSpawnedPlayer(@NotNull Object playerSpawnPacket);

    /**
     * Gets entities from destroy packet.
     *
     * @param   destroyPacket
     *          Entity destroy packet
     * @return  Entity ids from packet
     */
    int[] getDestroyedEntities(@NotNull Object destroyPacket);

    /**
     * Returns {@code true} if given packet is bundle packet, {@code false} if not.
     *
     * @param   packet
     *          Packet to check
     * @return  {@code true} if packet is bundle packet, {@code false} if not.
     */
    boolean isBundlePacket(@NotNull Object packet);

    /**
     * Returns bundled packets from bundle packet.
     *
     * @param   bundlePacket
     *          Bundle packet (1.19.4+)
     * @return  Bundled packets
     */
    Iterable<Object> getPackets(@NotNull Object bundlePacket);
}
