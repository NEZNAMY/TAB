package me.neznamy.tab.platforms.fabric.loader;

import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import me.neznamy.tab.platforms.fabric.FabricMultiVersion;
import me.neznamy.tab.platforms.fabric.FabricTabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.FixedFormat;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Method loader compiled using Minecraft 1.20.4.
 */
@SuppressWarnings({
        "unchecked", // Java generic types
        "DataFlowIssue", // Profile is not null on add action
        "unused" // Actually used, just via reflection
})
public class Loader_1_20_4 {

    private static final Scoreboard dummyScoreboard = new Scoreboard();

    /**
     * Constructs new instance and overrides all methods to their current format based on server version.
     *
     * @param   serverVersion
     *          Exact server version
     */
    public Loader_1_20_4(@NotNull ProtocolVersion serverVersion) {
        if (serverVersion.getMinorVersion() >= 15) {
            FabricMultiVersion.isSneaking = Entity::isCrouching;
        }
        if (serverVersion.getMinorVersion() >= 16) {
            FabricMultiVersion.deserialize = Component.Serializer::fromJson; // Return type has changed
            FabricMultiVersion.getLevelName = level -> {
                String path = level.dimension().location().getPath();
                return ((ServerLevelData)level.getLevelData()).getLevelName() + switch (path) {
                    case "overworld" -> ""; // No suffix for overworld
                    case "the_nether" -> "_nether";
                    default -> "_" + path; // End + default behavior for other dimensions created by mods
                };
            };
        }
        if (serverVersion.getMinorVersion() >= 17) {
            FabricMultiVersion.registerTeam = team -> ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
            FabricMultiVersion.unregisterTeam = ClientboundSetPlayerTeamPacket::createRemovePacket;
            FabricMultiVersion.updateTeam = team -> ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, false);
            FabricMultiVersion.newHeaderFooter = ClientboundTabListPacket::new;
            FabricMultiVersion.isTeamPacket = packet -> packet instanceof ClientboundSetPlayerTeamPacket; // Fabric-mapped name changed
        }
        if (serverVersion.getMinorVersion() >= 19) {
            FabricMultiVersion.sendMessage = ServerPlayer::sendSystemMessage;
            FabricMultiVersion.sendMessage2 = CommandSourceStack::sendSystemMessage;
            FabricMultiVersion.spawnEntity = (level, entityId, id, entityType, location) ->
                    new ClientboundAddEntityPacket(entityId, id, location.getX(), location.getY(), location.getZ(),
                            0, 0, (EntityType<?>) entityType, 0, Vec3.ZERO, 0);
        }
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId()) {
            FabricMultiVersion.newEntityMetadata = (entityId, data) ->  new ClientboundSetEntityDataPacket(entityId, (List<SynchedEntityData.DataValue<?>>) data.build());
            FabricMultiVersion.createDataWatcher = (viewer, flags, displayName, nameVisible) -> {
                Optional<Component> name = Optional.of(((FabricTabPlayer)viewer).getPlatform().toComponent(IChatBaseComponent.optimizedComponent(displayName), viewer.getVersion()));
                return () -> Arrays.asList(
                        new SynchedEntityData.DataValue<>(0, EntityDataSerializers.BYTE, flags),
                        new SynchedEntityData.DataValue<>(2, EntityDataSerializers.OPTIONAL_COMPONENT, name),
                        new SynchedEntityData.DataValue<>(3, EntityDataSerializers.BOOLEAN, nameVisible),
                        new SynchedEntityData.DataValue<>(EntityData.getArmorStandFlagsPosition(serverVersion.getMinorVersion()), EntityDataSerializers.BYTE, EntityData.MARKER_FLAG)
                );
            };
            FabricMultiVersion.buildTabListPacket = (action, entry) -> {
                if (action == TabList.Action.REMOVE_PLAYER) {
                    return new ClientboundPlayerInfoRemovePacket(Collections.singletonList(entry.getId()));
                }
                EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions = action == TabList.Action.ADD_PLAYER ?
                        EnumSet.allOf(ClientboundPlayerInfoUpdatePacket.Action.class) :
                        Register1_19_3.convertAction(action);
                ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(actions, Collections.emptyList());
                ReflectionUtils.getFields(ClientboundPlayerInfoUpdatePacket.class, List.class).get(0).set(packet,
                        Collections.singletonList(new ClientboundPlayerInfoUpdatePacket.Entry(
                                entry.getId(),
                                entry.createProfile(),
                                true,
                                entry.getLatency(),
                                GameType.byId(entry.getGameMode()),
                                entry.getDisplayName(),
                                null
                        )));
                return packet;
            };
            FabricMultiVersion.onPlayerInfo = (receiver, packet0) -> {
                ClientboundPlayerInfoUpdatePacket packet = (ClientboundPlayerInfoUpdatePacket) packet0;
                EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions = packet.actions();
                List<ClientboundPlayerInfoUpdatePacket.Entry> updatedList = new ArrayList<>();
                for (ClientboundPlayerInfoUpdatePacket.Entry nmsData : packet.entries()) {
                    GameProfile profile = nmsData.profile();
                    Component displayName = nmsData.displayName();
                    int latency = nmsData.latency();
                    if (actions.contains(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME)) {
                        IChatBaseComponent newDisplayName = TAB.getInstance().getFeatureManager().onDisplayNameChange(receiver, nmsData.profileId());
                        if (newDisplayName != null) displayName = ((FabricTabPlayer)receiver).getPlatform().toComponent(newDisplayName, receiver.getVersion());
                    }
                    if (actions.contains(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY)) {
                        latency = TAB.getInstance().getFeatureManager().onLatencyChange(receiver, nmsData.profileId(), latency);
                    }
                    if (actions.contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER)) {
                        TAB.getInstance().getFeatureManager().onEntryAdd(receiver, nmsData.profileId(), profile.getName());
                    }
                    updatedList.add(new ClientboundPlayerInfoUpdatePacket.Entry(nmsData.profileId(), profile, nmsData.listed(), latency, nmsData.gameMode(), displayName, nmsData.chatSession()));
                }
                ReflectionUtils.getFields(ClientboundPlayerInfoUpdatePacket.class, List.class).get(0).set(packet, updatedList);
            };
            FabricMultiVersion.isPlayerInfo = packet -> packet instanceof ClientboundPlayerInfoUpdatePacket;
        }
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_19_4.getNetworkId()) {
            FabricMultiVersion.isBundlePacket = packet -> packet instanceof ClientboundBundlePacket;
            FabricMultiVersion.getBundledPackets = packet -> (Iterable<Object>) (Object) ((ClientboundBundlePacket)packet).subPackets();
            FabricMultiVersion.sendPackets = (player, packets) -> player.connection.send(new ClientboundBundlePacket(packets));
        }
        if (serverVersion.getMinorVersion() >= 20) {
            FabricMultiVersion.getLevel = Entity::level;
        }
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_2.getNetworkId()) {
            FabricMultiVersion.propertyToSkin = property -> new TabList.Skin(property.value(), property.signature());
            FabricMultiVersion.isSpawnPlayerPacket = packet -> packet instanceof ClientboundAddEntityPacket;
            FabricMultiVersion.getPing = player -> player.connection.latency();
            FabricMultiVersion.getDisplaySlot = packet -> packet.getSlot().ordinal();
            FabricMultiVersion.setDisplaySlot = (slot, objective) -> new ClientboundSetDisplayObjectivePacket(DisplaySlot.values()[slot], objective);
            FabricMultiVersion.getChannel = player -> {
                Connection c = (Connection) ReflectionUtils.getFields(ServerCommonPacketListenerImpl.class, Connection.class).get(0).get(player.connection);
                return (Channel) ReflectionUtils.getFields(Connection.class, Channel.class).get(0).get(c);
            };
        }
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_3.getNetworkId()) {
            FabricMultiVersion.getMSPT = server -> (float) server.getAverageTickTimeNanos() / 1000000;
            FabricMultiVersion.removeScore = (objective, holder) -> new ClientboundResetScorePacket(holder, objective);
            Register1_20_3.register();
        }
    }

    /**
     * Why is this needed? Because otherwise it throws error about a class
     * not existing despite the code never running.
     * Why? Nobody knows.
     */
    private static class Register1_20_3 {

        public static void register() {
            FabricMultiVersion.newObjective = (name, displayName, renderType, numberFormat) ->
                    new Objective(dummyScoreboard, name, ObjectiveCriteria.DUMMY, displayName, renderType, false,
                            numberFormat == null ? null : new FixedFormat(numberFormat));
            FabricMultiVersion.setScore = (objective, scoreHolder, score, displayName, numberFormat) ->
                    new ClientboundSetScorePacket(scoreHolder, objective, score, displayName,
                            numberFormat == null ? null : new FixedFormat(numberFormat));
        }
    }

    private static class Register1_19_3 {

        public static EnumSet<ClientboundPlayerInfoUpdatePacket.Action> convertAction(TabList.Action action) {
            return EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.valueOf(action.name()));
        }
    }
}
