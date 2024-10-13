package me.neznamy.tab.platforms.bukkit.nms;

import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.util.BiConsumerWithException;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Class for sending packets to players.
 */
public class PacketSender {

    /** Function for sending packet */
    @NotNull
    private final BiConsumerWithException<BukkitTabPlayer, Object> send;

    /**
     * Constructs new instance and attempts to load required classes, fields and methods.
     * If something fails, error is thrown.
     *
     * @throws  ReflectiveOperationException
     *          If something fails
     */
    public PacketSender() throws ReflectiveOperationException {
        Class<?> Packet = BukkitReflection.getClass("network.protocol.Packet", "Packet");
        Class<?> EntityPlayer = BukkitReflection.getClass("server.level.ServerPlayer", "server.level.EntityPlayer", "EntityPlayer");
        Class<?> PlayerConnection = BukkitReflection.getClass("server.network.ServerGamePacketListenerImpl", "server.network.PlayerConnection", "PlayerConnection");
        Field PLAYER_CONNECTION = ReflectionUtils.getOnlyField(EntityPlayer, PlayerConnection);
        Method sendPacket;
        if (BukkitReflection.getMinorVersion() >= 7) {
            sendPacket = ReflectionUtils.getMethods(PlayerConnection, void.class, Packet).get(0);
        } else {
            sendPacket = ReflectionUtils.getMethod(PlayerConnection, new String[]{"sendPacket"}, Packet);
        }
        send = (player, packet) -> {
            if (player.connection == null) player.connection = PLAYER_CONNECTION.get(player.getHandle());
            sendPacket.invoke(player.connection, packet);
        };
    }

    /**
     * Sends packet to specified player.
     * If something goes wrong, throws an exception.
     *
     * @param   player
     *          Player to send packet to
     * @param   packet
     *          Packet to send
     */
    @SneakyThrows
    public void sendPacket(@NotNull BukkitTabPlayer player, @NotNull Object packet) {
        send.accept(player, packet);
    }
}
