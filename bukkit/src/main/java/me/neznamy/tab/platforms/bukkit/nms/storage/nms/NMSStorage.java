package me.neznamy.tab.platforms.bukkit.nms.storage.nms;

import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.chat.WrappedChatComponent;
import me.neznamy.tab.api.util.ComponentCache;
import me.neznamy.tab.api.util.ReflectionUtils;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherItem;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherObject;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.*;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class holding all NMS classes, methods, fields and constructors used by TAB.
 */
@SuppressWarnings("rawtypes")
public abstract class NMSStorage {

    /** Instance of this class */
    @Getter @Setter private static NMSStorage instance;

    /** Server's NMS/CraftBukkit package */
    protected final String serverPackage = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    /** Server's minor version */
    @Getter protected final int minorVersion = Integer.parseInt(serverPackage.split("_")[1]);

    /** Flag determining whether the server version is at least 1.19.3 or not */
    @Getter private final boolean is1_19_3Plus = ReflectionUtils.classExists("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket");
    @Getter private final boolean is1_19_4Plus = is1_19_3Plus && !serverPackage.equals("v1_19_R2");

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
    public Class<?> PacketPlayInUseEntity;
    public Class<?> PacketPlayInUseEntity$d;
    protected Class<Enum> EnumEntityUseAction;
    public Field PacketPlayInUseEntity_ACTION;

    public Class<?> PacketPlayOutEntity;
    public Field PacketPlayOutEntity_ENTITYID;

    public Class<?> PacketPlayOutEntityLook;

    public Class<?> PacketPlayOutNamedEntitySpawn;
    public Field PacketPlayOutNamedEntitySpawn_ENTITYID;

    /** Scoreboard objectives */
    public Class<?> Scoreboard;
    protected Constructor<?> newScoreboard;
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
            NETWORK_MANAGER = getFields(PlayerConnection, NetworkManager).get(0);
        }
        if (minorVersion >= 8) {
            CHANNEL = getFields(NetworkManager, Channel.class).get(0);
            getProfile = getMethods(EntityHuman, GameProfile.class).get(0);
            Constructor<?> newEntityArmorStand = EntityArmorStand.getConstructor(World, double.class, double.class, double.class);
            Method World_getHandle = Class.forName("org.bukkit.craftbukkit." + serverPackage + ".CraftWorld").getMethod("getHandle");
            dummyEntity = newEntityArmorStand.newInstance(World_getHandle.invoke(Bukkit.getWorlds().get(0)), 0, 0, 0);
        }
        PLAYER_CONNECTION = getFields(EntityPlayer, PlayerConnection).get(0);
        getHandle = Class.forName("org.bukkit.craftbukkit." + serverPackage + ".entity.CraftPlayer").getMethod("getHandle");
        sendPacket = getMethods(PlayerConnection, void.class, Packet).get(0);
        newScoreboard = Scoreboard.getConstructor();
        emptyScoreboard = newScoreboard.newInstance();
        DataWatcher.load(this);
        DataWatcherItem.load(this);
        DataWatcherObject.load(this);
        PacketPlayOutEntityDestroyStorage.load(this);
        PacketPlayOutEntityMetadataStorage.load(this);
        PacketPlayOutEntityTeleportStorage.load(this);
        PacketPlayOutSpawnEntityLivingStorage.load(this);
        PacketPlayOutPlayerListHeaderFooterStorage.load(this);
        PacketPlayOutPlayerInfoStorage.load(this);
        PacketPlayOutScoreboardObjectiveStorage.load(this);
        PacketPlayOutScoreboardDisplayObjectiveStorage.load(this);
        otherEntity();
        PacketPlayOutScoreboardTeamStorage.load(this);
        PacketPlayOutScoreboardScoreStorage.load(this);
        IScoreboardCriteria_self = getFields(IScoreboardCriteria, IScoreboardCriteria).get(0);
        loadNamedFieldsAndMethods();
    }

    /**
     * Loads fields for other entity packets
     *
     */
    protected void otherEntity() {
        PacketPlayOutEntity_ENTITYID = getFields(PacketPlayOutEntity, int.class).get(0);
        PacketPlayOutNamedEntitySpawn_ENTITYID = getFields(PacketPlayOutNamedEntitySpawn, int.class).get(0);
        if (minorVersion >= 7) {
            PacketPlayInUseEntity_ACTION = getFields(PacketPlayInUseEntity, EnumEntityUseAction).get(0);
        }
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
     * Returns method with specified possible names and parameters. Throws exception if no such method was found
     *
     * @param   clazz
     *          lass to get method from
     * @param   names
     *          possible method names
     * @param   parameterTypes
     *          parameter types of the method
     * @return  method with specified name and parameters
     * @throws  NoSuchMethodException
     *          if no such method exists
     */
    protected Method getMethod(Class<?> clazz, String[] names, Class<?>... parameterTypes) throws NoSuchMethodException {
        for (String name : names) {
            try {
                return clazz.getMethod(name, parameterTypes);
            } catch (NoSuchMethodException e) {
                //not the first method in array
            }
        }
        List<String> list = new ArrayList<>();
        for (Method m : clazz.getMethods()) {
            if (m.getParameterCount() != parameterTypes.length) continue;
            Class<?>[] types = m.getParameterTypes();
            boolean valid = true;
            for (int i=0; i<types.length; i++) {
                if (types[i] != parameterTypes[i]) {
                    valid = false;
                    break;
                }
            }
            if (valid) list.add(m.getName());
        }
        throw new NoSuchMethodException("No method found with possible names " + Arrays.toString(names) + " with parameters " +
                Arrays.toString(parameterTypes) + " in class " + clazz.getName() + ". Methods with matching parameters: " + list);
    }

    /**
     * Returns all methods from class which return specified class type and have specified parameter types.
     *
     * @param   clazz
     *          Class to get methods from
     * @param   returnType
     *          Return type of methods
     * @param   parameterTypes
     *          Parameter types of methods
     * @return  List of found methods matching requirements. If nothing is found, empty list is returned.
     */
    public List<Method> getMethods(Class<?> clazz, Class<?> returnType, Class<?>... parameterTypes) {
        List<Method> list = new ArrayList<>();
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getReturnType() != returnType || m.getParameterCount() != parameterTypes.length || !Modifier.isPublic(m.getModifiers())) continue;
            Class<?>[] types = m.getParameterTypes();
            boolean valid = true;
            for (int i=0; i<types.length; i++) {
                if (types[i] != parameterTypes[i]) {
                    valid = false;
                    break;
                }
            }
            if (valid) list.add(m);
        }
        return list;
    }

    /**
     * Returns all fields of class with defined class type
     *
     * @param   clazz
     *          class to check fields of
     * @param   type
     *          field type to check for
     * @return  list of all fields with specified class type
     */
    public List<Field> getFields(Class<?> clazz, Class<?> type) {
        List<Field> list = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType() == type) {
                field.setAccessible(true);
                list.add(field);
            }
        }
        return list;
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
    public Object toNMSComponent(IChatBaseComponent component, ProtocolVersion clientVersion) {
        if (component instanceof WrappedChatComponent) return ((WrappedChatComponent) component).getOriginalComponent();
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
    public Object newScoreboardObjective(String objectiveName) throws ReflectiveOperationException {
        if (minorVersion >= 13) {
            return PacketPlayOutScoreboardObjectiveStorage.newScoreboardObjective.newInstance(null, objectiveName, null, toNMSComponent(new IChatBaseComponent(""), TabAPI.getInstance().getServerVersion()), null);
        }
        return PacketPlayOutScoreboardObjectiveStorage.newScoreboardObjective.newInstance(null, objectiveName, IScoreboardCriteria_self.get(null));
    }
}