package me.neznamy.tab.platforms.bukkit.nms.storage;

import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.util.ReflectionUtils;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityDestroy;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityMetadata;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityTeleport;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherHelper;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherItem;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherObject;
import org.bukkit.Bukkit;

import java.lang.reflect.*;
import java.util.*;

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
    protected Class<?> IChatBaseComponent;
    protected Class<?> ChatSerializer;
    public Method ChatSerializer_DESERIALIZE;

    /** PacketPlayOutChat */
    protected Class<?> PacketPlayOutChat;
    public Class<Enum> ChatMessageType;
    public Constructor<?> newPacketPlayOutChat;

    /** PacketPlayOutPlayerListHeaderFooter */
    protected Class<?> PacketPlayOutPlayerListHeaderFooter;
    public Constructor<?> newPacketPlayOutPlayerListHeaderFooter;
    public Field PacketPlayOutPlayerListHeaderFooter_HEADER;
    public Field PacketPlayOutPlayerListHeaderFooter_FOOTER;

    /** Other entity packets */
    public Class<?> PacketPlayInUseEntity;
    public Class<?> PacketPlayInUseEntity$d;
    protected Class<Enum> EnumEntityUseAction;
    public Field PacketPlayInUseEntity_ENTITY;
    public Field PacketPlayInUseEntity_ACTION;

    public Class<?> PacketPlayOutEntity;
    public Field PacketPlayOutEntity_ENTITYID;

    public Class<?> PacketPlayOutEntityLook;

    public Class<?> PacketPlayOutNamedEntitySpawn;
    public Field PacketPlayOutNamedEntitySpawn_ENTITYID;

    /** PacketPlayOutPlayerInfo */
    public Class<?> PacketPlayOutPlayerInfo;
    public Class<Enum> EnumPlayerInfoAction;
    protected Class<?> PlayerInfoData;
    public Class<Enum> EnumGamemode;
    protected Class<?> ProfilePublicKey;
    protected Class<?> ProfilePublicKey$a;
    public Constructor<?> newPacketPlayOutPlayerInfo;
    public Constructor<?> newPlayerInfoData;
    public Field PacketPlayOutPlayerInfo_ACTION;
    public Field PacketPlayOutPlayerInfo_PLAYERS;
    public Method PlayerInfoData_getProfile;
    public Method PlayerInfoData_isListed;
    public Method PlayerInfoData_getLatency;
    public Method PlayerInfoData_getGamemode;
    public Method PlayerInfoData_getDisplayName;
    public Method PlayerInfoData_getProfilePublicKeyRecord;
    //1.19.3+
    public Class<?> ClientboundPlayerInfoRemovePacket;
    public Class<?> RemoteChatSession;
    public Class<?> RemoteChatSession$Data;
    public Constructor<?> newClientboundPlayerInfoRemovePacket;
    public Constructor<?> newRemoteChatSession$Data;
    public Method ClientboundPlayerInfoRemovePacket_getEntries;
    public Method RemoteChatSession$Data_getSessionId;
    public Method RemoteChatSession$Data_getProfilePublicKey;


    /** Scoreboard objectives */
    public Class<?> PacketPlayOutScoreboardDisplayObjective;
    public Class<?> PacketPlayOutScoreboardObjective;
    protected Class<?> Scoreboard;
    protected Class<?> PacketPlayOutScoreboardScore;
    protected Class<?> ScoreboardObjective;
    protected Class<?> ScoreboardScore;
    protected Class<?> IScoreboardCriteria;
    public Class<Enum> EnumScoreboardHealthDisplay;
    public Class<Enum> EnumScoreboardAction;
    public Constructor<?> newScoreboardObjective;
    protected Constructor<?> newScoreboard;
    public Constructor<?> newScoreboardScore;
    public Constructor<?> newPacketPlayOutScoreboardDisplayObjective;
    public Constructor<?> newPacketPlayOutScoreboardObjective;
    public Constructor<?> newPacketPlayOutScoreboardScore_1_13;
    public Constructor<?> newPacketPlayOutScoreboardScore_String;
    public Constructor<?> newPacketPlayOutScoreboardScore;
    public Field PacketPlayOutScoreboardDisplayObjective_POSITION;
    public Field PacketPlayOutScoreboardDisplayObjective_OBJECTIVENAME;
    public Field PacketPlayOutScoreboardObjective_OBJECTIVENAME;
    public Field PacketPlayOutScoreboardObjective_METHOD;
    public Field IScoreboardCriteria_self;
    public Field PacketPlayOutScoreboardObjective_RENDERTYPE;
    public Field PacketPlayOutScoreboardObjective_DISPLAYNAME;
    public Method ScoreboardScore_setScore;

    /** PacketPlayOutScoreboardTeam */
    public Class<?> PacketPlayOutScoreboardTeam;
    protected Class<?> ScoreboardTeam;
    public Class<Enum> EnumNameTagVisibility;
    public Class<Enum> EnumTeamPush;
    public Class<Enum> PacketPlayOutScoreboardTeam_PlayerAction;
    public Constructor<?> newScoreboardTeam;
    public Constructor<?> newPacketPlayOutScoreboardTeam;
    public Field PacketPlayOutScoreboardTeam_NAME;
    public Field PacketPlayOutScoreboardTeam_ACTION;
    public Field PacketPlayOutScoreboardTeam_PLAYERS;
    public Method ScoreboardTeam_getPlayerNameSet;
    public Method ScoreboardTeam_setNameTagVisibility;
    public Method ScoreboardTeam_setCollisionRule;
    public Method ScoreboardTeam_setPrefix;
    public Method ScoreboardTeam_setSuffix;
    public Method ScoreboardTeam_setColor;
    public Method ScoreboardTeam_setAllowFriendlyFire;
    public Method ScoreboardTeam_setCanSeeFriendlyInvisibles;
    public Method PacketPlayOutScoreboardTeam_of;
    public Method PacketPlayOutScoreboardTeam_ofBoolean;
    public Method PacketPlayOutScoreboardTeam_ofString;

    /** Other */
    public Object emptyScoreboard;
    public Object dummyEntity;

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
        DataWatcher.load(this);
        DataWatcherItem.load(this);
        DataWatcherObject.load(this);
        DataWatcherHelper.load(this);
        PacketPlayOutEntityDestroy.load(this);
        PacketPlayOutEntityMetadata.load(this);
        PacketPlayOutEntityTeleport.load(this);
        PacketPlayOutSpawnEntityLiving.load(this);
        chat();
        playerListHeaderFooter();
        playerInfo();
        otherEntity();
        scoreboard();
        scoreboardTeam();
        loadNamedFieldsAndMethods();
    }

    /**
     * Loads PacketPlayOutChat's methods, fields and constructors
     *
     * @throws  ReflectiveOperationException
     *          If some method, field or constructor was not found
     */
    protected void chat() throws ReflectiveOperationException {
        if (minorVersion >= 19) {
            try {
                newPacketPlayOutChat = PacketPlayOutChat.getConstructor(IChatBaseComponent, boolean.class);
            } catch (NoSuchMethodException e) {
                //1.19.0
                newPacketPlayOutChat = PacketPlayOutChat.getConstructor(IChatBaseComponent, int.class);
            }
        } else if (minorVersion >= 16) {
            newPacketPlayOutChat = PacketPlayOutChat.getConstructor(IChatBaseComponent, ChatMessageType, UUID.class);
        } else if (minorVersion >= 12) {
            newPacketPlayOutChat = PacketPlayOutChat.getConstructor(IChatBaseComponent, ChatMessageType);
        } else if (minorVersion >= 8) {
            newPacketPlayOutChat = PacketPlayOutChat.getConstructor(IChatBaseComponent, byte.class);
        } else if (minorVersion >= 7) {
            newPacketPlayOutChat = PacketPlayOutChat.getConstructor(IChatBaseComponent);
        }
    }

    /**
     * Loads PacketPlayOutPlayerInfo's methods, fields and constructors
     *
     * @throws  ReflectiveOperationException
     *          If some method, field or constructor was not found
     */
    protected void playerInfo() throws ReflectiveOperationException {
        if (minorVersion < 8) return;
        if (is1_19_3Plus()) {
            newClientboundPlayerInfoRemovePacket = ClientboundPlayerInfoRemovePacket.getConstructor(List.class);
            newPacketPlayOutPlayerInfo = PacketPlayOutPlayerInfo.getConstructor(EnumSet.class, Collection.class);
            ClientboundPlayerInfoRemovePacket_getEntries = getMethods(ClientboundPlayerInfoRemovePacket, List.class).get(0);
            PacketPlayOutPlayerInfo_ACTION = getFields(PacketPlayOutPlayerInfo, EnumSet.class).get(0);
            newRemoteChatSession$Data = RemoteChatSession$Data.getConstructor(UUID.class, ProfilePublicKey$a);
            RemoteChatSession$Data_getSessionId = getMethods(RemoteChatSession$Data, UUID.class).get(0);
            RemoteChatSession$Data_getProfilePublicKey = getMethods(RemoteChatSession$Data, ProfilePublicKey$a).get(0);
            PlayerInfoData_getProfilePublicKeyRecord = getMethods(PlayerInfoData, RemoteChatSession$Data).get(0);
            PlayerInfoData_isListed = getMethods(PlayerInfoData, boolean.class).get(0);
        } else {
            newPacketPlayOutPlayerInfo = PacketPlayOutPlayerInfo.getConstructor(EnumPlayerInfoAction, Array.newInstance(EntityPlayer, 0).getClass());
            PacketPlayOutPlayerInfo_ACTION = getFields(PacketPlayOutPlayerInfo, EnumPlayerInfoAction).get(0);
            if (minorVersion >= 19) {
                PlayerInfoData_getProfilePublicKeyRecord = getMethods(PlayerInfoData, ProfilePublicKey$a).get(0);
            }
        }
        newPlayerInfoData = PlayerInfoData.getConstructors()[0];
        PacketPlayOutPlayerInfo_PLAYERS = getFields(PacketPlayOutPlayerInfo, List.class).get(0);
        PlayerInfoData_getProfile = getMethods(PlayerInfoData, GameProfile.class).get(0);
        for (Method m: getMethods(PlayerInfoData, int.class)) {
            // do not take .hashCode(), which is final
            if (!Modifier.isFinal(m.getModifiers())) PlayerInfoData_getLatency = m;
        }
        PlayerInfoData_getGamemode = getMethods(PlayerInfoData, EnumGamemode).get(0);
        PlayerInfoData_getDisplayName = getMethods(PlayerInfoData, IChatBaseComponent).get(0);
    }

    /**
     * Loads PacketPlayOutPlayerListHeaderFooter's methods, fields and constructors
     *
     * @throws  ReflectiveOperationException
     *          If some method, field or constructor was not found
     */
    protected void playerListHeaderFooter() throws ReflectiveOperationException {
        if (minorVersion < 8) return;
        PacketPlayOutPlayerListHeaderFooter_HEADER = getFields(PacketPlayOutPlayerListHeaderFooter, IChatBaseComponent).get(0);
        PacketPlayOutPlayerListHeaderFooter_FOOTER = getFields(PacketPlayOutPlayerListHeaderFooter, IChatBaseComponent).get(1);
        if (minorVersion >= 17) {
            newPacketPlayOutPlayerListHeaderFooter = PacketPlayOutPlayerListHeaderFooter.getConstructor(IChatBaseComponent, IChatBaseComponent);
        } else {
            newPacketPlayOutPlayerListHeaderFooter = PacketPlayOutPlayerListHeaderFooter.getConstructor();
        }
    }

    /**
     * Loads fields for other entity packets
     *
     */
    protected void otherEntity() {
        PacketPlayOutEntity_ENTITYID = getFields(PacketPlayOutEntity, int.class).get(0);
        PacketPlayOutNamedEntitySpawn_ENTITYID = getFields(PacketPlayOutNamedEntitySpawn, int.class).get(0);
        if (minorVersion >= 7) {
            PacketPlayInUseEntity_ENTITY = getFields(PacketPlayInUseEntity, int.class).get(0);
            PacketPlayInUseEntity_ACTION = getFields(PacketPlayInUseEntity, EnumEntityUseAction).get(0);
        }
    }

    /**
     * Loads Scoreboard methods, fields and constructors
     *
     * @throws  ReflectiveOperationException
     *          If some method, field or constructor was not found
     */
    protected void scoreboard() throws ReflectiveOperationException {
        newScoreboardObjective = ScoreboardObjective.getConstructors()[0];
        newScoreboard = Scoreboard.getConstructor();
        emptyScoreboard = newScoreboard.newInstance();
        newScoreboardScore = ScoreboardScore.getConstructor(Scoreboard, ScoreboardObjective, String.class);
        newPacketPlayOutScoreboardDisplayObjective = PacketPlayOutScoreboardDisplayObjective.getConstructor(int.class, ScoreboardObjective);
        PacketPlayOutScoreboardDisplayObjective_POSITION = getFields(PacketPlayOutScoreboardDisplayObjective, int.class).get(0);
        PacketPlayOutScoreboardDisplayObjective_OBJECTIVENAME = getFields(PacketPlayOutScoreboardDisplayObjective, String.class).get(0);
        PacketPlayOutScoreboardObjective_OBJECTIVENAME = getFields(PacketPlayOutScoreboardObjective, String.class).get(0);
        IScoreboardCriteria_self = getFields(IScoreboardCriteria, IScoreboardCriteria).get(0);
        List<Field> list = getFields(PacketPlayOutScoreboardObjective, int.class);
        PacketPlayOutScoreboardObjective_METHOD = list.get(list.size()-1);
        if (minorVersion >= 8) {
            PacketPlayOutScoreboardObjective_RENDERTYPE = getFields(PacketPlayOutScoreboardObjective, EnumScoreboardHealthDisplay).get(0);
        }
        if (minorVersion >= 13) {
            newPacketPlayOutScoreboardObjective = PacketPlayOutScoreboardObjective.getConstructor(ScoreboardObjective, int.class);
            newPacketPlayOutScoreboardScore_1_13 = PacketPlayOutScoreboardScore.getConstructor(EnumScoreboardAction, String.class, String.class, int.class);
            PacketPlayOutScoreboardObjective_DISPLAYNAME = getFields(PacketPlayOutScoreboardObjective, IChatBaseComponent).get(0);
        } else {
            newPacketPlayOutScoreboardObjective = PacketPlayOutScoreboardObjective.getConstructor();
            newPacketPlayOutScoreboardScore_String = PacketPlayOutScoreboardScore.getConstructor(String.class);
            PacketPlayOutScoreboardObjective_DISPLAYNAME = getFields(PacketPlayOutScoreboardObjective, String.class).get(1);
            if (minorVersion >= 8) {
                newPacketPlayOutScoreboardScore = PacketPlayOutScoreboardScore.getConstructor(ScoreboardScore);
            } else {
                newPacketPlayOutScoreboardScore = PacketPlayOutScoreboardScore.getConstructor(ScoreboardScore, int.class);
            }
        }
    }

    /**
     * Loads PacketPlayOutScoreboardTeam's methods, fields and constructors
     *
     * @throws  ReflectiveOperationException
     *          If some method, field or constructor was not found
     */
    protected void scoreboardTeam() throws ReflectiveOperationException {
        newScoreboardTeam = ScoreboardTeam.getConstructor(Scoreboard, String.class);
        PacketPlayOutScoreboardTeam_NAME = getFields(PacketPlayOutScoreboardTeam, String.class).get(0);
        PacketPlayOutScoreboardTeam_ACTION = getInstanceFields(PacketPlayOutScoreboardTeam, int.class).get(0);
        PacketPlayOutScoreboardTeam_PLAYERS = getFields(PacketPlayOutScoreboardTeam, Collection.class).get(0);
        ScoreboardTeam_getPlayerNameSet = getMethods(ScoreboardTeam, Collection.class).get(0);
        if (minorVersion >= 9) {
            ScoreboardTeam_setCollisionRule = getMethods(ScoreboardTeam, void.class, EnumTeamPush).get(0);
        }
        if (minorVersion >= 13) {
            ScoreboardTeam_setColor = getMethods(ScoreboardTeam, void.class, EnumChatFormat).get(0);
        }
        if (minorVersion >= 17) {
            PacketPlayOutScoreboardTeam_of = getMethods(PacketPlayOutScoreboardTeam, PacketPlayOutScoreboardTeam, ScoreboardTeam).get(0);
            PacketPlayOutScoreboardTeam_ofBoolean = getMethods(PacketPlayOutScoreboardTeam, PacketPlayOutScoreboardTeam, ScoreboardTeam, boolean.class).get(0);
            PacketPlayOutScoreboardTeam_ofString = getMethods(PacketPlayOutScoreboardTeam, PacketPlayOutScoreboardTeam, ScoreboardTeam, String.class, PacketPlayOutScoreboardTeam_PlayerAction).get(0);
        } else {
            newPacketPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.getConstructor(ScoreboardTeam, int.class);
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
                list.add(setAccessible(field));
            }
        }
        return list;
    }

    /**
     * Returns all instance fields of class with defined class type
     *
     * @param   clazz
     *          class to check fields of
     * @param   type
     *          field type to check for
     * @return  list of all fields with specified class type
     */
    public List<Field> getInstanceFields(Class<?> clazz, Class<?> type) {
        List<Field> list = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType() == type && !Modifier.isStatic(field.getModifiers())) {
                list.add(setAccessible(field));
            }
        }
        return list;
    }

    /**
     * Returns field with specified name and makes it accessible
     *
     * @param   clazz
     *          class to get field from
     * @param   name
     *          field name
     * @return  accessible field with defined name
     * @throws  NoSuchFieldException
     *          if field was not found
     */
    protected Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
        for (Field f : clazz.getDeclaredFields()) {
            if (f.getName().equals(name) || (f.getName().split("_").length == 3 && f.getName().split("_")[2].equals(name))) {
                return setAccessible(f);
            }
        }
        throw new NoSuchFieldException("Field \"" + name + "\" was not found in class " + clazz.getName());
    }

    /**
     * Sets value of a field in object
     *
     * @param   obj
     *          Instance of object where field should be set
     * @param   field
     *          Field to set value of
     * @param   value
     *          Value to set field to
     * @throws  IllegalAccessException
     *          If thrown by reflective operation
     */
    public void setField(Object obj, Field field, Object value) throws IllegalAccessException {
        field.set(obj, value);
    }

    /**
     * Makes object accessible and returns it for chaining.
     *
     * @param   o
     *          Object to make accessible
     * @return  Entered object, for chaining
     */
    public <T extends AccessibleObject> T setAccessible(T o) {
        o.setAccessible(true);
        return o;
    }
}