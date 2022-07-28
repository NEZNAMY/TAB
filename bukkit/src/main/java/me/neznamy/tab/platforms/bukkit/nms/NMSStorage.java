package me.neznamy.tab.platforms.bukkit.nms;

import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherRegistry;
import org.bukkit.Bukkit;

import java.lang.reflect.*;
import java.util.*;

@SuppressWarnings("rawtypes")
public abstract class NMSStorage {

    //instance of this class
    private static NMSStorage instance;

    //server package, such as "v1_16_R3"
    protected final String serverPackage = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    //server minor version such as "16"
    protected final int minorVersion = Integer.parseInt(serverPackage.split("_")[1]);

    //another field due to fabric which uses 1.16 package on 1.17 server
    private final ProtocolVersion serverVersion = ProtocolVersion.fromFriendlyName(Bukkit.getBukkitVersion().split("-")[0]);

    //base
    protected Class<?> Packet;
    protected Class<?> NetworkManager;
    protected Class<?> EntityArmorStand;
    protected Class<?> World;
    public Class<?> EnumChatFormat;
    public Class<?> EntityPlayer;
    protected Class<?> EntityHuman;
    protected Class<?> Entity;
    protected Class<?> EntityLiving;
    protected Class<?> PlayerConnection;
    public Field PING;
    public Field PLAYER_CONNECTION;
    public Field NETWORK_MANAGER;
    public Field CHANNEL;
    public Method getHandle;
    public Method sendPacket;
    public Method getProfile;
    public Enum[] EnumChatFormat_values;

    //chat
    protected Class<?> IChatBaseComponent;
    protected Class<?> ChatSerializer;
    public Method ChatSerializer_DESERIALIZE;

    //PacketPlayOutChat
    protected Class<?> PacketPlayOutChat;
    public Class<?> ChatMessageType;
    public Constructor<?> newPacketPlayOutChat;
    public Enum[] ChatMessageType_values;

    //DataWatcher
    protected Class<?> DataWatcher;
    protected Class<?> DataWatcherItem;
    protected Class<?> DataWatcherObject;
    protected Class<?> DataWatcherSerializer;
    public Class<?> DataWatcherRegistry;
    public Constructor<?> newDataWatcher;
    public Constructor<?> newDataWatcherObject;
    public Field DataWatcherItem_TYPE;
    public Field DataWatcherItem_VALUE;
    public Field DataWatcherObject_SLOT;
    public Field DataWatcherObject_SERIALIZER;
    public Method DataWatcher_REGISTER;
    private final DataWatcherRegistry registry;

    //PacketPlayOutSpawnEntityLiving
    public Class<?> PacketPlayOutSpawnEntityLiving;
    public Constructor<?> newPacketPlayOutSpawnEntityLiving;
    public Field PacketPlayOutSpawnEntityLiving_ENTITYID;
    public Field PacketPlayOutSpawnEntityLiving_ENTITYTYPE;
    public Field PacketPlayOutSpawnEntityLiving_YAW;
    public Field PacketPlayOutSpawnEntityLiving_PITCH;
    public Field PacketPlayOutSpawnEntityLiving_UUID;
    public Field PacketPlayOutSpawnEntityLiving_X;
    public Field PacketPlayOutSpawnEntityLiving_Y;
    public Field PacketPlayOutSpawnEntityLiving_Z;
    public Field PacketPlayOutSpawnEntityLiving_DATAWATCHER;
    public Method Registry_a;
    public Object IRegistry_X;

    //PacketPlayOutEntityTeleport
    public Class<?> PacketPlayOutEntityTeleport;
    public Constructor<?> newPacketPlayOutEntityTeleport;
    public Field PacketPlayOutEntityTeleport_ENTITYID;
    public Field PacketPlayOutEntityTeleport_X;
    public Field PacketPlayOutEntityTeleport_Y;
    public Field PacketPlayOutEntityTeleport_Z;
    public Field PacketPlayOutEntityTeleport_YAW;
    public Field PacketPlayOutEntityTeleport_PITCH;

    //PacketPlayOutPlayerListHeaderFooter
    protected Class<?> PacketPlayOutPlayerListHeaderFooter;
    public Constructor<?> newPacketPlayOutPlayerListHeaderFooter;
    public Field PacketPlayOutPlayerListHeaderFooter_HEADER;
    public Field PacketPlayOutPlayerListHeaderFooter_FOOTER;

    //other entity packets
    public Class<?> PacketPlayInUseEntity;
    public Class<?> PacketPlayInUseEntity$d;
    protected Class<?> EnumEntityUseAction;
    public Field PacketPlayInUseEntity_ENTITY;
    public Field PacketPlayInUseEntity_ACTION;

    public Class<?> PacketPlayOutEntity;
    public Field PacketPlayOutEntity_ENTITYID;

    public Class<?> PacketPlayOutEntityDestroy;
    public Constructor<?> newPacketPlayOutEntityDestroy;
    public Field PacketPlayOutEntityDestroy_ENTITIES;

    public Class<?> PacketPlayOutEntityLook;

    public Class<?> PacketPlayOutEntityMetadata;
    public Constructor<?> newPacketPlayOutEntityMetadata;
    public Field PacketPlayOutEntityMetadata_LIST;

    public Class<?> PacketPlayOutNamedEntitySpawn;
    public Field PacketPlayOutNamedEntitySpawn_ENTITYID;

    //PacketPlayOutPlayerInfo
    public Class<?> PacketPlayOutPlayerInfo;
    protected Class<?> EnumPlayerInfoAction;
    protected Class<?> PlayerInfoData;
    protected Class<?> EnumGamemode;
    public Constructor<?> newPacketPlayOutPlayerInfo;
    public Constructor<?> newPlayerInfoData;
    public Field PacketPlayOutPlayerInfo_ACTION;
    public Field PacketPlayOutPlayerInfo_PLAYERS;
    public Method PlayerInfoData_getProfile;
    public Method PlayerInfoData_getLatency;
    public Method PlayerInfoData_getGamemode;
    public Method PlayerInfoData_getDisplayName;
    public Enum[] EnumPlayerInfoAction_values;
    public Enum[] EnumGamemode_values;

    //scoreboard objectives
    public Class<?> PacketPlayOutScoreboardDisplayObjective;
    public Class<?> PacketPlayOutScoreboardObjective;
    protected Class<?> Scoreboard;
    protected Class<?> PacketPlayOutScoreboardScore;
    protected Class<?> ScoreboardObjective;
    protected Class<?> ScoreboardScore;
    protected Class<?> IScoreboardCriteria;
    public Class<?> EnumScoreboardHealthDisplay;
    protected Class<?> EnumScoreboardAction;
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
    public Enum[] EnumScoreboardHealthDisplay_values;
    public Enum[] EnumScoreboardAction_values;

    //PacketPlayOutScoreboardTeam
    public Class<?> PacketPlayOutScoreboardTeam;
    protected Class<?> ScoreboardTeam;
    protected Class<?> EnumNameTagVisibility;
    protected Class<?> EnumTeamPush;
    protected Class<?> PacketPlayOutScoreboardTeam_PlayerAction;
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
    public Enum[] EnumNameTagVisibility_values;
    public Enum[] EnumTeamPush_values;
    public Enum[] PacketPlayOutScoreboardTeam_PlayerAction_values;

    //other
    public Object emptyScoreboard;
    public Object dummyEntity;

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
        EnumChatFormat_values = getEnumValues(EnumChatFormat);
        registry = new DataWatcherRegistry(this);
        dataWatcher();
        entityTeleport();
        spawnEntityLiving();
        chat();
        playerListHeaderFooter();
        playerInfo();
        otherEntity();
        scoreboard();
        scoreboardTeam();
        loadNamedFieldsAndMethods();
    }

    /**
     * Sets new instance
     *
     * @param   instance
     *          new instance
     */
    public static void setInstance(NMSStorage instance) {
        NMSStorage.instance = instance;
    }

    /**
     * Returns instance
     *
     * @return  instance
     */
    public static NMSStorage getInstance() {
        return instance;
    }

    protected void chat() throws ReflectiveOperationException {
        if (minorVersion >= 12 && minorVersion <= 18) {
            ChatMessageType_values = getEnumValues(ChatMessageType);
        }
        if (minorVersion >= 19) {
            newPacketPlayOutChat = PacketPlayOutChat.getConstructor(IChatBaseComponent, boolean.class); // int = NoSuchMethod
        } else if (minorVersion >= 16) {
            newPacketPlayOutChat = PacketPlayOutChat.getConstructor(IChatBaseComponent, ChatMessageType, UUID.class);
        } else if (minorVersion >= 12) {
            newPacketPlayOutChat = PacketPlayOutChat.getConstructor(IChatBaseComponent, ChatMessageType);
        } else if (minorVersion >= 8) {
            newPacketPlayOutChat = PacketPlayOutChat.getConstructor(IChatBaseComponent, byte.class);
        } else if (minorVersion >= 7){
            newPacketPlayOutChat = PacketPlayOutChat.getConstructor(IChatBaseComponent);
        }
    }

    protected void playerInfo() throws ReflectiveOperationException {
        if (minorVersion < 8) return;
        newPacketPlayOutPlayerInfo = PacketPlayOutPlayerInfo.getConstructor(EnumPlayerInfoAction, Array.newInstance(EntityPlayer, 0).getClass());
        newPlayerInfoData = PlayerInfoData.getConstructors()[0];
        PacketPlayOutPlayerInfo_ACTION = getFields(PacketPlayOutPlayerInfo, EnumPlayerInfoAction).get(0);
        PacketPlayOutPlayerInfo_PLAYERS = getFields(PacketPlayOutPlayerInfo, List.class).get(0);
        PlayerInfoData_getProfile = getMethods(PlayerInfoData, GameProfile.class).get(0);
        PlayerInfoData_getLatency = getMethods(PlayerInfoData, int.class).get(0);
        PlayerInfoData_getGamemode = getMethods(PlayerInfoData, EnumGamemode).get(0);
        PlayerInfoData_getDisplayName = getMethods(PlayerInfoData, IChatBaseComponent).get(0);
        EnumPlayerInfoAction_values = getEnumValues(EnumPlayerInfoAction);
        EnumGamemode_values = getEnumValues(EnumGamemode);
    }

    protected void spawnEntityLiving() throws ReflectiveOperationException {
        if (minorVersion >= 17 || serverVersion.getMinorVersion() >= 17) {
            newPacketPlayOutSpawnEntityLiving = PacketPlayOutSpawnEntityLiving.getConstructor(EntityLiving);
        } else {
            newPacketPlayOutSpawnEntityLiving = PacketPlayOutSpawnEntityLiving.getConstructor();
        }
        PacketPlayOutSpawnEntityLiving_ENTITYID = getFields(PacketPlayOutSpawnEntityLiving, int.class).get(0);
        PacketPlayOutSpawnEntityLiving_YAW = getFields(PacketPlayOutSpawnEntityLiving, byte.class).get(0);
        PacketPlayOutSpawnEntityLiving_PITCH = getFields(PacketPlayOutSpawnEntityLiving, byte.class).get(0);
        if (minorVersion >= 9) {
            PacketPlayOutSpawnEntityLiving_UUID = getFields(PacketPlayOutSpawnEntityLiving, UUID.class).get(0);
            if (minorVersion >= 19) {
                PacketPlayOutSpawnEntityLiving_X = getFields(PacketPlayOutSpawnEntityLiving, double.class).get(2);
                PacketPlayOutSpawnEntityLiving_Y = getFields(PacketPlayOutSpawnEntityLiving, double.class).get(3);
                PacketPlayOutSpawnEntityLiving_Z = getFields(PacketPlayOutSpawnEntityLiving, double.class).get(4);
            } else {
                PacketPlayOutSpawnEntityLiving_X = getFields(PacketPlayOutSpawnEntityLiving, double.class).get(0);
                PacketPlayOutSpawnEntityLiving_Y = getFields(PacketPlayOutSpawnEntityLiving, double.class).get(1);
                PacketPlayOutSpawnEntityLiving_Z = getFields(PacketPlayOutSpawnEntityLiving, double.class).get(2);
            }
        } else {
            PacketPlayOutSpawnEntityLiving_X = getFields(PacketPlayOutSpawnEntityLiving, int.class).get(2);
            PacketPlayOutSpawnEntityLiving_Y = getFields(PacketPlayOutSpawnEntityLiving, int.class).get(3);
            PacketPlayOutSpawnEntityLiving_Z = getFields(PacketPlayOutSpawnEntityLiving, int.class).get(4);
        }
        if (minorVersion < 19) {
            PacketPlayOutSpawnEntityLiving_ENTITYTYPE = getFields(PacketPlayOutSpawnEntityLiving, int.class).get(1);
        }
        if (minorVersion <= 14) {
            PacketPlayOutSpawnEntityLiving_DATAWATCHER = getFields(PacketPlayOutSpawnEntityLiving, DataWatcher).get(0);
        }
    }

    protected void entityTeleport() throws ReflectiveOperationException {
        PacketPlayOutEntityTeleport_ENTITYID = getFields(PacketPlayOutEntityTeleport, int.class).get(0);
        PacketPlayOutEntityTeleport_YAW = getFields(PacketPlayOutEntityTeleport, byte.class).get(0);
        PacketPlayOutEntityTeleport_PITCH = getFields(PacketPlayOutEntityTeleport, byte.class).get(1);
        if (minorVersion >= 17 || serverVersion.getMinorVersion() >= 17) {
            newPacketPlayOutEntityTeleport = PacketPlayOutEntityTeleport.getConstructor(Entity);
        } else {
            newPacketPlayOutEntityTeleport = PacketPlayOutEntityTeleport.getConstructor();
        }
        if (minorVersion >= 9) {
            PacketPlayOutEntityTeleport_X = getFields(PacketPlayOutEntityTeleport, double.class).get(0);
            PacketPlayOutEntityTeleport_Y = getFields(PacketPlayOutEntityTeleport, double.class).get(1);
            PacketPlayOutEntityTeleport_Z = getFields(PacketPlayOutEntityTeleport, double.class).get(2);
        } else {
            PacketPlayOutEntityTeleport_X = getFields(PacketPlayOutEntityTeleport, int.class).get(1);
            PacketPlayOutEntityTeleport_Y = getFields(PacketPlayOutEntityTeleport, int.class).get(2);
            PacketPlayOutEntityTeleport_Z = getFields(PacketPlayOutEntityTeleport, int.class).get(3);
        }
    }

    protected void playerListHeaderFooter() throws ReflectiveOperationException {
        if (minorVersion < 8) return;
        PacketPlayOutPlayerListHeaderFooter_HEADER = getFields(PacketPlayOutPlayerListHeaderFooter, IChatBaseComponent).get(0);
        PacketPlayOutPlayerListHeaderFooter_FOOTER = getFields(PacketPlayOutPlayerListHeaderFooter, IChatBaseComponent).get(1);
        if (minorVersion >= 17 || serverVersion.getMinorVersion() >= 17) {
            newPacketPlayOutPlayerListHeaderFooter = PacketPlayOutPlayerListHeaderFooter.getConstructor(IChatBaseComponent, IChatBaseComponent);
        } else {
            newPacketPlayOutPlayerListHeaderFooter = PacketPlayOutPlayerListHeaderFooter.getConstructor();
        }
    }

    protected void otherEntity() throws ReflectiveOperationException {
        PacketPlayOutEntity_ENTITYID = getFields(PacketPlayOutEntity, int.class).get(0);
        PacketPlayOutEntityDestroy_ENTITIES = setAccessible(PacketPlayOutEntityDestroy.getDeclaredFields()[0]);
        newPacketPlayOutEntityMetadata = PacketPlayOutEntityMetadata.getConstructor(int.class, DataWatcher, boolean.class);
        PacketPlayOutEntityMetadata_LIST = getFields(PacketPlayOutEntityMetadata, List.class).get(0);
        PacketPlayOutNamedEntitySpawn_ENTITYID = getFields(PacketPlayOutNamedEntitySpawn, int.class).get(0);
        if (minorVersion >= 7) {
            PacketPlayInUseEntity_ENTITY = getFields(PacketPlayInUseEntity, int.class).get(0);
            PacketPlayInUseEntity_ACTION = getFields(PacketPlayInUseEntity, EnumEntityUseAction).get(0);
        }
        try {
            newPacketPlayOutEntityDestroy = PacketPlayOutEntityDestroy.getConstructor(int[].class);
        } catch (NoSuchMethodException e) {
            //1.17.0
            newPacketPlayOutEntityDestroy = PacketPlayOutEntityDestroy.getConstructor(int.class);
        }
    }

    protected void dataWatcher() throws ReflectiveOperationException {
        newDataWatcher = DataWatcher.getConstructors()[0];
        DataWatcherItem_VALUE = getFields(DataWatcherItem, Object.class).get(0);
        if (minorVersion >= 9) {
            newDataWatcherObject = DataWatcherObject.getConstructor(int.class, DataWatcherSerializer);
            DataWatcherItem_TYPE = getFields(DataWatcherItem, DataWatcherObject).get(0);
            DataWatcherObject_SLOT = getFields(DataWatcherObject, int.class).get(0);
            DataWatcherObject_SERIALIZER = getFields(DataWatcherObject, DataWatcherSerializer).get(0);
        } else {
            DataWatcherItem_TYPE = getFields(DataWatcherItem, int.class).get(1);
        }
    }

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
            EnumScoreboardHealthDisplay_values = getEnumValues(EnumScoreboardHealthDisplay);
            EnumScoreboardAction_values = getEnumValues(EnumScoreboardAction);
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

    protected void scoreboardTeam() throws ReflectiveOperationException {
        newScoreboardTeam = ScoreboardTeam.getConstructor(Scoreboard, String.class);
        PacketPlayOutScoreboardTeam_NAME = getFields(PacketPlayOutScoreboardTeam, String.class).get(0);
        PacketPlayOutScoreboardTeam_ACTION = getInstanceFields(PacketPlayOutScoreboardTeam, int.class).get(0);
        PacketPlayOutScoreboardTeam_PLAYERS = getFields(PacketPlayOutScoreboardTeam, Collection.class).get(0);
        ScoreboardTeam_getPlayerNameSet = getMethods(ScoreboardTeam, Collection.class).get(0);
        if (minorVersion >= 8) {
            EnumNameTagVisibility_values = getEnumValues(EnumNameTagVisibility);
        }
        if (minorVersion >= 9) {
            EnumTeamPush_values = getEnumValues(EnumTeamPush);
            ScoreboardTeam_setCollisionRule = getMethods(ScoreboardTeam, void.class, EnumTeamPush).get(0);
        }
        if (minorVersion >= 13) {
            ScoreboardTeam_setColor = getMethods(ScoreboardTeam, void.class, EnumChatFormat).get(0);
        }
        if (minorVersion >= 17) {
            PacketPlayOutScoreboardTeam_of = getMethods(PacketPlayOutScoreboardTeam, PacketPlayOutScoreboardTeam, ScoreboardTeam).get(0);
            PacketPlayOutScoreboardTeam_ofBoolean = getMethods(PacketPlayOutScoreboardTeam, PacketPlayOutScoreboardTeam, ScoreboardTeam, boolean.class).get(0);
            PacketPlayOutScoreboardTeam_ofString = getMethods(PacketPlayOutScoreboardTeam, PacketPlayOutScoreboardTeam, ScoreboardTeam, String.class, PacketPlayOutScoreboardTeam_PlayerAction).get(0);
            PacketPlayOutScoreboardTeam_PlayerAction_values = getEnumValues(PacketPlayOutScoreboardTeam_PlayerAction);
        } else {
            newPacketPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.getConstructor(ScoreboardTeam, int.class);
        }
    }

    public abstract void loadClasses() throws ClassNotFoundException;

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
                return getMethod(clazz, name, parameterTypes);
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

    protected Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        List<Method> list = new ArrayList<>();
        for (Method m : clazz.getMethods()) {
            if (!m.getName().equals(name) || m.getParameterCount() != parameterTypes.length) continue;
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
        if (!list.isEmpty()) return list.get(0);
        throw new NoSuchMethodException("No method found with name " + name + " in class " + clazz.getName() + " with parameters " + Arrays.toString(parameterTypes));
    }

    protected List<Method> getMethods(Class<?> clazz, Class<?> returnType, Class<?>... parameterTypes){
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
    protected List<Field> getFields(Class<?> clazz, Class<?> type){
        if (clazz == null) throw new IllegalArgumentException("Source class cannot be null");
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
    public List<Field> getInstanceFields(Class<?> clazz, Class<?> type){
        if (clazz == null) throw new IllegalArgumentException("Source class cannot be null");
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

    protected Field getField(Class<?> clazz, String... potentialNames) throws NoSuchFieldException {
        for (String name : potentialNames) {
            try {
                return getField(clazz, name);
            } catch (NoSuchFieldException e) {
                //not the first field name from array
            }
        }
        throw new NoSuchFieldException("No field found in class " + clazz.getName() + " with potential names " + Arrays.toString(potentialNames));
    }

    protected Enum[] getEnumValues(Class<?> enumClass) {
        if (enumClass == null) throw new IllegalArgumentException("Class cannot be null");
        if (!enumClass.isEnum()) throw new IllegalArgumentException(enumClass.getName() + " is not an enum class");
        try {
            return (Enum[]) enumClass.getMethod("values").invoke(null);
        } catch (ReflectiveOperationException e) {
            //this should never happen
            return new Enum[0];
        }
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public void setField(Object obj, Field field, Object value) throws IllegalAccessException {
        field.set(obj, value);
    }

    public DataWatcherRegistry getDataWatcherRegistry() {
        return registry;
    }

    public <T extends AccessibleObject> T setAccessible(T o) {
        o.setAccessible(true);
        return o;
    }

    /**
     * Gets values of all static fields in a class
     *
     * @param   clazz
     *          class to return field values from
     * @return  map of values
     */
    public Map<String, Object> getStaticFields(Class<?> clazz){
        Map<String, Object> fields = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                setAccessible(field);
                try {
                    fields.put(field.getName(), field.get(null));
                } catch (IllegalAccessException e) {
                    //this will never happen
                }
            }
        }
        return fields;
    }
}