package me.neznamy.tab.platforms.bukkit;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.scoreboard.PacketScoreboard;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Pipeline injection for bukkit
 */
public class BukkitPipelineInjector extends NettyPipelineInjector {

    private static Method getHandle;
    private static Field PLAYER_CONNECTION;
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
        try {
            Class<?> NetworkManager = BukkitReflection.getClass("network.Connection", "network.NetworkManager", "NetworkManager");
            Class<?> PlayerConnection = BukkitReflection.getClass("server.network.ServerGamePacketListenerImpl",
                    "server.network.PlayerConnection", "PlayerConnection");
            Class<?> EntityPlayer = BukkitReflection.getClass("server.level.ServerPlayer", "server.level.EntityPlayer", "EntityPlayer");
            getHandle = BukkitReflection.getBukkitClass("entity.CraftPlayer").getMethod("getHandle");
            PLAYER_CONNECTION = ReflectionUtils.getOnlyField(EntityPlayer, PlayerConnection);
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
        return (Channel) CHANNEL.get(NETWORK_MANAGER.get(PLAYER_CONNECTION.get(getHandle.invoke(player.getPlayer()))));
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