package me.neznamy.tab.platforms.bukkit.nms;

import lombok.SneakyThrows;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Class for sending packets to players.
 */
public class PacketSender {

    /** Method for getting player's NMS object */
    @NotNull
    private final Method getHandle;

    /** Player connection field in player class */
    @NotNull
    private final Field PLAYER_CONNECTION;

    /** Method for sending packet */
    @NotNull
    private final Method sendPacket;

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
        Class<?> PlayerConnection = BukkitReflection.getClass("server.network.ServerGamePacketListenerImpl",
                "server.network.PlayerConnection", "PlayerConnection");
        getHandle = BukkitReflection.getBukkitClass("entity.CraftPlayer").getMethod("getHandle");
        PLAYER_CONNECTION = ReflectionUtils.getOnlyField(EntityPlayer, PlayerConnection);
        if (BukkitReflection.getMinorVersion() >= 7) {
            sendPacket = ReflectionUtils.getMethods(PlayerConnection, void.class, Packet).get(0);
        } else {
            sendPacket = ReflectionUtils.getMethod(PlayerConnection, new String[]{"sendPacket"}, Packet);
        }
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
    public void sendPacket(@NotNull Player player, @NotNull Object packet) {
        sendPacket.invoke(PLAYER_CONNECTION.get(getHandle.invoke(player)), packet);
    }
}
