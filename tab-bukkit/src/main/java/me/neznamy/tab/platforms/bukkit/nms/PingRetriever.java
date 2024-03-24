package me.neznamy.tab.platforms.bukkit.nms;

import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitUtils;
import me.neznamy.tab.shared.util.FunctionWithException;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Class for retrieving ping of players.
 */
public class PingRetriever {

    /** Ping getter function */
    private static FunctionWithException<Player, Integer> getPing;

    /**
     * Attempts to load required classes, fields and methods and marks class as available.
     * If something fails, error message is printed and class is not marked as available.
     */
    public static void tryLoad() {
        try {
            if (BukkitReflection.getMinorVersion() >= 17) {
                getPing = Player::getPing;
            } else {
                Method getHandle = BukkitReflection.getBukkitClass("entity.CraftPlayer").getMethod("getHandle");
                Class<?> EntityPlayer = BukkitReflection.getClass("server.level.ServerPlayer", "server.level.EntityPlayer", "EntityPlayer");
                Field PING = ReflectionUtils.getField(EntityPlayer, "ping", "field_71138_i"); // 1.5.2 - 1.16.5, 1.7.10 Thermos
                getPing = player -> PING.getInt(getHandle.invoke(player));
            }
        } catch (Exception e) {
            getPing = p -> -1;
            BukkitUtils.compatibilityError(e, "getting player's ping", null, "%ping% returning -1");
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
        return getPing.apply(player);
    }
}
