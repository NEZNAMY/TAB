package me.neznamy.tab.shared.backend.entityview;

import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DummyEntityView implements EntityView {

    @Override
    public void spawnEntity(int entityId, @NotNull UUID id, @NotNull Object entityType, @NotNull Location location, @NotNull EntityData data) {
        throw new UnsupportedOperationException("Not available on this platform");
    }

    @Override
    public void updateEntityMetadata(int entityId, @NotNull EntityData data) {
        throw new UnsupportedOperationException("Not available on this platform");
    }

    @Override
    public void teleportEntity(int entityId, @NotNull Location location) {
        throw new UnsupportedOperationException("Not available on this platform");
    }

    @Override
    public void destroyEntities(int... entities) {
        throw new UnsupportedOperationException("Not available on this platform");
    }

    @Override
    public boolean isDestroyPacket(Object packet) {
        throw new UnsupportedOperationException("Not available on this platform");
    }

    @Override
    public boolean isTeleportPacket(Object packet) {
        throw new UnsupportedOperationException("Not available on this platform");
    }

    @Override
    public boolean isNamedEntitySpawnPacket(Object packet) {
        throw new UnsupportedOperationException("Not available on this platform");
    }

    @Override
    public boolean isMovePacket(Object packet) {
        throw new UnsupportedOperationException("Not available on this platform");
    }

    @Override
    public boolean isLookPacket(Object packet) {
        throw new UnsupportedOperationException("Not available on this platform");
    }

    @Override
    public int getTeleportEntityId(Object teleportPacket) {
        throw new UnsupportedOperationException("Not available on this platform");
    }

    @Override
    public int getMoveEntityId(Object movePacket) {
        throw new UnsupportedOperationException("Not available on this platform");
    }

    @Override
    public int getSpawnedPlayer(Object playerSpawnPacket) {
        throw new UnsupportedOperationException("Not available on this platform");
    }

    @Override
    public int[] getDestroyedEntities(Object destroyPacket) {
        throw new UnsupportedOperationException("Not available on this platform");
    }
}
