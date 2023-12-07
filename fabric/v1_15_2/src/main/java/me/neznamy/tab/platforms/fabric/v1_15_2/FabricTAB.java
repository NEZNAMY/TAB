package me.neznamy.tab.platforms.fabric.v1_15_2;

import com.mojang.authlib.properties.Property;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.fabric.*;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

public class FabricTAB implements VersionLoader {

    @Override
    public List<String> getSupportedVersions() {
        return Arrays.asList("1.14.4", "1.15.2");
    }

    @Override
    public String getName(Level level) {
        return level.getLevelData().getLevelName() + level.dimension.getType().getFileSuffix();
    }

    @Override
    public int getPing(ServerPlayer player) {
        return player.latency;
    }

    @Override
    public void sendMessage(ServerPlayer player, Component message) {
        player.sendMessage(message);
    }

    @Override
    public TabList.Skin propertyToSkin(Property property) {
        return new TabList.Skin(property.getValue(), property.getSignature());
    }

    @Override
    public void sendMessage(CommandSourceStack sender, Component message) {
        sender.sendSuccess(message, false);
    }

    @Override
    public String getServerVersion() {
        return SharedConstants.getCurrentVersion().getName();
    }

    @Override
    public double getMSPT() {
        return ((FabricPlatform)TAB.getInstance().getPlatform()).getServer().getAverageTickTime();
    }

    @Override
    public Component deserialize(String string) {
        return Component.Serializer.fromJson(string);
    }

    @Override
    @SneakyThrows
    public Packet<?> build(TabList.Action action, FabricTabList.Builder entry) {
        ClientboundPlayerInfoPacket packet = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.valueOf(action.name()), Collections.emptyList());
        ReflectionUtils.getFields(ClientboundPlayerInfoPacket.class, List.class).get(0).set(packet,
                Collections.singletonList(new ClientboundPlayerInfoPacket().new PlayerUpdate(
                        entry.createProfile(),
                        entry.getLatency(),
                        GameType.byId(entry.getGameMode()),
                        entry.getDisplayName()
                )));
        return packet;
    }

    @Override
    public Level getLevel(ServerPlayer player) {
        return player.level;
    }

    @Override
    @SneakyThrows
    public boolean isSneaking(ServerPlayer player) {
        if (getServerVersion().equals("1.14.4")) {
            return (boolean) player.getClass().getMethod("method_5715").invoke(player);
        }

        // 1.15.2
        return player.isCrouching();
    }

    @Override
    public EntityData createDataWatcher(TabPlayer viewer, byte flags, String displayName, boolean nameVisible) {
        SynchedEntityData data = new SynchedEntityData(null);
        data.define(new EntityDataAccessor<>(0, EntityDataSerializers.BYTE), flags);
        data.define(new EntityDataAccessor<>(2, EntityDataSerializers.OPTIONAL_COMPONENT),
                Optional.of(((FabricTabPlayer)viewer).getPlatform().toComponent(IChatBaseComponent.optimizedComponent(displayName), viewer.getVersion())));
        data.define(new EntityDataAccessor<>(3, EntityDataSerializers.BOOLEAN), nameVisible);
        int markerPosition = viewer.getPlatform().getServerVersion().getMinorVersion() >= 15 ? 14 : 13;
        data.define(new EntityDataAccessor<>(markerPosition, EntityDataSerializers.BYTE), (byte)16);
        return () -> data;
    }

    @Override
    public Packet<?> spawnEntity(int entityId, UUID id, Object entityType, Location location) {
        dummyEntity.setId(entityId);
        dummyEntity.setUUID(id);
        dummyEntity.setPos(location.getX(), location.getY(), location.getZ());
        return new ClientboundAddMobPacket(dummyEntity);
    }

    @Override
    public Packet<?> newEntityMetadata(int entityId, EntityData data) {
        return new ClientboundSetEntityDataPacket(entityId, (SynchedEntityData) data.build(), true);
    }

    @Override
    public boolean isSpawnPlayerPacket(Object packet) {
        return packet instanceof ClientboundAddPlayerPacket;
    }

    @Override
    @SneakyThrows
    public int getSpawnedPlayer(Object spawnPacket) {
        // Getter is client only
        return ReflectionUtils.getFields(ClientboundAddPlayerPacket.class, int.class).get(0).getInt(spawnPacket);
    }

    @Override
    @SneakyThrows
    public int[] getDestroyedEntities(Object entityDestroyPacket) {
        // Getter is client only
        return (int[]) ReflectionUtils.getFields(ClientboundRemoveEntitiesPacket.class, int[].class).get(0).get(entityDestroyPacket);
    }

    @Override
    public boolean isBundlePacket(Object packet) {
        return false;
    }

    @Override
    public Iterable<Object> getPackets(Object bundlePacket) {
        return Collections.emptyList();
    }

    @Override
    public PipelineInjector createPipelineInjector() {
        return new FabricPipelineInjector();
    }

    @Override
    public void destroyEntities(FabricTabPlayer player, int... entities) {
        player.sendPacket(new ClientboundRemoveEntitiesPacket(entities));
    }

    @Override
    public void registerCommandCallback() {
        CommandRegistrationCallback.EVENT.register((dispatcher, $) -> new FabricTabCommand().onRegisterCommands(dispatcher));
    }

    @Override
    @SneakyThrows
    public Packet<?> newHeaderFooter(Component header, Component footer) {
        ClientboundTabListPacket packet = new ClientboundTabListPacket();
        List<Field> fields = ReflectionUtils.getFields(ClientboundTabListPacket.class, Component.class);
        fields.get(0).set(packet, header);
        fields.get(1).set(packet, footer);
        return packet;
    }

    @Override
    public Packet<?> setDisplaySlot(int slot, Objective objective) {
        return new ClientboundSetDisplayObjectivePacket(slot, objective);
    }

    @Override
    public Objective newObjective(String name, Component displayName, ObjectiveCriteria.RenderType renderType,
                                  @Nullable Component numberFormat) {
        return new Objective(dummyScoreboard, name, ObjectiveCriteria.DUMMY, displayName, renderType);
    }

    @Override
    public Packet<?> registerTeam(PlayerTeam team) {
        return new ClientboundSetPlayerTeamPacket(team, Scoreboard.TeamAction.CREATE);
    }

    @Override
    public Packet<?> unregisterTeam(PlayerTeam team) {
        return new ClientboundSetPlayerTeamPacket(team, Scoreboard.TeamAction.REMOVE);
    }

    @Override
    public Packet<?> updateTeam(PlayerTeam team) {
        return new ClientboundSetPlayerTeamPacket(team, Scoreboard.TeamAction.UPDATE);
    }

    @Override
    public Packet<?> setScore(String objective, String scoreHolder, int score, Component displayName, @Nullable Component numberFormat) {
        return new ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, objective, scoreHolder, score);
    }

    @Override
    public Packet<?> removeScore(String objective, String scoreHolder) {
        return new ClientboundSetScorePacket(ServerScoreboard.Method.REMOVE, objective, scoreHolder, 0);
    }
}
