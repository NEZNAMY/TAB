package me.neznamy.tab.platforms.fabric;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import me.neznamy.tab.shared.backend.entityview.EntityView;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

/**
 * EntityView implementation for Fabric using packets.
 */
@RequiredArgsConstructor
public class FabricEntityView implements EntityView {

    /** Player this view belongs to */
    @NotNull
    private final FabricTabPlayer player;

    @NotNull
    private final ArmorStand dummyEntity;

    /**
     * Constructs new instance.
     *
     * @param   player
     *          Player this view will belong to
     */
    public FabricEntityView(@NotNull FabricTabPlayer player) {
        this.player = player;

        // Make level not null, because some mods hacking deep into the server code cause NPE
        dummyEntity = new ArmorStand(FabricMultiVersion.getLevel.apply(player.getPlayer()), 0, 0, 0);
    }

    @Override
    @SneakyThrows
    public void spawnEntity(int entityId, @NotNull UUID id, @NotNull Object entityType, @NotNull Location location,
                            @NotNull EntityData data) {
        FabricMultiVersion.sendPackets.accept(player.getPlayer(), Arrays.asList(
                FabricMultiVersion.spawnEntity.apply(FabricMultiVersion.getLevel.apply(player.getPlayer()), entityId, id, entityType, location),
                FabricMultiVersion.newEntityMetadata.apply(entityId, data)
        ));
    }

    @Override
    public void updateEntityMetadata(int entityId, @NotNull EntityData data) {
        player.sendPacket(FabricMultiVersion.newEntityMetadata.apply(entityId, data));
    }

    @Override
    public void teleportEntity(int entityId, @NotNull Location location) {
        dummyEntity.setId(entityId);
        dummyEntity.setPos(location.getX(), location.getY(), location.getZ());
        player.sendPacket(new ClientboundTeleportEntityPacket(dummyEntity));
    }

    @Override
    @SneakyThrows
    public void destroyEntities(int... entities) {
        FabricMultiVersion.destroyEntities.accept(player, entities);
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
        return FabricMultiVersion.isSpawnPlayerPacket.apply((Packet<?>) packet);
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
    @SneakyThrows
    public int getSpawnedPlayer(@NotNull Object playerSpawnPacket) {
        // On 1.16.5- getter is client-only and on 1.20.2+ it is a different class
        return ReflectionUtils.getFields(playerSpawnPacket.getClass(), int.class).get(0).getInt(playerSpawnPacket);
    }

    @Override
    @SneakyThrows
    public int[] getDestroyedEntities(@NotNull Object destroyPacket) {
        return FabricMultiVersion.getDestroyedEntities.apply(destroyPacket);
    }

    @Override
    public boolean isBundlePacket(@NotNull Object packet) {
        return FabricMultiVersion.isBundlePacket.apply((Packet<?>) packet);
    }

    @Override
    public Iterable<Object> getPackets(@NotNull Object bundlePacket) {
        return FabricMultiVersion.getBundledPackets.apply((Packet<?>) bundlePacket);
    }
}
