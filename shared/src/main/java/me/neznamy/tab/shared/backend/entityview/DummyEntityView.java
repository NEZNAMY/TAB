package me.neznamy.tab.shared.backend.entityview;

import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.UUID;

/**
 * Dummy implementation for platforms that do not support entity view.
 */
public class DummyEntityView implements EntityView {

    @Override
    public void spawnEntity(int entityId, @NotNull UUID id, @NotNull Object entityType, @NotNull Location location, @NotNull EntityData data) {
        // Do nothing
    }

    @Override
    public void updateEntityMetadata(int entityId, @NotNull EntityData data) {
        // Do nothing
    }

    @Override
    public void teleportEntity(int entityId, @NotNull Location location) {
        // Do nothing
    }

    @Override
    public void destroyEntities(int... entities) {
        // Do nothing
    }

    @Override
    public boolean isDestroyPacket(@NotNull Object packet) {
        return false;
    }

    @Override
    public boolean isTeleportPacket(@NotNull Object packet) {
        return false;
    }

    @Override
    public boolean isNamedEntitySpawnPacket(@NotNull Object packet) {
        return false;
    }

    @Override
    public boolean isMovePacket(@NotNull Object packet) {
        return false;
    }

    @Override
    public boolean isLookPacket(@NotNull Object packet) {
        return false;
    }

    @Override
    public int getTeleportEntityId(@NotNull Object teleportPacket) {
        return 0;
    }

    @Override
    public int getMoveEntityId(@NotNull Object movePacket) {
        return 0;
    }

    @Override
    public int getSpawnedPlayer(@NotNull Object playerSpawnPacket) {
        return 0;
    }

    @Override
    public int[] getDestroyedEntities(@NotNull Object destroyPacket) {
        return new int[0];
    }

    @Override
    public boolean isBundlePacket(@NotNull Object packet) {
        return false;
    }

    @Override
    public Iterable<Object> getPackets(@NotNull Object bundlePacket) {
        return Collections.emptyList();
    }
}
