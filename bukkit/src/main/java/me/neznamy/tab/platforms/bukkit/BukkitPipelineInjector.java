package me.neznamy.tab.platforms.bukkit;

import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.scoreboard.PacketScoreboard;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Pipeline injection for bukkit
 */
public class BukkitPipelineInjector extends NettyPipelineInjector {

    private static Field NETWORK_MANAGER;
    private static Field CHANNEL;

    @Getter
    private static boolean available;

    /**
     * Constructs new instance
     */
    public BukkitPipelineInjector() {
        super("packet_handler");
    }

    public static void tryLoad() {
        if (BukkitReflection.getMinorVersion() < 8) return;
        try {
            Class<?> NetworkManager = BukkitReflection.getClass("network.Connection", "network.NetworkManager", "NetworkManager");
            Class<?> PlayerConnection = BukkitReflection.getClass("server.network.ServerGamePacketListenerImpl",
                    "server.network.PlayerConnection", "PlayerConnection");
            if (BukkitReflection.is1_20_2Plus()) {
                NETWORK_MANAGER = ReflectionUtils.getOnlyField(PlayerConnection.getSuperclass(), NetworkManager);
            } else {
                NETWORK_MANAGER = ReflectionUtils.getOnlyField(PlayerConnection, NetworkManager);
            }
            CHANNEL = ReflectionUtils.getOnlyField(NetworkManager, Channel.class);
            available = true;
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(EnumChatFormat.RED.getFormat() + "[TAB] Failed to initialize NMS fields for " +
                    "network channel injection due to a compatibility error. This will make the following features not work: " +
                    "Unlimited nametag mode, anti-override for tablist formatting & nametags, detecting nickname change for compatibility " +
                    "with nick plugins and compatibility with other scoreboard plugins. " +
                    "Please update the plugin a to version with native support for your server version to unlock the features.");
        }
    }

    @Override
    @Nullable
    @SneakyThrows
    protected Channel getChannel(@NotNull TabPlayer player) {
        return (Channel) CHANNEL.get(NETWORK_MANAGER.get(((BukkitTabPlayer) player).getPlayerConnection()));
    }

    @Override
    @SneakyThrows
    public void onDisplayObjective(@NotNull TabPlayer player, @NotNull Object packet) {
        if (!PacketScoreboard.isAvailable()) return;
        int position;
        if (BukkitReflection.is1_20_2Plus()) {
            position = ((Enum<?>)PacketScoreboard.displayPacketData.DisplayObjective_POSITION.get(packet)).ordinal();
        } else {
            position = PacketScoreboard.displayPacketData.DisplayObjective_POSITION.getInt(packet);
        }
        TAB.getInstance().getFeatureManager().onDisplayObjective(player, position,
                (String) PacketScoreboard.displayPacketData.DisplayObjective_OBJECTIVE_NAME.get(packet));
    }

    @Override
    @SneakyThrows
    public void onObjective(@NotNull TabPlayer player, @NotNull Object packet) {
        if (!PacketScoreboard.isAvailable()) return;
        TAB.getInstance().getFeatureManager().onObjective(player,
                PacketScoreboard.Objective_METHOD.getInt(packet),
                (String) PacketScoreboard.Objective_OBJECTIVE_NAME.get(packet));
    }

    @Override
    public boolean isDisplayObjective(@NotNull Object packet) {
        if (!PacketScoreboard.isAvailable()) return false;
        return PacketScoreboard.displayPacketData.DisplayObjectiveClass.isInstance(packet);
    }

    @Override
    public boolean isObjective(@NotNull Object packet) {
        if (!PacketScoreboard.isAvailable()) return false;
        return PacketScoreboard.ObjectivePacketClass.isInstance(packet);
    }

    @Override
    public boolean isTeam(@NotNull Object packet) {
        if (!PacketScoreboard.isAvailable()) return false;
        return PacketScoreboard.teamPacketData.TeamPacketClass.isInstance(packet);
    }

    @Override
    public boolean isPlayerInfo(@NotNull Object packet) {
        return BukkitTabList.PlayerInfoClass.isInstance(packet);
    }

    @Override
    public void onPlayerInfo(@NotNull TabPlayer receiver, @NotNull Object packet) {
        if (BukkitReflection.is1_19_3Plus()) {
            onPlayerInfo1_19_3(receiver, packet);
        } else {
            onPlayerInfo1_19_2(receiver, packet);
        }
    }

    @SneakyThrows
    private void onPlayerInfo1_19_3(@NotNull TabPlayer receiver, @NotNull Object packet) {
        List<String> actions = ((EnumSet<?>)BukkitTabList.ACTION.get(packet)).stream().map(Enum::name).collect(Collectors.toList());
        List<Object> updatedList = new ArrayList<>();
        for (Object nmsData : (List<?>) BukkitTabList.PLAYERS.get(packet)) {
            GameProfile profile = (GameProfile) BukkitTabList.PlayerInfoData_Profile.get(nmsData);
            UUID id;
            id = (UUID) BukkitTabList.PlayerInfoData_UUID.get(nmsData);
            Object displayName = null;
            int latency = 0;
            if (actions.contains(TabList.Action.UPDATE_DISPLAY_NAME.name())) {
                displayName = BukkitTabList.PlayerInfoData_DisplayName.get(nmsData);
                IChatBaseComponent newDisplayName = TAB.getInstance().getFeatureManager().onDisplayNameChange(receiver, id);
                if (newDisplayName != null) displayName = ((BukkitTabPlayer)receiver).getPlatform().toComponent(newDisplayName, receiver.getVersion());
            }
            if (actions.contains(TabList.Action.UPDATE_LATENCY.name())) {
                latency = TAB.getInstance().getFeatureManager().onLatencyChange(receiver, id, BukkitTabList.PlayerInfoData_Latency.getInt(nmsData));
            }
            if (actions.contains(TabList.Action.ADD_PLAYER.name())) {
                TAB.getInstance().getFeatureManager().onEntryAdd(receiver, id, profile.getName());
            }
            // 1.19.3 is using records, which do not allow changing final fields, need to rewrite the list entirely
            updatedList.add(BukkitTabList.newPlayerInfoData.newInstance(
                    id,
                    profile,
                    BukkitTabList.PlayerInfoData_Listed.getBoolean(nmsData),
                    latency,
                    BukkitTabList.PlayerInfoData_GameMode.get(nmsData),
                    displayName,
                    BukkitTabList.PlayerInfoData_RemoteChatSession.get(nmsData)));
        }
        BukkitTabList.PLAYERS.set(packet, updatedList);
    }

    @SneakyThrows
    private void onPlayerInfo1_19_2(@NotNull TabPlayer receiver, @NotNull Object packet) {
        String action = BukkitTabList.ACTION.get(packet).toString();
        for (Object nmsData : (List<?>) BukkitTabList.PLAYERS.get(packet)) {
            GameProfile profile = (GameProfile) BukkitTabList.PlayerInfoData_Profile.get(nmsData);
            UUID id = profile.getId();
            if (action.equals(TabList.Action.UPDATE_DISPLAY_NAME.name()) || action.equals(TabList.Action.ADD_PLAYER.name())) {
                Object displayName = BukkitTabList.PlayerInfoData_DisplayName.get(nmsData);
                IChatBaseComponent newDisplayName = TAB.getInstance().getFeatureManager().onDisplayNameChange(receiver, id);
                if (newDisplayName != null) displayName = ((BukkitTabPlayer)receiver).getPlatform().toComponent(newDisplayName, receiver.getVersion());
                BukkitTabList.PlayerInfoData_DisplayName.set(nmsData, displayName);
            }
            if (action.equals(TabList.Action.UPDATE_LATENCY.name()) || action.equals(TabList.Action.ADD_PLAYER.name())) {
                int latency = TAB.getInstance().getFeatureManager().onLatencyChange(receiver, id, BukkitTabList.PlayerInfoData_Latency.getInt(nmsData));
                BukkitTabList.PlayerInfoData_Latency.set(nmsData, latency);
            }
            if (action.equals(TabList.Action.ADD_PLAYER.name())) {
                TAB.getInstance().getFeatureManager().onEntryAdd(receiver, id, profile.getName());
            }
        }
    }

    @Override
    public boolean isLogin(@NotNull Object packet) {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    @SneakyThrows
    public void modifyPlayers(@NotNull Object packetPlayOutScoreboardTeam) {
        if (!PacketScoreboard.isAvailable()) return;
        if (TAB.getInstance().getNameTagManager() == null) return;
        int action = PacketScoreboard.teamPacketData.TeamPacket_ACTION.getInt(packetPlayOutScoreboardTeam);
        if (action == 1 || action == 2 || action == 4) return;
        Collection<String> players = (Collection<String>) PacketScoreboard.teamPacketData.TeamPacket_PLAYERS.get(packetPlayOutScoreboardTeam);
        String teamName = (String) PacketScoreboard.teamPacketData.TeamPacket_NAME.get(packetPlayOutScoreboardTeam);
        if (players == null) return;
        //creating a new list to prevent NoSuchFieldException in minecraft packet encoder when a player is removed
        Collection<String> newList = new ArrayList<>();
        for (String entry : players) {
            TabPlayer p = getPlayer(entry);
            if (p == null) {
                newList.add(entry);
                continue;
            }
            Sorting sorting = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SORTING);
            String expectedTeam = sorting.getShortTeamName(p);
            if (expectedTeam == null) {
                newList.add(entry);
                continue;
            }
            if (!((NameTag)TAB.getInstance().getNameTagManager()).getDisableChecker().isDisabledPlayer(p) &&
                    !TAB.getInstance().getNameTagManager().hasTeamHandlingPaused(p) && !teamName.equals(expectedTeam)) {
                logTeamOverride(teamName, p.getName(), expectedTeam);
            } else {
                newList.add(entry);
            }
        }
        PacketScoreboard.teamPacketData.TeamPacket_PLAYERS.set(packetPlayOutScoreboardTeam, newList);
    }
}