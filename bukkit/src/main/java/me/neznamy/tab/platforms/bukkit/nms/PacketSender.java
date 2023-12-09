package me.neznamy.tab.platforms.bukkit.nms;

import lombok.Getter;
import lombok.SneakyThrows;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PacketSender {

    private static Method getHandle;
    private static Field PLAYER_CONNECTION;
    private static Method sendPacket;
    @Getter private static boolean available;

    public static void tryLoad() {
        try {
            Class<?> Packet = BukkitReflection.getClass("network.protocol.Packet", "Packet");
            Class<?> EntityPlayer = BukkitReflection.getClass("server.level.ServerPlayer", "server.level.EntityPlayer", "EntityPlayer");
            Class<?> PlayerConnection = BukkitReflection.getClass("server.network.ServerGamePacketListenerImpl",
                    "server.network.PlayerConnection", "PlayerConnection");
            getHandle = BukkitReflection.getBukkitClass("entity.CraftPlayer").getMethod("getHandle");
            PLAYER_CONNECTION = ReflectionUtils.getOnlyField(EntityPlayer, PlayerConnection);
            if (BukkitReflection.getMinorVersion() >= 7) {
                sendPacket = ReflectionUtils.getMethods(PlayerConnection, void.class, Packet).get(0);
            } else {
                sendPacket = ReflectionUtils.getMethod(PlayerConnection, new String[]{"sendPacket"}, Packet);
            }
            available = true;
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(EnumChatFormat.RED.getFormat() + "[TAB] Failed to initialize NMS fields for " +
                    "sending packets due to a compatibility error. Majority of the features will not work / be very limited. " +
                    "Please update the plugin to a version with proper support for your server version.");
        }
    }

    @SneakyThrows
    public static void sendPacket(@NotNull Player player, @NotNull Object packet) {
        if (!player.isOnline() || sendPacket == null) return;
        sendPacket.invoke(PLAYER_CONNECTION.get(getHandle.invoke(player)), packet);
    }
}
