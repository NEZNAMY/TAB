package me.neznamy.tab.platforms.fabric.loader;

import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.fabric.FabricMultiVersion;
import me.neznamy.tab.platforms.fabric.FabricTabList;
import me.neznamy.tab.platforms.fabric.FabricTabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.PlayerUpdate;
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
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Method loader compiled using Minecraft 1.14.4.
 */
@SuppressWarnings({
        "unchecked", // Java generic types
        "Convert2MethodRef", // Would throw if method does not exist
        "unused", // Actually used, just via reflection
        "rawtypes" // raw enums
})
public class Loader_1_14_4 {

    private ArmorStand dummyEntity;
    private final Scoreboard dummyScoreboard = new Scoreboard();
    private Class<Enum> tablistActionClass;

    /**
     * Constructs new instance and registers all method implementations as per 1.14.4.
     *
     * @param   serverVersion
     *          Exact server version
     */
    @SneakyThrows
    public Loader_1_14_4(@NotNull ProtocolVersion serverVersion) {
        if (serverVersion.getNetworkId() < ProtocolVersion.V1_19_3.getNetworkId()) {
            if (serverVersion.getMinorVersion() >= 17) {
                tablistActionClass = (Class<Enum>) Class.forName("net.minecraft.class_2703$class_5893");
            } else {
                tablistActionClass = (Class<Enum>) Class.forName("net.minecraft.class_2703$class_2704");
            }
        }
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
        FabricMultiVersion.spawnEntity = (level, entityId, id, entityType, location) -> {
            if (dummyEntity == null) dummyEntity = new ArmorStand(level, 0, 0, 0);
            dummyEntity.setId(entityId);
            dummyEntity.setUUID(id);
            dummyEntity.setPos(location.getX(), location.getY(), location.getZ());
            return new ClientboundAddMobPacket(dummyEntity);
        };
        FabricMultiVersion.createDataWatcher = (viewer, flags, displayName, nameVisible) -> {
            Optional<Component> name = Optional.of(((FabricTabPlayer)viewer).getPlatform().toComponent(
                    TabComponent.optimized(displayName), viewer.getVersion()));
            SynchedEntityData data = new SynchedEntityData(null);
            data.define(new EntityDataAccessor<>(0, EntityDataSerializers.BYTE), flags);
            data.define(new EntityDataAccessor<>(2, EntityDataSerializers.OPTIONAL_COMPONENT), name);
            data.define(new EntityDataAccessor<>(3, EntityDataSerializers.BOOLEAN), nameVisible);
            data.define(new EntityDataAccessor<>(EntityData.getArmorStandFlagsPosition(serverVersion.getMinorVersion()), EntityDataSerializers.BYTE), EntityData.MARKER_FLAG);
            return () -> data;
        };
        FabricMultiVersion.buildTabListPacket = (action, entry) -> {
            ClientboundPlayerInfoPacket packet = createInfoPacket(serverVersion, action);
            ReflectionUtils.getFields(ClientboundPlayerInfoPacket.class, List.class).get(0).set(packet, Collections.singletonList(createUpdate(serverVersion, entry)));
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
            Enum action = (Enum) ReflectionUtils.getFields(packet.getClass(), tablistActionClass).get(0).get(packet);
            List<PlayerUpdate> players = (List<PlayerUpdate>) ReflectionUtils.getFields(packet.getClass(), List.class).get(0).get(packet);
            for (PlayerUpdate nmsData : players) {
                GameProfile profile = nmsData.getProfile();
                Field displayNameField = ReflectionUtils.getFields(PlayerUpdate.class, Component.class).get(0);
                Field latencyField = ReflectionUtils.getFields(PlayerUpdate.class, int.class).get(0);
                if (action.name().equals(TabList.Action.UPDATE_DISPLAY_NAME.name())) {
                    TabComponent newDisplayName = TAB.getInstance().getFeatureManager().onDisplayNameChange(receiver, profile.getId());
                    if (newDisplayName != null) displayNameField.set(nmsData, ((FabricTabPlayer)receiver).getPlatform().toComponent(newDisplayName, receiver.getVersion()));
                }
                if (action.name().equals(TabList.Action.UPDATE_LATENCY.name())) {
                    latencyField.set(nmsData, TAB.getInstance().getFeatureManager().onLatencyChange(receiver, profile.getId(), latencyField.getInt(nmsData)));
                }
                if (action.name().equals(TabList.Action.ADD_PLAYER.name())) {
                    TAB.getInstance().getFeatureManager().onEntryAdd(receiver, profile.getId(), profile.getName());
                }
            }
        };
        FabricMultiVersion.isPlayerInfo = packet -> packet instanceof ClientboundPlayerInfoPacket;
        FabricMultiVersion.sendPackets = (player, packets) -> {
            for (Packet<?> packet : packets) {
                player.connection.send(packet);
            }
        };
        FabricMultiVersion.getDestroyedEntities = packet -> (int[]) ReflectionUtils.getOnlyField(packet.getClass()).get(packet);
        if (serverVersion == ProtocolVersion.V1_17) {
            FabricMultiVersion.destroyEntities = (player, entities) -> {
                for (int entity : entities) {
                    // While the actual packet name is different, fabric-mapped name is the same
                    //noinspection JavaReflectionMemberAccess
                    player.sendPacket(ClientboundRemoveEntitiesPacket.class.getConstructor(int.class).newInstance(entity));
                }
            };
        } else {
            FabricMultiVersion.destroyEntities = (player, entities) -> player.sendPacket(new ClientboundRemoveEntitiesPacket(entities));
        }
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private PlayerUpdate createUpdate(@NotNull ProtocolVersion serverVersion, @NotNull FabricTabList.Builder entry) {
        Constructor<PlayerUpdate> constructor = (Constructor<PlayerUpdate>) PlayerUpdate.class.getConstructors()[0];
        if (serverVersion.getMinorVersion() >= 19) {
            // 1.19 - 1.19.2
            return constructor.newInstance(
                    entry.createProfile(), entry.getLatency(), GameType.byId(entry.getGameMode()), entry.getDisplayName(), null);
        } else if (serverVersion.getMinorVersion() >= 17) {
            // 1.17 - 1.18.2
            return constructor.newInstance(
                    entry.createProfile(), entry.getLatency(), GameType.byId(entry.getGameMode()), entry.getDisplayName());
        } else {
            // 1.14 - 1.16.5
            return constructor.newInstance(new ClientboundPlayerInfoPacket(null, Collections.emptyList()),
                    entry.createProfile(), entry.getLatency(), GameType.byId(entry.getGameMode()), entry.getDisplayName());
        }
    }

    @SneakyThrows
    private ClientboundPlayerInfoPacket createInfoPacket(@NotNull ProtocolVersion serverVersion, @NotNull TabList.Action action) {
        Class<?> classType = serverVersion.getMinorVersion() >= 17 ? Collection.class : Iterable.class;
        return ClientboundPlayerInfoPacket.class.getConstructor(tablistActionClass, classType)
                .newInstance(Enum.valueOf(tablistActionClass, action.name()), Collections.emptyList());
    }
}
