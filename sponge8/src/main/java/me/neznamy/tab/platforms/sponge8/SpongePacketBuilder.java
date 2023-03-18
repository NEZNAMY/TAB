package me.neznamy.tab.platforms.sponge8;

import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.shared.backend.protocol.PacketPlayOutEntityDestroy;
import me.neznamy.tab.shared.backend.protocol.PacketPlayOutEntityMetadata;
import me.neznamy.tab.shared.backend.protocol.PacketPlayOutEntityTeleport;
import me.neznamy.tab.shared.backend.protocol.PacketPlayOutSpawnEntityLiving;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;

public final class SpongePacketBuilder extends PacketBuilder {

    private final ArmorStand dummyEntity = new ArmorStand(EntityType.ARMOR_STAND, null);

    {
        buildMap.put(PacketPlayOutEntityDestroy.class, (packet, version) -> build((PacketPlayOutEntityDestroy) packet));
        buildMap.put(PacketPlayOutEntityTeleport.class, (packet, version) -> build((PacketPlayOutEntityTeleport) packet));
        buildMap.put(PacketPlayOutEntityMetadata.class, (packet, version) -> build((PacketPlayOutEntityMetadata) packet));
        buildMap.put(PacketPlayOutSpawnEntityLiving.class, (packet, version) -> build((PacketPlayOutSpawnEntityLiving) packet));
    }

    /**
     * Builds entity teleport packet from custom packet class
     *
     * @param   packet
     *          Teleport packet
     * @return  NMS teleport packet
     */
    public Object build(PacketPlayOutEntityTeleport packet) {
        // While the entity is shared, packets are build in a single thread, so no risk of concurrent access
        dummyEntity.setId(packet.getEntityId());
        dummyEntity.setPos(packet.getX(), packet.getY(), packet.getZ());
        dummyEntity.xRot = packet.getYaw();
        dummyEntity.yRot = packet.getPitch();
        return new ClientboundTeleportEntityPacket(dummyEntity);
    }


    public Object build(PacketPlayOutEntityMetadata packet) {
        return new ClientboundSetEntityDataPacket(packet.getEntityId(), (SynchedEntityData) packet.getDataWatcher(), true);
    }

    /**
     * Builds entity spawn packet from custom packet class
     *
     * @param   packet
     *          Spawn packet
     * @return  NMS spawn packet
     */

    public Object build(PacketPlayOutSpawnEntityLiving packet) {
        return new ClientboundAddEntityPacket(packet.getEntityId(), packet.getUniqueId(), packet.getX(), packet.getY(), packet.getZ(),
                packet.getYaw(), packet.getPitch(), Registry.ENTITY_TYPE.byId((Integer) packet.getEntityType()), 0, Vec3.ZERO);
    }

    public Object build(PacketPlayOutEntityDestroy packet) {
        return new ClientboundRemoveEntitiesPacket(packet.getEntities());
    }
}
