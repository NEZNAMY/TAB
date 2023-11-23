package me.neznamy.tab.platforms.fabric.v1_19_2;

import com.mojang.authlib.properties.Property;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.fabric.*;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FabricTAB implements VersionLoader {

    @Override
    public List<String> getSupportedVersions() {
        return List.of("1.19", "1.19.1", "1.19.2");
    }

    @Override
    public String getName(Level level) {
        String path = level.dimension().location().getPath();
        String dimensionSuffix = switch (path) {
            case "overworld" -> ""; // No suffix for overworld
            case "the_nether" -> "_nether";
            default -> "_" + path; // End + default behavior for other dimensions created by mods
        };
        return ((ServerLevelData)level.getLevelData()).getLevelName() + dimensionSuffix;
    }

    @Override
    public int getPing(ServerPlayer player) {
        return player.latency;
    }

    @Override
    public void sendMessage(ServerPlayer player, Component message) {
        player.sendSystemMessage(message);
    }

    @Override
    public TabList.Skin propertyToSkin(Property property) {
        return new TabList.Skin(property.getValue(), property.getSignature());
    }

    @Override
    public void sendMessage(CommandSourceStack sender, Component message) {
        sender.sendSystemMessage(message);
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
                Collections.singletonList(new ClientboundPlayerInfoPacket.PlayerUpdate(
                        entry.createProfile(),
                        entry.getLatency(),
                        GameType.byId(entry.getGameMode()),
                        entry.getDisplayName(),
                        null
                )));
        return packet;
    }

    @Override
    public Level getLevel(ServerPlayer player) {
        return player.level;
    }

    @Override
    public boolean isSneaking(ServerPlayer player) {
        return player.isCrouching();
    }

    @Override
    public EntityData createDataWatcher(TabPlayer viewer, byte flags, String displayName, boolean nameVisible) {
        SynchedEntityData data = new SynchedEntityData(null);
        data.define(new EntityDataAccessor<>(0, EntityDataSerializers.BYTE), flags);
        data.define(new EntityDataAccessor<>(2, EntityDataSerializers.OPTIONAL_COMPONENT),
                Optional.of(((FabricTabPlayer)viewer).getPlatform().toComponent(IChatBaseComponent.optimizedComponent(displayName), viewer.getVersion())));
        data.define(new EntityDataAccessor<>(3, EntityDataSerializers.BOOLEAN), nameVisible);
        data.define(new EntityDataAccessor<>(15, EntityDataSerializers.BYTE), (byte)16);
        return () -> data;
    }

    @Override
    public Packet<?> spawnEntity(int entityId, UUID id, Object entityType, Location location) {
        return new ClientboundAddEntityPacket(entityId, id, location.getX(), location.getY(), location.getZ(),
                0, 0, (EntityType<?>) entityType, 0, Vec3.ZERO, 0);
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
    public int getSpawnedPlayer(Object spawnPacket) {
        return ((ClientboundAddPlayerPacket) spawnPacket).getEntityId();
    }

    @Override
    public int[] getDestroyedEntities(Object entityDestroyPacket) {
        return ((ClientboundRemoveEntitiesPacket) entityDestroyPacket).getEntityIds().toIntArray();
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
        CommandRegistrationCallback.EVENT.register((dispatcher, $, $$) -> new FabricTabCommand().onRegisterCommands(dispatcher));
    }

    @Override
    public Packet<?> newHeaderFooter(Component header, Component footer) {
        return new ClientboundTabListPacket(header, footer);
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
        return ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
    }

    @Override
    public Packet<?> unregisterTeam(PlayerTeam team) {
        return ClientboundSetPlayerTeamPacket.createRemovePacket(team);
    }

    @Override
    public Packet<?> updateTeam(PlayerTeam team) {
        return ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, false);
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
