package me.neznamy.tab.shared.backend.entityview;

import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface EntityView {

    void spawnEntity(int entityId, @NotNull UUID id, @NotNull Object entityType, @NotNull Location location, @NotNull EntityData data);

    void updateEntityMetadata(int entityId, @NotNull EntityData data);

    void teleportEntity(int entityId, @NotNull Location location);

    void destroyEntities(int... entities);

    boolean isDestroyPacket(Object packet);

    boolean isTeleportPacket(Object packet);

    boolean isNamedEntitySpawnPacket(Object packet);

    boolean isMovePacket(Object packet);

    boolean isLookPacket(Object packet);

    int getTeleportEntityId(Object teleportPacket);

    int getMoveEntityId(Object movePacket);

    int getSpawnedPlayer(Object playerSpawnPacket);

    int[] getDestroyedEntities(Object destroyPacket);
}
