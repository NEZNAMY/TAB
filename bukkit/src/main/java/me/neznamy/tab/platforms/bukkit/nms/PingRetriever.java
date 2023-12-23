package me.neznamy.tab.platforms.bukkit.nms;

import lombok.SneakyThrows;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Class for retrieving ping of players.
 */
public class PingRetriever {

    /** First version with proper ping getter */
    private static final int PING_GETTER_VERSION = 17;

    private static Method getHandle;
    private static Field PING;

    /**
     * Attempts to load required classes, fields and methods and marks class as available.
     * If something fails, error message is printed and class is not marked as available.
     */
    public static void tryLoad() {
        try {
            if (BukkitReflection.getMinorVersion() < PING_GETTER_VERSION) {
                getHandle = BukkitReflection.getBukkitClass("entity.CraftPlayer").getMethod("getHandle");
                Class<?> EntityPlayer = BukkitReflection.getClass("server.level.ServerPlayer", "server.level.EntityPlayer", "EntityPlayer");
                PING = ReflectionUtils.getField(EntityPlayer, "ping", "field_71138_i"); // 1.5.2 - 1.16.5, 1.7.10 Thermos
            }
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(EnumChatFormat.RED.getFormat() + "[TAB] Failed to initialize NMS fields for " +
                    "getting player's ping due to a compatibility error. This will " +
                    "result in ping showing \"-1\". " +
                    "Please update the plugin a to version with native support for your server version to properly show ping.");
        }
    }

    /**
     * Returns player's ping. If ping getter is not available and fields
     * failed to load, returns {@code -1}. If an exception was throws by
     * reflective operation, it is re-thrown.
     *
     * @param   player
     *          Player to get ping of
     * @return  Player's ping or {@code -1} if it failed
     */
    @SneakyThrows
    public static int getPing(@NotNull Player player) {
        if (BukkitReflection.getMinorVersion() >= PING_GETTER_VERSION) {
            return player.getPing();
        }
        if (PING == null) return -1;
        return PING.getInt(getHandle.invoke(player));
    }
}
