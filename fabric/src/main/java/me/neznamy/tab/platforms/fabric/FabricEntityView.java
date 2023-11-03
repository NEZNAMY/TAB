package me.neznamy.tab.platforms.fabric;

import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import me.neznamy.tab.shared.backend.entityview.EntityView;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record FabricEntityView(FabricTabPlayer player) implements EntityView {

    @NotNull
    private static final Entity dummyEntity = new ArmorStand(null, 0, 0, 0);

    @Override
    public void spawnEntity(int entityId, @NotNull UUID id, @NotNull Object entityType, @NotNull Location location,
                            @NotNull EntityData data) {
        player.sendPacket(FabricMultiVersion.addEntity(entityId, id, entityType, location, data));
        updateEntityMetadata(entityId, data);
    }

    @Override
    public void updateEntityMetadata(int entityId, @NotNull EntityData data) {
        player.sendPacket(FabricMultiVersion.newEntityMetadata(entityId, data));

    }

    @Override
    public void teleportEntity(int entityId, @NotNull Location location) {
        dummyEntity.setId(entityId);
        dummyEntity.setPos(location.getX(), location.getY(), location.getZ());
        player.sendPacket(new ClientboundTeleportEntityPacket(dummyEntity));
    }

    @Override
    public void destroyEntities(int... entities) {
        FabricMultiVersion.destroyEntities(player, entities);
    }

    @Override
    public boolean isDestroyPacket(@NotNull Object packet) {
        return FabricMultiVersion.isEntityDestroyPacket(packet);
    }

    @Override
    public boolean isTeleportPacket(@NotNull Object packet) {
        return packet instanceof ClientboundTeleportEntityPacket;
    }

    @Override
    public boolean isNamedEntitySpawnPacket(@NotNull Object packet) {
        return packet instanceof ClientboundAddEntityPacket;
    }

    @Override
    public boolean isMovePacket(@NotNull Object packet) {
        return packet instanceof ClientboundMoveEntityPacket;
    }

    @Override
    public boolean isLookPacket(@NotNull Object packet) {
        return packet instanceof ClientboundMoveEntityPacket.Rot;
    }

    @Override
    public int getTeleportEntityId(@NotNull Object teleportPacket) {
        return ((ClientboundTeleportEntityPacket) teleportPacket).getId();
    }

    @Override
    public int getMoveEntityId(@NotNull Object movePacket) {
        return ((ClientboundMoveEntityPacket) movePacket).entityId;
    }

    @Override
    public int getSpawnedPlayer(@NotNull Object playerSpawnPacket) {
        return ((ClientboundAddEntityPacket) playerSpawnPacket).getId();
    }

    @Override
    public int[] getDestroyedEntities(@NotNull Object destroyPacket) {
        return FabricMultiVersion.getDestroyedEntities(destroyPacket);
    }

    @Override
    public boolean isBundlePacket(@NotNull Object packet) {
        return FabricMultiVersion.isBundlePacket(packet);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<Object> getPackets(@NotNull Object bundlePacket) {
        return (Iterable<Object>) (Object) FabricMultiVersion.getPackets(bundlePacket);
    }
}
