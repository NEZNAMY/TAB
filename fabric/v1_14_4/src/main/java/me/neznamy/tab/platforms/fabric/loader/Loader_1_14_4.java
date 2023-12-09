package me.neznamy.tab.platforms.fabric.loader;

import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import me.neznamy.tab.platforms.fabric.FabricMultiVersion;
import me.neznamy.tab.platforms.fabric.FabricTabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Loader_1_14_4 {

    private final ArmorStand dummyEntity = new ArmorStand(null, 0, 0, 0);
    private final Scoreboard dummyScoreboard = new Scoreboard();

    @SuppressWarnings("unchecked")
    public Loader_1_14_4(ProtocolVersion serverVersion) {
        FabricMultiVersion.getLevelName = level -> level.getLevelData().getLevelName() + level.dimension.getType().getFileSuffix();
        FabricMultiVersion.propertyToSkin = property -> new TabList.Skin(property.getValue(), property.getSignature());
        FabricMultiVersion.newEntityMetadata = (entityId, data) -> new ClientboundSetEntityDataPacket(entityId, (SynchedEntityData) data.build(), true);
        FabricMultiVersion.isSpawnPlayerPacket = packet -> packet instanceof ClientboundAddPlayerPacket;
        FabricMultiVersion.isSneaking = player -> player.isSneaking();
        FabricMultiVersion.registerTeam = team -> new ClientboundSetPlayerTeamPacket(team, 0);
        FabricMultiVersion.unregisterTeam = team -> new ClientboundSetPlayerTeamPacket(team, 1);
        FabricMultiVersion.updateTeam = team -> new ClientboundSetPlayerTeamPacket(team, 2);
        FabricMultiVersion.getLevel = player -> player.level;
        FabricMultiVersion.getPing = player -> player.latency;
        FabricMultiVersion.getDisplaySlot = packet -> ReflectionUtils.getFields(packet.getClass(), int.class).get(0).getInt(packet);
        FabricMultiVersion.setDisplaySlot = (slot, objective) -> new ClientboundSetDisplayObjectivePacket(slot, objective);
        FabricMultiVersion.getMSPT = server -> server.getAverageTickTime();
        FabricMultiVersion.newObjective = (name, displayName, renderType, numberFormat) ->
                new Objective(dummyScoreboard, name, ObjectiveCriteria.DUMMY, displayName, renderType);
        FabricMultiVersion.setScore = (objective, scoreHolder, score, displayName, numberFormat) ->
                new ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, objective, scoreHolder, score);
        FabricMultiVersion.removeScore = (objective, holder) ->
                new ClientboundSetScorePacket(ServerScoreboard.Method.REMOVE, objective, holder, 0);
        FabricMultiVersion.deserialize = string -> Component.Serializer.fromJson(string);
        FabricMultiVersion.getChannel = player -> {
            Connection c = (Connection) ReflectionUtils.getFields(ServerGamePacketListenerImpl.class, Connection.class).get(0).get(player.connection);
            return (Channel) ReflectionUtils.getFields(Connection.class, Channel.class).get(0).get(c);
        };
        FabricMultiVersion.sendMessage = (player, message) -> player.sendMessage(message);
        FabricMultiVersion.sendMessage2 = (sender, message) -> sender.sendSuccess(message, false);
        FabricMultiVersion.spawnEntity = (entityId, id, entityType, location) -> {
            dummyEntity.setId(entityId);
            dummyEntity.setUUID(id);
            dummyEntity.setPos(location.getX(), location.getY(), location.getZ());
            return new ClientboundAddMobPacket(dummyEntity);
        };
        FabricMultiVersion.createDataWatcher = (viewer, flags, displayName, nameVisible) -> {
            Optional<Component> name = Optional.of(((FabricTabPlayer)viewer).getPlatform().toComponent(
                    IChatBaseComponent.optimizedComponent(displayName), viewer.getVersion()));
            SynchedEntityData data = new SynchedEntityData(null);
            data.define(new EntityDataAccessor<>(0, EntityDataSerializers.BYTE), flags);
            data.define(new EntityDataAccessor<>(2, EntityDataSerializers.OPTIONAL_COMPONENT), name);
            data.define(new EntityDataAccessor<>(3, EntityDataSerializers.BOOLEAN), nameVisible);
            data.define(new EntityDataAccessor<>(getMarkerPosition(serverVersion.getMinorVersion()), EntityDataSerializers.BYTE), (byte)16);
            return () -> data;
        };
        FabricMultiVersion.buildTabListPacket = (action, entry) -> {
            ClientboundPlayerInfoPacket packet = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.valueOf(action.name()), Collections.emptyList());
            ReflectionUtils.getFields(ClientboundPlayerInfoPacket.class, List.class).get(0).set(packet,
                    Collections.singletonList(packet.new PlayerUpdate(
                            entry.createProfile(),
                            entry.getLatency(),
                            GameType.byId(entry.getGameMode()),
                            entry.getDisplayName()
                    )));
            return packet;
        };
        FabricMultiVersion.newHeaderFooter = (header, footer) -> {
            ClientboundTabListPacket packet = ClientboundTabListPacket.class.getConstructor().newInstance();
            List<Field> fields = ReflectionUtils.getFields(ClientboundTabListPacket.class, Component.class);
            fields.get(0).set(packet, header);
            fields.get(1).set(packet, footer);
            return packet;
        };
        FabricMultiVersion.isTeamPacket = packet -> packet instanceof ClientboundSetPlayerTeamPacket;
        FabricMultiVersion.onPlayerInfo = (receiver, packet) -> {
            // Getters are client only
            ClientboundPlayerInfoPacket.Action action = (ClientboundPlayerInfoPacket.Action) ReflectionUtils.getFields(packet.getClass(), ClientboundPlayerInfoPacket.Action.class).get(0).get(packet);
            List<ClientboundPlayerInfoPacket.PlayerUpdate> players = (List<ClientboundPlayerInfoPacket.PlayerUpdate>) ReflectionUtils.getFields(packet.getClass(), List.class).get(0).get(packet);
            for (ClientboundPlayerInfoPacket.PlayerUpdate nmsData : players) {
                GameProfile profile = nmsData.getProfile();
                Field displayNameField = ReflectionUtils.getFields(ClientboundPlayerInfoPacket.PlayerUpdate.class, Component.class).get(0);
                Field latencyField = ReflectionUtils.getFields(ClientboundPlayerInfoPacket.PlayerUpdate.class, int.class).get(0);
                if (action == ClientboundPlayerInfoPacket.Action.UPDATE_DISPLAY_NAME) {
                    IChatBaseComponent newDisplayName = TAB.getInstance().getFeatureManager().onDisplayNameChange(receiver, profile.getId());
                    if (newDisplayName != null) displayNameField.set(nmsData, ((FabricTabPlayer)receiver).getPlatform().toComponent(newDisplayName, receiver.getVersion()));
                }
                if (action == ClientboundPlayerInfoPacket.Action.UPDATE_LATENCY) {
                    latencyField.set(nmsData, TAB.getInstance().getFeatureManager().onLatencyChange(receiver, profile.getId(), latencyField.getInt(nmsData)));
                }
                if (action == ClientboundPlayerInfoPacket.Action.ADD_PLAYER) {
                    TAB.getInstance().getFeatureManager().onEntryAdd(receiver, profile.getId(), profile.getName());
                }
            }
        };
        FabricMultiVersion.isPlayerInfo = packet -> packet instanceof ClientboundPlayerInfoPacket;
    }

    private int getMarkerPosition(int serverMinorVersion) {
        if (serverMinorVersion >= 17) {
            //1.17.x, 1.18.x, 1.19.x, 1.20.x
            return 15;
        } else if (serverMinorVersion >= 15) {
            //1.15.x, 1.16.x
            return 14;
        } else {
            //1.14.x
            return 13;
        }
    }
}
