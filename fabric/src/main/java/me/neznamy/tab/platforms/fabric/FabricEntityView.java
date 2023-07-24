package me.neznamy.tab.platforms.fabric;

import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import me.neznamy.tab.shared.backend.entityview.EntityView;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public record FabricEntityView(FabricTabPlayer player) implements EntityView {

    private static final Entity dummyEntity = new ArmorStand(null, 0, 0, 0);

    @Override
    public void spawnEntity(int entityId, @NotNull UUID id, @NotNull Object entityType, @NotNull Location location, @NotNull EntityData data) {
        player.sendPacket(new ClientboundAddEntityPacket(entityId, id,
                location.getX(), location.getY(), location.getZ(), 0, 0,
                (EntityType<?>) entityType, 0, Vec3.ZERO, 0));
        updateEntityMetadata(entityId, data);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateEntityMetadata(int entityId, @NotNull EntityData data) {
        player.sendPacket(new ClientboundSetEntityDataPacket(entityId, (List<SynchedEntityData.DataValue<?>>) data.build()));

    }

    @Override
    public void teleportEntity(int entityId, @NotNull Location location) {
        dummyEntity.setId(entityId);
        dummyEntity.setPos(location.getX(), location.getY(), location.getZ());
        player.sendPacket(new ClientboundTeleportEntityPacket(dummyEntity));
    }

    @Override
    public void destroyEntities(int... entities) {
        player.sendPacket(new ClientboundRemoveEntitiesPacket(entities));
    }

    @Override
    public boolean isDestroyPacket(Object packet) {
        return packet instanceof ClientboundRemoveEntitiesPacket;
    }

    @Override
    public boolean isTeleportPacket(Object packet) {
        return packet instanceof ClientboundTeleportEntityPacket;
    }

    @Override
    public boolean isNamedEntitySpawnPacket(Object packet) {
        return packet instanceof ClientboundAddPlayerPacket;
    }

    @Override
    public boolean isMovePacket(Object packet) {
        return packet instanceof ClientboundMoveEntityPacket;
    }

    @Override
    public boolean isLookPacket(Object packet) {
        return packet instanceof ClientboundMoveEntityPacket.Rot;
    }

    @Override
    public int getTeleportEntityId(Object teleportPacket) {
        return ((ClientboundTeleportEntityPacket) teleportPacket).getId();
    }

    @Override
    public int getMoveEntityId(Object movePacket) {
        return ((ClientboundMoveEntityPacket) movePacket).entityId;
    }

    @Override
    public int getSpawnedPlayer(Object playerSpawnPacket) {
        return ((ClientboundAddPlayerPacket) playerSpawnPacket).getEntityId();
    }

    @Override
    public int[] getDestroyedEntities(Object destroyPacket) {
        return ((ClientboundRemoveEntitiesPacket) destroyPacket).getEntityIds().toIntArray();
    }
}
