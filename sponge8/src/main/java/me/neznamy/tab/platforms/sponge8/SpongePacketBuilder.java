package me.neznamy.tab.platforms.sponge8;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.chat.WrappedChatComponent;
import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.Skin;
import me.neznamy.tab.api.util.ComponentCache;
import me.neznamy.tab.platforms.sponge8.nms.NMSStorage;
import me.neznamy.tab.shared.backend.protocol.PacketPlayOutEntityDestroy;
import me.neznamy.tab.shared.backend.protocol.PacketPlayOutEntityMetadata;
import me.neznamy.tab.shared.backend.protocol.PacketPlayOutEntityTeleport;
import me.neznamy.tab.shared.backend.protocol.PacketPlayOutSpawnEntityLiving;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class SpongePacketBuilder extends PacketBuilder {

    private final NMSStorage nms = NMSStorage.getInstance();
    private final ArmorStand dummyEntity = new ArmorStand(EntityType.ARMOR_STAND, null);

    @Getter private static final ComponentCache<IChatBaseComponent, Component> componentCache = new ComponentCache<>(10000,
            (component, clientVersion) -> Component.Serializer.fromJson(component.toString(clientVersion)));

    {
        buildMap.put(PacketPlayOutEntityDestroy.class, (packet, version) -> build((PacketPlayOutEntityDestroy) packet));
        buildMap.put(PacketPlayOutEntityTeleport.class, (packet, version) -> build((PacketPlayOutEntityTeleport) packet));
        buildMap.put(PacketPlayOutEntityMetadata.class, (packet, version) -> build((PacketPlayOutEntityMetadata) packet));
        buildMap.put(PacketPlayOutSpawnEntityLiving.class, (packet, version) -> build((PacketPlayOutSpawnEntityLiving) packet));
    }

    @Override
    public Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) throws IllegalAccessException {
        final EnumPlayerInfoAction action = packet.getActions().contains(EnumPlayerInfoAction.ADD_PLAYER) ? EnumPlayerInfoAction.ADD_PLAYER : packet.getActions().iterator().next();
        final List<ClientboundPlayerInfoPacket.PlayerUpdate> entries = packet.getEntries().stream().map(entry -> {
            final GameProfile profile = new GameProfile(entry.getUniqueId(), entry.getName());
            if (entry.getSkin() != null) {
                profile.getProperties().put("textures", new Property("textures", entry.getSkin().getValue(), entry.getSkin().getSignature()));
            }
            final GameType type = GameType.valueOf(entry.getGameMode().name());
            Component displayName = entry.getDisplayName() instanceof WrappedChatComponent ?
                    (Component) ((WrappedChatComponent) entry.getDisplayName()).getOriginalComponent() : componentCache.get(entry.getDisplayName(), clientVersion);
            return new ClientboundPlayerInfoPacket().new PlayerUpdate(profile, entry.getLatency(), type, displayName);
        }).collect(Collectors.toList());
        final ClientboundPlayerInfoPacket infoPacket = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.valueOf(action.name()));
        nms.ClientboundPlayerInfoPacket_entries.set(infoPacket, entries);
        return infoPacket;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PacketPlayOutPlayerInfo readPlayerInfo(Object obj, ProtocolVersion clientVersion) throws IllegalAccessException {
        ClientboundPlayerInfoPacket packet = (ClientboundPlayerInfoPacket) obj;
        EnumPlayerInfoAction action = EnumPlayerInfoAction.valueOf(nms.ClientboundPlayerInfoPacket_action.get(packet).toString());
        List<PacketPlayOutPlayerInfo.PlayerInfoData> listData = new ArrayList<>();
        for (ClientboundPlayerInfoPacket.PlayerUpdate nmsData : (List<ClientboundPlayerInfoPacket.PlayerUpdate>) nms.ClientboundPlayerInfoPacket_entries.get(packet)) {
            GameProfile profile = nmsData.getProfile();
            Skin skin = null;
            if (!profile.getProperties().get("textures").isEmpty()) {
                Property pr = profile.getProperties().get("textures").iterator().next();
                skin = new Skin(pr.getValue(), pr.getSignature());
            }
            listData.add(new PacketPlayOutPlayerInfo.PlayerInfoData(
                    profile.getName(),
                    profile.getId(),
                    skin,
                    true,
                    nmsData.getLatency(),
                    nmsData.getGameMode() == null ? null : PacketPlayOutPlayerInfo.EnumGamemode.valueOf(nmsData.getGameMode().name()),
                    nmsData.getDisplayName() == null ? null : new WrappedChatComponent(nmsData.getDisplayName()),
                    null,
                    null));
        }
        return new PacketPlayOutPlayerInfo(action, listData);
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
