package me.neznamy.tab.platforms.bukkit.nms;

import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabList;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherHelper;
import me.neznamy.tab.platforms.bukkit.scoreboard.PacketScoreboard;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.util.ComponentCache;
import me.neznamy.tab.shared.util.ReflectionUtils;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherItem;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherObject;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Class holding all NMS classes, methods, fields and constructors used by TAB.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class NMSStorage {

    /** Instance of this class */
    @Getter @Setter private static NMSStorage instance;

    /** Server's NMS/CraftBukkit package */
    @Getter private final String serverPackage = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    /** Server's minor version */
    @Getter private final int minorVersion = Integer.parseInt(serverPackage.split("_")[1]);

    /** Flag determining whether the server version is at least 1.19.3 or not */
    @Getter private final boolean is1_19_3Plus = ReflectionUtils.classExists("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket");
    @Getter private final boolean is1_19_4Plus = is1_19_3Plus && !serverPackage.equals("v1_19_R2");

    /** Flag determining whether this server jar is mojang mapped or not */
    @Getter private final boolean mojangMapped = ReflectionUtils.classExists("net.minecraft.ChatFormatting") && minorVersion >= 17;

    /** Basic universal values */
    protected Class<?> Packet;
    protected Class<?> NetworkManager;
    public Class<Enum> EnumChatFormat;
    public Class<?> EntityPlayer;
    protected Class<?> EntityHuman;
    protected Class<?> PlayerConnection;

    // Ping field for 1.5.2 - 1.16.5, 1.17+ has Player#getPing()
    public Field PING;

    public Field PLAYER_CONNECTION;
    public Field NETWORK_MANAGER;
    public Field CHANNEL;
    public Method getHandle;
    public Method sendPacket;
    public Method getProfile;

    /** Chat components */
    public Class<?> IChatBaseComponent;
    protected Class<?> ChatSerializer;
    public Method ChatSerializer_DESERIALIZE;

    private final ComponentCache<IChatBaseComponent, Object> componentCache = new ComponentCache<>(1000,
            (component, clientVersion) -> ChatSerializer_DESERIALIZE.invoke(null, component.toString(clientVersion)));

    /**
     * Constructs new instance and attempts to load all fields, methods and constructors.
     */
    @SneakyThrows
    public NMSStorage() {
        ProtocolVersion.UNKNOWN_SERVER_VERSION.setMinorVersion(minorVersion); //fixing compatibility with forks that set version field value to "Unknown"
        loadClasses();
        if (minorVersion >= 7) {
            NETWORK_MANAGER = ReflectionUtils.getOnlyField(PlayerConnection, NetworkManager);
            sendPacket = ReflectionUtils.getMethods(PlayerConnection, void.class, Packet).get(0);
        } else {
            sendPacket = ReflectionUtils.getMethod(PlayerConnection, new String[]{"sendPacket"}, Packet);
        }
        if (minorVersion >= 8) {
            ChatSerializer_DESERIALIZE = ReflectionUtils.getMethod(ChatSerializer, new String[]{"fromJson", "a"}, String.class);
            CHANNEL = ReflectionUtils.getOnlyField(NetworkManager, Channel.class);
            try {
                getProfile = ReflectionUtils.getOnlyMethod(EntityHuman, GameProfile.class);
            } catch (IllegalStateException catServer) {
                getProfile = ReflectionUtils.getMethod(EntityHuman, new String[] {"getProfile"});
            }
        }
        if (minorVersion >= 9) {
            DataWatcherObject.load();
        }
        PLAYER_CONNECTION = ReflectionUtils.getOnlyField(EntityPlayer, PlayerConnection);
        getHandle = Class.forName("org.bukkit.craftbukkit." + serverPackage + ".entity.CraftPlayer").getMethod("getHandle");
        BukkitTabList.load(this);
        DataWatcher.load(this);
        DataWatcherItem.load(this);
        DataWatcherHelper.load(this);
        PacketEntityView.load(this);
        PacketScoreboard.load(this);
        if (minorVersion < 17) {
            try {
                (PING = EntityPlayer.getDeclaredField("ping")).setAccessible(true); // 1.5.2 - 1.16.5
            } catch (NoSuchFieldException e) {
                (PING = EntityPlayer.getDeclaredField("field_71138_i")).setAccessible(true); // 1.7.10 Thermos
            }
        }
    }

    private void loadClasses() throws ClassNotFoundException {
        if (mojangMapped) {
            IChatBaseComponent = Class.forName("net.minecraft.network.chat.Component");
            ChatSerializer = Class.forName("net.minecraft.network.chat.Component$Serializer");
            EntityHuman = Class.forName("net.minecraft.world.entity.player.Player");
            NetworkManager = Class.forName("net.minecraft.network.Connection");
            Packet = Class.forName("net.minecraft.network.protocol.Packet");
            EnumChatFormat = (Class<Enum>) Class.forName("net.minecraft.ChatFormatting");
            EntityPlayer = Class.forName("net.minecraft.server.level.ServerPlayer");
            PlayerConnection = Class.forName("net.minecraft.server.network.ServerGamePacketListenerImpl");
            DataWatcher.CLASS = Class.forName("net.minecraft.network.syncher.SynchedEntityData");
            DataWatcherItem.CLASS = Class.forName("net.minecraft.network.syncher.SynchedEntityData$DataItem");
            DataWatcherObject.CLASS = Class.forName("net.minecraft.network.syncher.EntityDataAccessor");
            DataWatcherHelper.DataWatcherRegistry = Class.forName("net.minecraft.network.syncher.EntityDataSerializers");
            DataWatcherHelper.DataWatcherSerializer = Class.forName("net.minecraft.network.syncher.EntityDataSerializer");
        } else if (minorVersion >= 17) {
            ChatSerializer = Class.forName("net.minecraft.network.chat.IChatBaseComponent$ChatSerializer");
            EntityHuman = Class.forName("net.minecraft.world.entity.player.EntityHuman");
            NetworkManager = Class.forName("net.minecraft.network.NetworkManager");
            IChatBaseComponent = Class.forName("net.minecraft.network.chat.IChatBaseComponent");
            Packet = Class.forName("net.minecraft.network.protocol.Packet");
            EnumChatFormat = (Class<Enum>) Class.forName("net.minecraft.EnumChatFormat");
            EntityPlayer = Class.forName("net.minecraft.server.level.EntityPlayer");
            PlayerConnection = Class.forName("net.minecraft.server.network.PlayerConnection");
            DataWatcher.CLASS = Class.forName("net.minecraft.network.syncher.DataWatcher");
            DataWatcherItem.CLASS = Class.forName("net.minecraft.network.syncher.DataWatcher$Item");
            DataWatcherObject.CLASS = Class.forName("net.minecraft.network.syncher.DataWatcherObject");
            DataWatcherHelper.DataWatcherRegistry = Class.forName("net.minecraft.network.syncher.DataWatcherRegistry");
            DataWatcherHelper.DataWatcherSerializer = Class.forName("net.minecraft.network.syncher.DataWatcherSerializer");
        } else {
            EntityHuman = getLegacyClass("EntityHuman");
            Packet = getLegacyClass("Packet");
            EnumChatFormat = (Class<Enum>) getLegacyClass("EnumChatFormat");
            EntityPlayer = getLegacyClass("EntityPlayer");
            PlayerConnection = getLegacyClass("PlayerConnection");
            NetworkManager = getLegacyClass("NetworkManager");
            DataWatcher.CLASS = getLegacyClass("DataWatcher");
            DataWatcherItem.CLASS = getLegacyClass("DataWatcher$Item", "DataWatcher$WatchableObject", "WatchableObject");
            if (minorVersion >= 7) {
                IChatBaseComponent = getLegacyClass("IChatBaseComponent");
                ChatSerializer = getLegacyClass("IChatBaseComponent$ChatSerializer", "ChatSerializer");
            }
            if (minorVersion >= 9) {
                DataWatcherObject.CLASS = getLegacyClass("DataWatcherObject");
                DataWatcherHelper.DataWatcherRegistry = getLegacyClass("DataWatcherRegistry");
                DataWatcherHelper.DataWatcherSerializer = getLegacyClass("DataWatcherSerializer");
            }
        }
    }

    /**
     * Converts TAB's IChatBaseComponent into minecraft's component using String deserialization.
     * If the requested component is found in cache, it is returned. If not, it is created, added into cache and returned.
     * If {@code component} is {@code null}, returns {@code null}
     *
     * @param   component
     *          component to convert
     * @param   clientVersion
     *          client version used to decide RGB conversion
     * @return  converted component or {@code null} if {@code component} is {@code null}
     */
    public @Nullable Object toNMSComponent(@NotNull IChatBaseComponent component, @NotNull ProtocolVersion clientVersion) {
        return componentCache.get(component, clientVersion);
    }

    /**
     * Returns class with given potential names in same order
     *
     * @param   names
     *          possible class names
     * @return  class for specified names
     * @throws  ClassNotFoundException
     *          if class does not exist
     */
    public Class<?> getLegacyClass(@NotNull String... names) throws ClassNotFoundException {
        for (String name : names) {
            try {
                return getLegacyClass(name);
            } catch (ClassNotFoundException e) {
                //not the first class name in array
            }
        }
        throw new ClassNotFoundException("No class found with possible names " + Arrays.toString(names));
    }

    /**
     * Returns class from given name. Supports modded servers, such as Thermos.
     *
     * @param   name
     *          class name
     * @return  class from given name
     * @throws  ClassNotFoundException
     *          if class was not found
     */
    public Class<?> getLegacyClass(@NotNull String name) throws ClassNotFoundException {
        try {
            return getClass().getClassLoader().loadClass("net.minecraft.server." + serverPackage + "." + name);
        } catch (NullPointerException e) {
            // nested class not found
            throw new ClassNotFoundException(name);
        }
    }
}