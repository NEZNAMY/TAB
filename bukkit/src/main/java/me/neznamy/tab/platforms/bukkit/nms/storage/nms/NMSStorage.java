package me.neznamy.tab.platforms.bukkit.nms.storage.nms;

import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.util.ComponentCache;
import me.neznamy.tab.shared.util.ReflectionUtils;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherItem;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherObject;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.*;
import me.neznamy.tab.shared.TAB;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Class holding all NMS classes, methods, fields and constructors used by TAB.
 */
@SuppressWarnings("rawtypes")
public abstract class NMSStorage {

    /** Instance of this class */
    @Getter @Setter private static NMSStorage instance;

    /** Server's NMS/CraftBukkit package */
    @Getter protected final String serverPackage = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    /** Server's minor version */
    @Getter protected final int minorVersion = Integer.parseInt(serverPackage.split("_")[1]);

    /** Flag determining whether the server version is at least 1.19.3 or not */
    @Getter private final boolean is1_19_3Plus = ReflectionUtils.classExists("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket");
    @Getter private final boolean is1_19_4Plus = is1_19_3Plus && !serverPackage.equals("v1_19_R2");
    @Getter private final boolean is1_20Plus = is1_19_4Plus && !serverPackage.equals("v1_20_R1");

    /** Basic universal values */
    protected Class<?> Packet;
    protected Class<?> NetworkManager;
    protected Class<?> EntityArmorStand;
    protected Class<?> World;
    public Class<Enum> EnumChatFormat;
    public Class<?> EntityPlayer;
    protected Class<?> EntityHuman;
    public Class<?> Entity;
    public Class<?> EntityLiving;
    protected Class<?> PlayerConnection;
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

    /** Other entity packets */
    public Class<?> PacketPlayOutEntity;
    public Field PacketPlayOutEntity_ENTITYID;

    public Class<?> PacketPlayOutEntityLook;

    public Class<?> PacketPlayOutNamedEntitySpawn;
    public Field PacketPlayOutNamedEntitySpawn_ENTITYID;

    /** Scoreboard objectives */
    public Class<?> Scoreboard;
    protected Class<?> IScoreboardCriteria;
    public Field IScoreboardCriteria_self;
    public Object emptyScoreboard;

    public Object dummyEntity;

    private final ComponentCache<IChatBaseComponent, Object> componentCache = new ComponentCache<>(10000,
            (component, clientVersion) -> ChatSerializer_DESERIALIZE.invoke(null, component.toString(clientVersion)));

    /**
     * Constructs new instance and attempts to load all fields, methods and constructors.
     *
     * @throws  ReflectiveOperationException
     *          If some field, constructor or method was not found
     */
    public NMSStorage() throws ReflectiveOperationException {
        ProtocolVersion.UNKNOWN_SERVER_VERSION.setMinorVersion(minorVersion); //fixing compatibility with forks that set version field value to "Unknown"
        loadClasses();
        if (minorVersion >= 7) {
            NETWORK_MANAGER = ReflectionUtils.getFields(PlayerConnection, NetworkManager).get(0);
        }
        if (minorVersion >= 8) {
            CHANNEL = ReflectionUtils.getFields(NetworkManager, Channel.class).get(0);
            getProfile = ReflectionUtils.getMethods(EntityHuman, GameProfile.class).get(0);
            Constructor<?> newEntityArmorStand = EntityArmorStand.getConstructor(World, double.class, double.class, double.class);
            Method World_getHandle = Class.forName("org.bukkit.craftbukkit." + serverPackage + ".CraftWorld").getMethod("getHandle");
            dummyEntity = newEntityArmorStand.newInstance(World_getHandle.invoke(Bukkit.getWorlds().get(0)), 0, 0, 0);
            PacketPlayOutPlayerListHeaderFooterStorage.load(this);
            PacketPlayOutPlayerInfoStorage.load(this);
        }
        if (minorVersion >= 9) {
            DataWatcherObject.load();
        }
        PLAYER_CONNECTION = ReflectionUtils.getFields(EntityPlayer, PlayerConnection).get(0);
        getHandle = Class.forName("org.bukkit.craftbukkit." + serverPackage + ".entity.CraftPlayer").getMethod("getHandle");
        sendPacket = ReflectionUtils.getMethods(PlayerConnection, void.class, Packet).get(0);
        emptyScoreboard = Scoreboard.getConstructor().newInstance();
        DataWatcher.load(this);
        DataWatcherItem.load(this);
        PacketPlayOutEntityDestroyStorage.load();
        PacketPlayOutEntityMetadataStorage.load(this);
        PacketPlayOutEntityTeleportStorage.load(this);
        PacketPlayOutSpawnEntityLivingStorage.load(this);
        PacketPlayOutScoreboardObjectiveStorage.load(this);
        PacketPlayOutScoreboardDisplayObjectiveStorage.load();
        PacketPlayOutScoreboardTeamStorage.load(this);
        PacketPlayOutScoreboardScoreStorage.load(this);
        IScoreboardCriteria_self = ReflectionUtils.getFields(IScoreboardCriteria, IScoreboardCriteria).get(0);
        PacketPlayOutEntity_ENTITYID = ReflectionUtils.getFields(PacketPlayOutEntity, int.class).get(0);
        PacketPlayOutNamedEntitySpawn_ENTITYID = ReflectionUtils.getFields(PacketPlayOutNamedEntitySpawn, int.class).get(0);
        loadNamedFieldsAndMethods();
    }

    /**
     * Loads all classes used by the plugin
     *
     * @throws  ClassNotFoundException
     *          If a class was not found
     */
    public abstract void loadClasses() throws ClassNotFoundException;

    /**
     * Loads all fields and methods which must be loaded by their name due to
     * lack of other methods of reliably retrieving them.
     *
     * @throws  ReflectiveOperationException
     *          If a field or method was not found
     */
    public abstract void loadNamedFieldsAndMethods() throws ReflectiveOperationException;

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
     * Creates a new Scoreboard Objective with given name.
     *
     * @param   objectiveName
     *          Objective name
     * @return  NMS Objective
     * @throws  ReflectiveOperationException
     *          if thrown by reflective operation
     */
    public Object newScoreboardObjective(@NotNull String objectiveName) throws ReflectiveOperationException {
        if (minorVersion >= 13) {
            return PacketPlayOutScoreboardObjectiveStorage.newScoreboardObjective.newInstance(null, objectiveName, null, toNMSComponent(new IChatBaseComponent(""), TAB.getInstance().getServerVersion()), null);
        }
        return PacketPlayOutScoreboardObjectiveStorage.newScoreboardObjective.newInstance(null, objectiveName, IScoreboardCriteria_self.get(null));
    }
}