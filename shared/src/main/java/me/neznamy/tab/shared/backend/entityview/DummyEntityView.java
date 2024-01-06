package me.neznamy.tab.shared.backend.entityview;

import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.UUID;

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
    public boolean isDestroyPacket(Object packet) {
        return false;
    }

    @Override
    public boolean isTeleportPacket(Object packet) {
        return false;
    }

    @Override
    public boolean isNamedEntitySpawnPacket(Object packet) {
        return false;
    }

    @Override
    public boolean isMovePacket(Object packet) {
        return false;
    }

    @Override
    public boolean isLookPacket(Object packet) {
        return false;
    }

    @Override
    public int getTeleportEntityId(Object teleportPacket) {
        return 0;
    }

    @Override
    public int getMoveEntityId(Object movePacket) {
        return 0;
    }

    @Override
    public int getSpawnedPlayer(Object playerSpawnPacket) {
        return 0;
    }

    @Override
    public int[] getDestroyedEntities(Object destroyPacket) {
        return new int[0];
    }

    @Override
    public boolean isBundlePacket(Object packet) {
        return false;
    }

    @Override
    public Iterable<Object> getPackets(Object bundlePacket) {
        return Collections.emptyList();
    }
}
