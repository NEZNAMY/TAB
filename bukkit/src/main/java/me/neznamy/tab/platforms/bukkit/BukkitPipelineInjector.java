package me.neznamy.tab.platforms.bukkit;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
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

    /**
     * Attempts to load required classes, fields and methods and marks class as available.
     * If something fails, error message is printed and class is not marked as available.
     */
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
            BukkitUtils.compatibilityError(e, "network channel injection", null,
                    "Unlimited nametag mode not working and being replaced with regular nametags",
                    "Anti-override for tablist & nametags not working",
                    "Compatibility with nickname plugins changing player names will not work",
                    "Scoreboard will not be checking for other plugins");
        }
    }

    @Override
    @Nullable
    @SneakyThrows
    protected Channel getChannel(@NotNull TabPlayer player) {
        return (Channel) CHANNEL.get(NETWORK_MANAGER.get(PLAYER_CONNECTION.get(getHandle.invoke(player.getPlayer()))));
    }

    @Override
    public boolean isLogin(@NotNull Object packet) {
        return false;
    }
}