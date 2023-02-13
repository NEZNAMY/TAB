package me.neznamy.tab.platforms.sponge.features.unlimitedtags;

import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.TabPacket;
import me.neznamy.tab.platforms.sponge.SpongePacketBuilder;
import me.neznamy.tab.platforms.sponge.nms.PacketPlayOutEntityMetadata;
import me.neznamy.tab.platforms.sponge.nms.PacketPlayOutEntityTeleport;
import me.neznamy.tab.platforms.sponge.nms.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.shared.backend.features.unlimitedtags.BackendArmorStand;
import me.neznamy.tab.shared.backend.features.unlimitedtags.BackendArmorStandManager;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;

public class SpongeArmorStand extends BackendArmorStand {

    /** Owner as a Sponge player */
    private final ServerPlayer player;

    /** Entity destroy packet */
    protected Packet<ClientGamePacketListener> destroyPacket = new ClientboundRemoveEntitiesPacket(entityId);

    /**
     * Constructs new instance with given parameters.
     *
     * @param   asm
     *          Armor stand manager which this armor stand belongs to
     * @param   owner
     *          Owner of the armor stand
     * @param   propertyName
     *          Name of refresh property to use
     * @param   yOffset
     *          Offset in blocks
     * @param   staticOffset
     *          {@code true} if offset is static, {@code false} if not
     */
    public SpongeArmorStand(SpongeNameTagX feature, BackendArmorStandManager asm, TabPlayer owner, String propertyName, double yOffset, boolean staticOffset) {
        super(feature, asm, owner, propertyName, yOffset, staticOffset);
        player = (ServerPlayer) owner.getPlayer();
        sneaking = player.sneaking().get();
    }

    @Override
    public void spawn(TabPlayer viewer) {
        for (TabPacket packet : getSpawnPackets(viewer)) {
            viewer.sendPacket(packet);
        }
    }

    @Override
    public void destroy(TabPlayer viewer) {
        viewer.sendPacket(destroyPacket);
    }

    /**
     * Returns Y location where armor stand should be at time of calling.
     * This takes into account everything that affects height, including
     * viewer's game version.
     *
     * @param   viewer
     *          Player looking at the armor stand
     * @return  Location where armor stand should be for specified viewer
     */
    public double getLocation(TabPlayer viewer) {
        double y = player.location().y();
        //1.14+ server sided bug
        Entity vehicle = (Entity) manager.getVehicle(owner);
        if (vehicle != null) {
            String type = manager.getEntityType(vehicle);
            if (type.contains("HORSE")) { //covering all 3 horse types
                y = vehicle.location().y() + 0.85;
            }
            if (type.equals("DONKEY")) { //1.11+
                y = vehicle.location().y() + 0.525;
            }
            if (type.equals("PIG")) {
                y = vehicle.location().y() + 0.325;
            }
            if (type.equals("STRIDER")) { //1.16+
                y = vehicle.location().y() + 1.15;
            }
        } else {
            //1.13+ swimming or 1.9+ flying with elytra
            if (isSwimming() || isGliding()) {
                y = player.location().y()-1.22;
            }
        }
        y += getYAdd(player.sleeping().get(), sneaking, viewer);
        return y;
    }

    @Override
    public void updateMetadata(TabPlayer viewer) {
        viewer.sendPacket(new ClientboundSetEntityDataPacket(entityId, createDataWatcher(property.getFormat(viewer), viewer), true));
    }

    @Override
    public void sendTeleportPacket(TabPlayer viewer) {
        viewer.sendCustomPacket(new PacketPlayOutEntityTeleport(entityId, player.location().x(),
                        getLocation(viewer), player.location().z(), 0, 0),
                TabConstants.PacketCategory.UNLIMITED_NAMETAGS_TELEPORT);
    }

    /**
     * Returns {@code true} if owner is swimming, {@code false} if not
     * @return  {@code true} if owner is swimming, {@code false} if not
     */
    private boolean isSwimming() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private boolean isGliding() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private TabPacket[] getSpawnPackets(TabPlayer viewer) {
        visible = calculateVisibility();
        return new TabPacket[] {
                new PacketPlayOutSpawnEntityLiving(entityId, uuid, SpongePacketBuilder.getInstance().ARMOR_STAND_ID,
                        player.location().x(), getLocation(viewer), player.location().z(), 0, 0),
                new PacketPlayOutEntityMetadata(entityId, createDataWatcher(property.getFormat(viewer), viewer))
        };
    }

    /**
     * Creates data watcher with specified display name for viewer
     *
     * @param   displayName
     *          armor stand name
     * @param   viewer
     *          player to apply checks against
     * @return  DataWatcher for viewer
     */
    public SynchedEntityData createDataWatcher(String displayName, TabPlayer viewer) {
        SynchedEntityData dataWatcher = new SynchedEntityData(null);
        dataWatcher.define(new EntityDataAccessor<>(0, EntityDataSerializers.BYTE), (byte) (sneaking ? 34 : 32));
        dataWatcher.define(new EntityDataAccessor<>(2, EntityDataSerializers.OPTIONAL_COMPONENT),
                Optional.ofNullable(SpongePacketBuilder.getComponentCache().get(
                        IChatBaseComponent.optimizedComponent(displayName), viewer.getVersion())));
        dataWatcher.define(new EntityDataAccessor<>(3, EntityDataSerializers.BOOLEAN), !shouldBeInvisibleFor(viewer, displayName) && visible);
        if (viewer.getVersion().getMinorVersion() > 8 || manager.isMarkerFor18x())
            dataWatcher.define(new EntityDataAccessor<>(14, EntityDataSerializers.BYTE), (byte)16);
        return dataWatcher;
    }
}
