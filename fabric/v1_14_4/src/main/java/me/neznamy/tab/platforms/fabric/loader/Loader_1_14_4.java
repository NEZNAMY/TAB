package me.neznamy.tab.platforms.fabric.loader;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.fabric.FabricTabList;
import me.neznamy.tab.platforms.fabric.FabricTabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.PlayerUpdate;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Method loader compiled using Minecraft 1.14.4.
 */
@SuppressWarnings({
        "unchecked", // Java generic types
        "unused", // Actually used, just via reflection
        "rawtypes" // raw enums
})
@RequiredArgsConstructor
public class Loader_1_14_4 implements Loader {

    private final ProtocolVersion serverVersion;
    private ArmorStand dummyEntity;
    private Class<Enum> tablistActionClass;

    @SneakyThrows
    private ClientboundPlayerInfoPacket createInfoPacket(@NotNull ProtocolVersion serverVersion, @NotNull TabList.Action action) {
        if (tablistActionClass == null) {
            if (serverVersion.getMinorVersion() >= 17) {
                tablistActionClass = (Class<Enum>) Class.forName("net.minecraft.class_2703$class_5893");
            } else {
                tablistActionClass = (Class<Enum>) Class.forName("net.minecraft.class_2703$class_2704");
            }
        }
        Class<?> classType = serverVersion.getMinorVersion() >= 17 ? Collection.class : Iterable.class;
        return ClientboundPlayerInfoPacket.class.getConstructor(tablistActionClass, classType)
                .newInstance(Enum.valueOf(tablistActionClass, action.name()), Collections.emptyList());
    }

    @Override
    @NotNull
    public String getLevelName(@NotNull Level level) {
        return level.getLevelData().getLevelName() + level.dimension.getType().getFileSuffix();
    }

    @Override
    @NotNull
    public TabList.Skin propertyToSkin(@NotNull Property property) {
        return new TabList.Skin(property.getValue(), property.getSignature());
    }

    @Override
    @NotNull
    public Component newTextComponent(@NotNull String text) {
        return new TextComponent(text);
    }

    @Override
    @NotNull
    public Style convertModifier(@NotNull ChatModifier modifier, boolean modern) {
        Style style = new Style();
        if (modifier.getColor() != null) {
            style.setColor(ChatFormatting.valueOf(modifier.getColor().getLegacyColor().name()));
        }
        if (modifier.isBold()) style.setBold(true);
        if (modifier.isItalic()) style.setItalic(true);
        if (modifier.isStrikethrough()) style.setStrikethrough(true);
        if (modifier.isUnderlined()) style.setUnderlined(true);
        if (modifier.isObfuscated()) style.setObfuscated(true);
        return style;
    }

    @Override
    public void addSibling(@NotNull Component parent, @NotNull Component child) {
        parent.append(child);
    }

    @Override
    @NotNull
    public Packet<?> registerTeam(@NotNull PlayerTeam team) {
        return new ClientboundSetPlayerTeamPacket(team, 0);
    }

    @Override
    @NotNull
    public Packet<?> unregisterTeam(@NotNull PlayerTeam team) {
        return new ClientboundSetPlayerTeamPacket(team, 1);
    }

    @Override
    @NotNull
    public Packet<?> updateTeam(@NotNull PlayerTeam team) {
        return new ClientboundSetPlayerTeamPacket(team, 2);
    }

    @Override
    public boolean isSneaking(@NotNull ServerPlayer player) {
        return player.isSneaking();
    }

    @Override
    public void sendMessage(@NotNull ServerPlayer player, @NotNull Component message) {
        player.sendMessage(message);
    }

    @Override
    public void sendMessage(@NotNull CommandSourceStack source, @NotNull Component message) {
        source.sendSuccess(message, false);
    }

    @Override
    @NotNull
    @SneakyThrows
    public Packet<?> newHeaderFooter(@NotNull Component header, @NotNull Component footer) {
        ClientboundTabListPacket packet = ClientboundTabListPacket.class.getConstructor().newInstance();
        List<Field> fields = ReflectionUtils.getFields(ClientboundTabListPacket.class, Component.class);
        fields.get(0).set(packet, header);
        fields.get(1).set(packet, footer);
        return packet;
    }

    @Override
    public boolean isTeamPacket(@NotNull Packet<?> packet) {
        return packet instanceof ClientboundSetPlayerTeamPacket;
    }

    @Override
    @NotNull
    public Packet<ClientGamePacketListener> spawnEntity(@NotNull Level level, int id, @NotNull UUID uuid, @NotNull Object type, @NotNull Location location) {
        if (dummyEntity == null) dummyEntity = new ArmorStand(level, 0, 0, 0);
        dummyEntity.setId(id);
        dummyEntity.setUUID(uuid);
        dummyEntity.setPos(location.getX(), location.getY(), location.getZ());
        return new ClientboundAddMobPacket(dummyEntity);
    }

    @Override
    @NotNull
    public Packet<ClientGamePacketListener> newEntityMetadata(int entityId, @NotNull EntityData data) {
        return new ClientboundSetEntityDataPacket(entityId, (SynchedEntityData) data.build(), true);
    }

    @Override
    @NotNull
    public EntityData createDataWatcher(@NotNull TabPlayer viewer, byte flags, @NotNull String displayName, boolean nameVisible, int markerPosition) {
        Optional<Component> name = Optional.of(TabComponent.optimized(displayName).convert(viewer.getVersion()));
        SynchedEntityData data = new SynchedEntityData(null);
        data.define(new EntityDataAccessor<>(0, EntityDataSerializers.BYTE), flags);
        data.define(new EntityDataAccessor<>(2, EntityDataSerializers.OPTIONAL_COMPONENT), name);
        data.define(new EntityDataAccessor<>(3, EntityDataSerializers.BOOLEAN), nameVisible);
        data.define(new EntityDataAccessor<>(markerPosition, EntityDataSerializers.BYTE), EntityData.MARKER_FLAG);
        return () -> data;
    }

    @Override
    public boolean isPlayerInfo(@NotNull Packet<?> packet) {
        return packet instanceof ClientboundPlayerInfoPacket;
    }

    @Override
    @SneakyThrows
    public void onPlayerInfo(@NotNull TabPlayer receiver, @NotNull Object packet) {
        Enum action = (Enum) ReflectionUtils.getFields(packet.getClass(), tablistActionClass).get(0).get(packet);
        List<PlayerUpdate> players = (List<PlayerUpdate>) ReflectionUtils.getFields(packet.getClass(), List.class).get(0).get(packet);
        for (PlayerUpdate nmsData : players) {
            GameProfile profile = nmsData.getProfile();
            Field displayNameField = ReflectionUtils.getFields(PlayerUpdate.class, Component.class).get(0);
            Field latencyField = ReflectionUtils.getFields(PlayerUpdate.class, int.class).get(0);
            if (action.name().equals(TabList.Action.UPDATE_DISPLAY_NAME.name()) || action.name().equals(TabList.Action.ADD_PLAYER.name())) {
                Object expectedName = ((FabricTabPlayer)receiver).getTabList().getExpectedDisplayName(profile.getId());
                if (expectedName != null) displayNameField.set(nmsData, expectedName);
            }
            if (action.name().equals(TabList.Action.UPDATE_LATENCY.name()) || action.name().equals(TabList.Action.ADD_PLAYER.name())) {
                latencyField.set(nmsData, TAB.getInstance().getFeatureManager().onLatencyChange(receiver, profile.getId(), latencyField.getInt(nmsData)));
            }
            if (action.name().equals(TabList.Action.ADD_PLAYER.name())) {
                TAB.getInstance().getFeatureManager().onEntryAdd(receiver, profile.getId(), profile.getName());
            }
        }
    }

    @Override
    @NotNull
    @SneakyThrows
    public Packet<?> buildTabListPacket(TabList.@NotNull Action action, @NotNull FabricTabList.Builder entry) {
        ClientboundPlayerInfoPacket packet = createInfoPacket(serverVersion, action);
        ReflectionUtils.getFields(ClientboundPlayerInfoPacket.class, List.class).get(0).set(packet, Collections.singletonList(createUpdate(serverVersion, entry)));
        return packet;
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

    @Override
    public boolean isBundlePacket(@NotNull Packet<?> packet) {
        return false;
    }

    @Override
    @NotNull
    public Iterable<Object> getBundledPackets(@NotNull Packet<?> bundlePacket) {
        return Collections.emptyList();
    }

    @Override
    public void sendPackets(@NotNull ServerPlayer player, @NotNull Iterable<Packet<ClientGamePacketListener>> packets) {
        for (Packet<?> packet : packets) {
            player.connection.send(packet);
        }
    }

    @Override
    @NotNull
    public Level getLevel(@NotNull ServerPlayer player) {
        return player.level;
    }

    @Override
    public boolean isSpawnPlayerPacket(@NotNull Packet<?> packet) {
        return packet instanceof ClientboundAddPlayerPacket;
    }

    @Override
    @SneakyThrows
    public int getSpawnedPlayerId(@NotNull Packet<?> packet) {
        return ReflectionUtils.getFields(ClientboundAddPlayerPacket.class, int.class).get(0).getInt(packet);
    }

    @Override
    public int getPing(@NotNull ServerPlayer player) {
        return player.latency;
    }

    @Override
    @SneakyThrows
    public int getDisplaySlot(@NotNull ClientboundSetDisplayObjectivePacket packet) {
        return ReflectionUtils.getFields(packet.getClass(), int.class).get(0).getInt(packet);
    }

    @Override
    @NotNull
    public Packet<?> setDisplaySlot(int slot, @NotNull Objective objective) {
        return new ClientboundSetDisplayObjectivePacket(slot, objective);
    }

    @Override
    @NotNull
    @SneakyThrows
    public Channel getChannel(@NotNull ServerPlayer player) {
        Connection c = (Connection) ReflectionUtils.getFields(ServerGamePacketListenerImpl.class, Connection.class).get(0).get(player.connection);
        return (Channel) ReflectionUtils.getFields(Connection.class, Channel.class).get(0).get(c);
    }

    @Override
    public float getMSPT(@NotNull MinecraftServer server) {
        return server.getAverageTickTime();
    }

    @Override
    @NotNull
    public Packet<?> removeScore(@NotNull String objective, @NotNull String holder) {
        return new ClientboundSetScorePacket(ServerScoreboard.Method.REMOVE, objective, holder, 0);
    }

    @Override
    @SneakyThrows
    public int[] getDestroyedEntities(Packet<?> destroyPacket) {
        return (int[]) ReflectionUtils.getOnlyField(destroyPacket.getClass()).get(destroyPacket);
    }

    @Override
    @NotNull
    public Objective newObjective(@NotNull String name, @NotNull Component displayName,
                                  @NotNull RenderType renderType, @Nullable Component numberFormat) {
        return new Objective(dummyScoreboard, name, ObjectiveCriteria.DUMMY, displayName, renderType);
    }

    @Override
    @NotNull
    public Packet<?> setScore(@NotNull String objective, @NotNull String holder, int score, @Nullable Component displayName, @Nullable Component numberFormat) {
        return new ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, objective, holder, score);
    }

    @Override
    public void setStyle(@NotNull Component component, @NotNull Style style) {
        component.setStyle(style);
    }

    @Override
    @SneakyThrows
    public void logInfo(@NotNull TabComponent message) {
        Object logger = ReflectionUtils.getFields(MinecraftServer.class, Class.forName("org.apache.logging.log4j.Logger")).get(0).get(null);
        logger.getClass().getMethod("info", String.class).invoke(logger, "[TAB] " + message.toLegacyText());
    }

    @Override
    @SneakyThrows
    public void logWarn(@NotNull TabComponent message) {
        Object logger = ReflectionUtils.getFields(MinecraftServer.class, Class.forName("org.apache.logging.log4j.Logger")).get(0).get(null);
        logger.getClass().getMethod("warn", String.class).invoke(logger, "[TAB] " + message.toLegacyText());
    }
}
