package me.neznamy.tab.platforms.sponge8;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.chat.WrappedChatComponent;
import me.neznamy.tab.api.protocol.*;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.util.ComponentCache;
import me.neznamy.tab.platforms.sponge8.nms.NMSStorage;
import me.neznamy.tab.platforms.sponge8.nms.PacketPlayOutEntityMetadata;
import me.neznamy.tab.platforms.sponge8.nms.PacketPlayOutEntityTeleport;
import me.neznamy.tab.platforms.sponge8.nms.PacketPlayOutSpawnEntityLiving;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.util.*;
import java.util.stream.Collectors;

public final class SpongePacketBuilder extends PacketBuilder {

    private final NMSStorage nms = NMSStorage.getInstance();
    public final Scoreboard dummyScoreboard = new Scoreboard();

    public static final int ARMOR_STAND_ID = 1;
    private static final UUID SYSTEM_ID = new UUID(0, 0);

    @Getter private static final ComponentCache<IChatBaseComponent, Component> componentCache = new ComponentCache<>(10000,
            (component, clientVersion) -> Component.Serializer.fromJson(component.toString(clientVersion)));

    {
        buildMap.put(PacketPlayOutEntityTeleport.class, (packet, version) -> build((PacketPlayOutEntityTeleport) packet));
        buildMap.put(PacketPlayOutEntityMetadata.class, (packet, version) -> build((PacketPlayOutEntityMetadata) packet));
        buildMap.put(PacketPlayOutSpawnEntityLiving.class, (packet, version) -> build((PacketPlayOutSpawnEntityLiving) packet));
    }

    @Override
    public Object build(PacketPlayOutChat packet, ProtocolVersion clientVersion) {
        return new ClientboundChatPacket(componentCache.get(packet.getMessage(), clientVersion), ChatType.valueOf(packet.getType().name()), SYSTEM_ID);
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
            return new ClientboundPlayerInfoPacket().new PlayerUpdate(profile, entry.getLatency(), type, componentCache.get(entry.getDisplayName(), clientVersion));
        }).collect(Collectors.toList());
        final ClientboundPlayerInfoPacket infoPacket = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.valueOf(action.name()));
        nms.ClientboundPlayerInfoPacket_entries.set(infoPacket, entries);
        return infoPacket;
    }

    @Override
    public Object build(PacketPlayOutScoreboardDisplayObjective packet, ProtocolVersion clientVersion) {
        return new ClientboundSetDisplayObjectivePacket(
                packet.getSlot(),
                new Objective(dummyScoreboard, packet.getObjectiveName(), null, TextComponent.EMPTY, null)
        );
    }

    @Override
    public Object build(PacketPlayOutScoreboardObjective packet, ProtocolVersion clientVersion) {
        String displayName = clientVersion.getMinorVersion() < 13 ? cutTo(packet.getDisplayName(), 32) : packet.getDisplayName();
        return new ClientboundSetObjectivePacket(
                new Objective(
                        dummyScoreboard,
                        packet.getObjectiveName(),
                        null,
                        componentCache.get(IChatBaseComponent.optimizedComponent(displayName), clientVersion),
                        packet.getRenderType() == null ? null : ObjectiveCriteria.RenderType.valueOf(packet.getRenderType().name())
                ),
                packet.getAction()
        );
    }

    @Override
    public Object build(PacketPlayOutScoreboardScore packet, ProtocolVersion clientVersion) {
        return new ClientboundSetScorePacket(
                ServerScoreboard.Method.valueOf(packet.getAction().name()),
                packet.getObjectiveName(),
                packet.getPlayer(),
                packet.getScore()
        );
    }

    @Override
    public Object build(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion) {
        PlayerTeam team = new PlayerTeam(dummyScoreboard, packet.getName());
        team.setAllowFriendlyFire((packet.getOptions() & 0x01) > 0);
        team.setSeeFriendlyInvisibles((packet.getOptions() & 0x02) > 0);
        team.setColor(packet.getColor() == null ? ChatFormatting.RESET : ChatFormatting.valueOf(packet.getColor().name()));
        String prefix = packet.getPlayerPrefix();
        String suffix = packet.getPlayerSuffix();
        if (clientVersion.getMinorVersion() < 13) {
            prefix = cutTo(prefix, 16);
            suffix = cutTo(suffix, 16);
        }
        if (packet.getCollisionRule() != null)
            team.setCollisionRule(Team.CollisionRule.valueOf(packet.getCollisionRule().toUpperCase(Locale.US)));
        if (packet.getNameTagVisibility() != null)
            team.setNameTagVisibility(Team.Visibility.valueOf(packet.getNameTagVisibility().toUpperCase(Locale.US)));
        if (prefix != null)
            team.setPlayerPrefix(componentCache.get(IChatBaseComponent.optimizedComponent(prefix), clientVersion));
        if (suffix != null)
            team.setPlayerSuffix(componentCache.get(IChatBaseComponent.optimizedComponent(suffix), clientVersion));
        team.getPlayers().addAll(packet.getPlayers());
        return new ClientboundSetPlayerTeamPacket(team, packet.getAction());
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

    @Override
    public PacketPlayOutScoreboardObjective readObjective(Object packet) throws ReflectiveOperationException {
        return new PacketPlayOutScoreboardObjective(
                nms.ClientboundSetObjectivePacket_action.getInt(packet),
                (String) nms.ClientboundSetObjectivePacket_objectivename.get(packet),
                null,
                PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay.INTEGER
        );
    }

    @Override
    public PacketPlayOutScoreboardDisplayObjective readDisplayObjective(Object nmsPacket) throws ReflectiveOperationException {
        return new PacketPlayOutScoreboardDisplayObjective(
                nms.ClientboundSetDisplayObjectivePacket_position.getInt(nmsPacket),
                (String) nms.ClientboundSetDisplayObjectivePacket_objectivename.get(nmsPacket)
        );
    }

    /**
     * Builds entity teleport packet from custom packet class
     *
     * @param   packet
     *          Teleport packet
     * @return  NMS teleport packet
     */
    public Object build(PacketPlayOutEntityTeleport packet) throws ReflectiveOperationException{
        ClientboundTeleportEntityPacket nmsPacket = new ClientboundTeleportEntityPacket();
        nms.ClientboundTeleportEntityPacket_ENTITYID.set(nmsPacket, packet.getEntityId());
        nms.ClientboundTeleportEntityPacket_X.set(nmsPacket, packet.getX());
        nms.ClientboundTeleportEntityPacket_Y.set(nmsPacket, packet.getY());
        nms.ClientboundTeleportEntityPacket_Z.set(nmsPacket, packet.getZ());
        nms.ClientboundTeleportEntityPacket_YAW.set(nmsPacket,(byte) (packet.getYaw()/360*256));
        nms.ClientboundTeleportEntityPacket_PITCH.set(nmsPacket, (byte) (packet.getPitch()/360*256));
        return nmsPacket;
    }


    public Object build(PacketPlayOutEntityMetadata packet) {
        return new ClientboundSetEntityDataPacket(packet.getEntityId(), packet.getDataWatcher(), true);
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
                packet.getYaw(), packet.getPitch(), Registry.ENTITY_TYPE.byId(packet.getEntityType()), 0, Vec3.ZERO);
    }
}
