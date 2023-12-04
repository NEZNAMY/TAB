package me.neznamy.tab.platforms.bukkit.nms;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitPipelineInjector;
import me.neznamy.tab.platforms.bukkit.BukkitTabList;
import me.neznamy.tab.platforms.bukkit.header.HeaderFooter;
import me.neznamy.tab.platforms.bukkit.scoreboard.ScoreboardLoader;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Class holding all NMS classes, methods, fields and constructors used by TAB.
 */
public class NMSStorage {

    /** Instance of this class */
    @Getter @Setter private static NMSStorage instance;

    // Ping field for 1.5.2 - 1.16.5, 1.17+ has Player#getPing()
    public Field PING;
    public Field PLAYER_CONNECTION;
    public Method getHandle;
    public Method sendPacket;

    /**
     * Constructs new instance and attempts to load all fields, methods and constructors.
     */
    @SneakyThrows
    public NMSStorage() {
        int minorVersion = BukkitReflection.getMinorVersion();
        ProtocolVersion.UNKNOWN_SERVER_VERSION.setMinorVersion(minorVersion); //fixing compatibility with forks that set version field value to "Unknown"
        Class<?> Packet = BukkitReflection.getClass("network.protocol.Packet", "Packet");
        Class<?> EntityPlayer = BukkitReflection.getClass("server.level.ServerPlayer", "server.level.EntityPlayer", "EntityPlayer");
        Class<?> PlayerConnection = BukkitReflection.getClass("server.network.ServerGamePacketListenerImpl",
                "server.network.PlayerConnection", "PlayerConnection");
        if (minorVersion >= 7) {
            sendPacket = ReflectionUtils.getMethods(PlayerConnection, void.class, Packet).get(0);
        } else {
            sendPacket = ReflectionUtils.getMethod(PlayerConnection, new String[]{"sendPacket"}, Packet);
        }
        if (minorVersion >= 8) {
            BukkitPipelineInjector.tryLoad();
            BukkitTabList.load();
        }
        PLAYER_CONNECTION = ReflectionUtils.getOnlyField(EntityPlayer, PlayerConnection);
        getHandle = Class.forName("org.bukkit.craftbukkit." + BukkitReflection.getServerPackage() + ".entity.CraftPlayer").getMethod("getHandle");
        DataWatcher.load();
        PacketEntityView.load();
        ScoreboardLoader.findInstance();
        HeaderFooter.findInstance();
        if (minorVersion < 17) {
            PING = ReflectionUtils.getField(EntityPlayer, "ping", "field_71138_i"); // 1.5.2 - 1.16.5, 1.7.10 Thermos
        }
    }
}