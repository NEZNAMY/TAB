package me.neznamy.tab.platforms.fabric;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import me.neznamy.tab.shared.backend.entityview.EntityView;
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
        dummyEntity = new ArmorStand(FabricMultiVersion.getLevel(player.getPlayer()), 0, 0, 0);
    }

    @Override
    public void spawnEntity(int entityId, @NotNull UUID id, @NotNull Object entityType, @NotNull Location location,
                            @NotNull EntityData data) {
        FabricMultiVersion.sendPackets(player.getPlayer(), Arrays.asList(
                FabricMultiVersion.spawnEntity(FabricMultiVersion.getLevel(player.getPlayer()), entityId, id, entityType, location),
                FabricMultiVersion.newEntityMetadata(entityId, data)
        ));
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
        FabricMultiVersion.destroyEntities(player.getPlayer(), entities);
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
        return FabricMultiVersion.isSpawnPlayerPacket((Packet<?>) packet);
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
        return ((ClientboundTeleportEntityPacket)teleportPacket).id;
    }

    @Override
    public int getMoveEntityId(@NotNull Object movePacket) {
        return ((ClientboundMoveEntityPacket)movePacket).entityId;
    }

    @Override
    public int getSpawnedPlayer(@NotNull Object playerSpawnPacket) {
        return FabricMultiVersion.getSpawnedPlayerId((Packet<?>) playerSpawnPacket);
    }

    @Override
    public int[] getDestroyedEntities(@NotNull Object destroyPacket) {
        return FabricMultiVersion.getDestroyedEntities((Packet<?>) destroyPacket);
    }

    @Override
    public boolean isBundlePacket(@NotNull Object packet) {
        return FabricMultiVersion.isBundlePacket((Packet<?>) packet);
    }

    @Override
    public Iterable<Object> getPackets(@NotNull Object bundlePacket) {
        return FabricMultiVersion.getBundledPackets((Packet<?>) bundlePacket);
    }

    @Override
    @NotNull
    public Location getMoveDiff(@NotNull Object movePacket) {
        ClientboundMoveEntityPacket packet = (ClientboundMoveEntityPacket) movePacket;
        return new Location(packet.xa, packet.ya, packet.za);
    }

    @Override
    public void moveEntity(int entityId, @NotNull Location moveDiff) {
        player.sendPacket(new ClientboundMoveEntityPacket.Pos(
                entityId,
                (short) moveDiff.getX(),
                (short) moveDiff.getY(),
                (short) moveDiff.getZ(),
                false
        ));
    }
}
