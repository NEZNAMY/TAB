package me.neznamy.tab.platforms.fabric;

import com.mojang.authlib.properties.Property;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface VersionLoader {

    Scoreboard dummyScoreboard = new Scoreboard();
    ArmorStand dummyEntity = new ArmorStand(null, 0, 0, 0);

    String getName(Level level);

    int getPing(ServerPlayer player);

    void sendMessage(ServerPlayer player, Component message);

    TabList.Skin propertyToSkin(Property property);

    void sendMessage(CommandSourceStack sender, Component message);

    String getServerVersion();

    double getMSPT();

    Component deserialize(String string);

    Packet<?> build(TabList.Action action, FabricTabList.Builder entry);

    Level getLevel(ServerPlayer player);

    boolean isSneaking(ServerPlayer player);

    EntityData createDataWatcher(TabPlayer viewer, byte flags, String displayName, boolean nameVisible);

    Packet<?> spawnEntity(int entityId, UUID id, Object entityType, Location location);

    Packet<?> newEntityMetadata(int entityId, EntityData data);

    boolean isSpawnPlayerPacket(Object packet);

    int getSpawnedPlayer(Object spawnPacket);

    int[] getDestroyedEntities(Object entityDestroyPacket);

    boolean isBundlePacket(Object packet);

    Iterable<Object> getPackets(Object bundlePacket);

    PipelineInjector createPipelineInjector();

    void destroyEntities(FabricTabPlayer player, int... entities);

    void registerCommandCallback();

    List<String> getSupportedVersions();

    Packet<?> newHeaderFooter(Component header, Component footer);

    Packet<?> setDisplaySlot(int slot, Objective objective);

    Objective newObjective(String name, Component displayName, RenderType renderType, @Nullable Component numberFormat);

    Packet<?> registerTeam(PlayerTeam team);

    Packet<?> unregisterTeam(PlayerTeam team);

    Packet<?> updateTeam(PlayerTeam team);

    Packet<?> setScore(String objective, String scoreHolder, int score, Component displayName, @Nullable Component numberFormat);

    Packet<?> removeScore(String objective, String scoreHolder);
}
