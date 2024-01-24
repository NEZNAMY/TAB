package me.neznamy.tab.platforms.fabric;

import com.mojang.authlib.properties.Property;
import io.netty.channel.Channel;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;

import java.util.Collections;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Class managing cross-version code in shared module.
 */
public class FabricMultiVersion {

    public static Function<Level, String> getLevelName;
    public static Function<Property, TabList.Skin> propertyToSkin;
    public static BiFunction<Integer, EntityData, Packet<ClientGamePacketListener>> newEntityMetadata;
    public static Function<Packet<?>, Boolean> isSpawnPlayerPacket;
    public static Function<ServerPlayer, Boolean> isSneaking;
    public static Function<ServerPlayer, Level> getLevel;
    public static Function<ServerPlayer, Integer> getPing;
    public static Function<MinecraftServer, Float> getMSPT;
    public static Function<String, Component> deserialize;
    public static Function<Packet<?>, Boolean> isBundlePacket = packet -> false;
    public static Function<Packet<?>, Iterable<Object>> getBundledPackets = packet -> Collections.emptyList();
    public static FunctionWithException<ServerPlayer, Channel> getChannel;
    public static BiConsumerWithException<ServerPlayer, Component> sendMessage;
    public static BiConsumer<CommandSourceStack, Component> sendMessage2;
    public static QuintFunction<Level, Integer, UUID, Object, Location, Packet<ClientGamePacketListener>> spawnEntity;
    public static QuadFunction<TabPlayer, Byte, String, Boolean, EntityData> createDataWatcher;
    public static BiFunctionWithException<TabList.Action, FabricTabList.Builder, Packet<?>> buildTabListPacket;
    public static BiFunctionWithException<Component, Component, Packet<?>> newHeaderFooter;
    public static BiConsumerWithException<TabPlayer, Object> onPlayerInfo;
    public static Function<Packet<?>, Boolean> isPlayerInfo;
    public static BiConsumerWithException<ServerPlayer, Iterable<Packet<ClientGamePacketListener>>> sendPackets;
    public static FunctionWithException<Object, int[]> getDestroyedEntities;
    public static BiConsumerWithException<FabricTabPlayer, int[]> destroyEntities;

    public static Function<PlayerTeam, Packet<?>> registerTeam;
    public static Function<PlayerTeam, Packet<?>> unregisterTeam;
    public static Function<PlayerTeam, Packet<?>> updateTeam;
    public static BiFunction<Integer, Objective, Packet<?>> setDisplaySlot;
    public static QuadFunction<String, Component, RenderType, Component, Objective> newObjective;
    public static QuintFunction<String, String, Integer, Component, Component, Packet<?>> setScore;
    public static BiFunction<String, String, Packet<?>> removeScore;
    public static FunctionWithException<ClientboundSetDisplayObjectivePacket, Integer> getDisplaySlot;
    public static Function<Packet<?>, Boolean> isTeamPacket;
}
