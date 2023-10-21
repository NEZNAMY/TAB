package me.neznamy.tab.platforms.bukkit;

import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.scoreboard.PacketScoreboard;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.features.sorting.Sorting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Pipeline injection for bukkit
 */
public class BukkitPipelineInjector extends NettyPipelineInjector {

    /**
     * Constructs new instance
     */
    public BukkitPipelineInjector() {
        super("packet_handler");
    }

    @Override
    @Nullable
    protected Channel getChannel(@NotNull TabPlayer player) {
        BukkitTabPlayer bukkit = (BukkitTabPlayer) player;
        NMSStorage nms = NMSStorage.getInstance();
        try {
            if (nms.CHANNEL != null) return (Channel) nms.CHANNEL.get(nms.NETWORK_MANAGER.get(bukkit.getPlayerConnection()));
        } catch (IllegalAccessException exception) {
            TAB.getInstance().getErrorManager().printError("Failed to get channel of " + bukkit.getName(), exception);
        }
        return null;
    }

    @Override
    @SneakyThrows
    public void onDisplayObjective(@NotNull TabPlayer player, @NotNull Object packet) {
        int position;
        if (BukkitReflection.is1_20_2Plus()) {
            position = ((Enum<?>)PacketScoreboard.DisplayObjective_POSITION.get(packet)).ordinal();
        } else {
            position = PacketScoreboard.DisplayObjective_POSITION.getInt(packet);
        }
        TAB.getInstance().getFeatureManager().onDisplayObjective(player, position,
                (String) PacketScoreboard.DisplayObjective_OBJECTIVE_NAME.get(packet));
    }

    @Override
    @SneakyThrows
    public void onObjective(@NotNull TabPlayer player, @NotNull Object packet) {
        TAB.getInstance().getFeatureManager().onObjective(player,
                PacketScoreboard.Objective_METHOD.getInt(packet),
                (String) PacketScoreboard.Objective_OBJECTIVE_NAME.get(packet));
    }

    @Override
    public boolean isDisplayObjective(@NotNull Object packet) {
        return PacketScoreboard.DisplayObjectiveClass.isInstance(packet);
    }

    @Override
    public boolean isObjective(@NotNull Object packet) {
        return PacketScoreboard.ObjectivePacketClass.isInstance(packet);
    }

    @Override
    public boolean isTeam(@NotNull Object packet) {
        return PacketScoreboard.TeamPacketClass.isInstance(packet);
    }

    @Override
    public boolean isPlayerInfo(@NotNull Object packet) {
        return BukkitTabList.PlayerInfoClass.isInstance(packet);
    }

    @Override
    @SneakyThrows
    public void onPlayerInfo(@NotNull TabPlayer receiver, @NotNull Object packet) {
        List<String> actions;
        if (BukkitTabList.ClientboundPlayerInfoRemovePacket != null) {
            //1.19.3+
            actions = ((EnumSet<?>)BukkitTabList.ACTION.get(packet)).stream().map(Enum::name).collect(Collectors.toList());
        } else {
            //1.19.2-
            actions = Collections.singletonList(BukkitTabList.ACTION.get(packet).toString());
        }
        List<Object> updatedList = new ArrayList<>();
        for (Object nmsData : (List<?>) BukkitTabList.PLAYERS.get(packet)) {
            GameProfile profile = (GameProfile) BukkitTabList.PlayerInfoData_Profile.get(nmsData);
            UUID id;
            if (BukkitReflection.is1_19_3Plus()) {
                id = (UUID) BukkitTabList.PlayerInfoData_UUID.get(nmsData);
            } else {
                id = profile.getId();
            }
            Object displayName = null;
            int latency = 0;
            if (actions.contains(TabList.Action.UPDATE_DISPLAY_NAME.name()) || actions.contains(TabList.Action.ADD_PLAYER.name())) {
                displayName = BukkitTabList.PlayerInfoData_DisplayName.get(nmsData);
                IChatBaseComponent newDisplayName = TAB.getInstance().getFeatureManager().onDisplayNameChange(receiver, id);
                if (newDisplayName != null) displayName = ((BukkitTabPlayer)receiver).getPlatform().toComponent(newDisplayName, receiver.getVersion());
                if (!BukkitReflection.is1_19_3Plus()) BukkitTabList.PlayerInfoData_DisplayName.set(nmsData, displayName);
            }
            if (actions.contains(TabList.Action.UPDATE_LATENCY.name()) || actions.contains(TabList.Action.ADD_PLAYER.name())) {
                latency = BukkitTabList.PlayerInfoData_Latency.getInt(nmsData);
                latency = TAB.getInstance().getFeatureManager().onLatencyChange(receiver, id, latency);
                if (!BukkitReflection.is1_19_3Plus()) BukkitTabList.PlayerInfoData_Latency.set(nmsData, latency);
            }
            if (actions.contains(TabList.Action.ADD_PLAYER.name())) {
                TAB.getInstance().getFeatureManager().onEntryAdd(receiver, id, profile.getName());
            }
            if (BukkitReflection.is1_19_3Plus()) {
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
        }
        if (BukkitReflection.is1_19_3Plus()) {
            BukkitTabList.PLAYERS.set(packet, updatedList);
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
        if (TAB.getInstance().getNameTagManager() == null) return;
        int action = PacketScoreboard.TeamPacket_ACTION.getInt(packetPlayOutScoreboardTeam);
        if (action == 1 || action == 2 || action == 4) return;
        Collection<String> players = (Collection<String>) PacketScoreboard.TeamPacket_PLAYERS.get(packetPlayOutScoreboardTeam);
        String teamName = (String) PacketScoreboard.TeamPacket_NAME.get(packetPlayOutScoreboardTeam);
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
        PacketScoreboard.TeamPacket_PLAYERS.set(packetPlayOutScoreboardTeam, newList);
    }
}