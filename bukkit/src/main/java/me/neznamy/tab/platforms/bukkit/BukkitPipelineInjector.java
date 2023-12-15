package me.neznamy.tab.platforms.bukkit;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.scoreboard.PacketScoreboard;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
    public boolean isLogin(@NotNull Object packet) {
        return false;
    }
}