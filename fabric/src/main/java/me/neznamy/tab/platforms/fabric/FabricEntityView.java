package me.neznamy.tab.platforms.fabric;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import me.neznamy.tab.shared.backend.entityview.EntityView;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@RequiredArgsConstructor
public class FabricEntityView implements EntityView {

    private final FabricTabPlayer player;

    @NotNull
    private static final ArmorStand dummyEntity = new ArmorStand(null, 0, 0, 0);

    @Override
    public void spawnEntity(int entityId, @NotNull UUID id, @NotNull Object entityType, @NotNull Location location,
                            @NotNull EntityData data) {
        player.sendPacket(FabricTAB.getVersion().spawnEntity(entityId, id, entityType, location));
        updateEntityMetadata(entityId, data);
    }

    @Override
    public void updateEntityMetadata(int entityId, @NotNull EntityData data) {
        player.sendPacket(FabricTAB.getVersion().newEntityMetadata(entityId, data));
    }

    @Override
    public void teleportEntity(int entityId, @NotNull Location location) {
        dummyEntity.setId(entityId);
        dummyEntity.setPos(location.getX(), location.getY(), location.getZ());
        player.sendPacket(new ClientboundTeleportEntityPacket(dummyEntity));
    }

    @Override
    public void destroyEntities(int... entities) {
        FabricTAB.getVersion().destroyEntities(player, entities);
    }

    @Override
    public boolean isDestroyPacket(@NotNull Object packet) {
        return packet instanceof ClientboundRemoveEntitiesPacket;
    }

    @Override
    public boolean isTeleportPacket(@NotNull Object packet) {
        return packet instanceof ClientboundTeleportEntityPacket;
    }

    @Override
    public boolean isNamedEntitySpawnPacket(@NotNull Object packet) {
        return FabricTAB.getVersion().isSpawnPlayerPacket(packet);
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
    @SneakyThrows
    public int getTeleportEntityId(@NotNull Object teleportPacket) {
        // Reflection because on 1.16.5 there is no getter and per-version code would only add unnecessary code
        return ReflectionUtils.getFields(ClientboundTeleportEntityPacket.class, int.class).get(0).getInt(teleportPacket);
    }

    @Override
    @SneakyThrows
    public int getMoveEntityId(@NotNull Object movePacket) {
        return (int) ReflectionUtils.getFields(ClientboundMoveEntityPacket.class, int.class).get(0).get(movePacket);
    }

    @Override
    public int getSpawnedPlayer(@NotNull Object playerSpawnPacket) {
        return FabricTAB.getVersion().getSpawnedPlayer(playerSpawnPacket);
    }

    @Override
    public int[] getDestroyedEntities(@NotNull Object destroyPacket) {
        return FabricTAB.getVersion().getDestroyedEntities(destroyPacket);
    }

    @Override
    public boolean isBundlePacket(@NotNull Object packet) {
        return FabricTAB.getVersion().isBundlePacket(packet);
    }

    @Override
    public Iterable<Object> getPackets(@NotNull Object bundlePacket) {
        return FabricTAB.getVersion().getPackets(bundlePacket);
    }
}
