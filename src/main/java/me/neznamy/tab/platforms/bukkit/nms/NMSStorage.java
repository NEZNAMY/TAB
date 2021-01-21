package me.neznamy.tab.platforms.bukkit.nms;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;

import io.netty.channel.Channel;

@SuppressWarnings({"unchecked", "rawtypes"})
public class NMSStorage {

	private String serverPackage;
	public int minorVersion;

	public Field PING;
	public Field PLAYER_CONNECTION;
	public Field NETWORK_MANAGER;
	public Field CHANNEL;
	public Method getHandle;
	public Method getProfile;
	public Method sendPacket;

	public Class<Enum> EnumChatFormat;

	public Class<?> IChatBaseComponent;
	public Class<?> ChatSerializer;
	public Method ChatSerializer_SERIALIZE;
	public Method ChatSerializer_DESERIALIZE;

	//PacketPlayOutBoss
	public Class<?> PacketPlayOutBoss;
	public Class<Enum> BarColor;
	public Class<Enum> BarStyle;
	public Class<Enum> PacketPlayOutBoss_Action;
	public Constructor<?> newPacketPlayOutBoss;
	public Field PacketPlayOutBoss_UUID;
	public Field PacketPlayOutBoss_ACTION;
	public Field PacketPlayOutBoss_NAME;
	public Field PacketPlayOutBoss_PROGRESS;
	public Field PacketPlayOutBoss_COLOR;
	public Field PacketPlayOutBoss_STYLE;
	public Field PacketPlayOutBoss_DARKEN_SKY;
	public Field PacketPlayOutBoss_PLAY_MUSIC;
	public Field PacketPlayOutBoss_CREATE_FOG;

	//PacketPlayOutChat
	public Class<?> PacketPlayOutChat;
	public Class<Enum> ChatMessageType;
	public Constructor<?> newPacketPlayOutChat;
	public Field PacketPlayOutChat_MESSAGE;
	public Field PacketPlayOutChat_POSITION;
	public Field PacketPlayOutChat_SENDER;

	//PacketPlayOutPlayerListHeaderFooter
	public Class<?> PacketPlayOutPlayerListHeaderFooter;
	public Constructor<?> newPacketPlayOutPlayerListHeaderFooter;
	public Field PacketPlayOutPlayerListHeaderFooter_HEADER;
	public Field PacketPlayOutPlayerListHeaderFooter_FOOTER;

	//PacketPlayOutScoreboardDisplayObjective
	public Class<?> PacketPlayOutScoreboardDisplayObjective;
	public Constructor<?> newPacketPlayOutScoreboardDisplayObjective;
	public Field PacketPlayOutScoreboardDisplayObjective_POSITION;
	public Field PacketPlayOutScoreboardDisplayObjective_OBJECTIVENAME;

	//PacketPlayOutScoreboardObjective
	public Class<?> PacketPlayOutScoreboardObjective;
	public Class<Enum> EnumScoreboardHealthDisplay;
	public Constructor<?> newPacketPlayOutScoreboardObjective;
	public Field PacketPlayOutScoreboardObjective_OBJECTIVENAME;
	public Field PacketPlayOutScoreboardObjective_DISPLAYNAME;
	public Field PacketPlayOutScoreboardObjective_RENDERTYPE;
	public Field PacketPlayOutScoreboardObjective_METHOD;

	//PacketPlayOutScoreboardScore
	public Class<?> PacketPlayOutScoreboardScore;
	public Class<Enum> EnumScoreboardAction;
	public Constructor<?> newPacketPlayOutScoreboardScore0;
	public Constructor<?> newPacketPlayOutScoreboardScore_String;
	public Constructor<?> newPacketPlayOutScoreboardScore_1_13;
	public Field PacketPlayOutScoreboardScore_PLAYER;
	public Field PacketPlayOutScoreboardScore_OBJECTIVENAME;
	public Field PacketPlayOutScoreboardScore_SCORE;
	public Field PacketPlayOutScoreboardScore_ACTION;

	//PacketPlayOutScoreboardTeam
	public Class<?> PacketPlayOutScoreboardTeam;
	public Constructor<?> newPacketPlayOutScoreboardTeam;
	public Field PacketPlayOutScoreboardTeam_NAME;
	public Field PacketPlayOutScoreboardTeam_DISPLAYNAME;
	public Field PacketPlayOutScoreboardTeam_PREFIX;
	public Field PacketPlayOutScoreboardTeam_SUFFIX;
	public Field PacketPlayOutScoreboardTeam_VISIBILITY; //1.8+
	public Field PacketPlayOutScoreboardTeam_CHATFORMAT; //1.13+
	public Field PacketPlayOutScoreboardTeam_COLLISION; //1.9+
	public Field PacketPlayOutScoreboardTeam_PLAYERS;
	public Field PacketPlayOutScoreboardTeam_ACTION;
	public Field PacketPlayOutScoreboardTeam_SIGNATURE;

	//PacketPlayOutPlayerInfo
	public Class<?> PacketPlayOutPlayerInfo;
	public Class<Enum> EnumGamemode;
	public Class<Enum> EnumPlayerInfoAction;
	public Class<?> PlayerInfoData;
	public Constructor<?> newPacketPlayOutPlayerInfo;
	public Constructor<?> newPlayerInfoData;

	public Class<?> GameProfile;
	public Constructor<?> newGameProfile;
	public Field GameProfile_ID;
	public Field GameProfile_NAME;
	public Field GameProfile_PROPERTIES;
	public Class<?> PropertyMap;
	public Method PropertyMap_putAll;

	public Field PacketPlayOutPlayerInfo_ACTION;
	public Field PacketPlayOutPlayerInfo_PLAYERS;

	public Field PlayerInfoData_PING;
	public Field PlayerInfoData_GAMEMODE;
	public Field PlayerInfoData_PROFILE;
	public Field PlayerInfoData_LISTNAME;

	//PacketPlayOutTitle
	public Class<?> PacketPlayOutTitle;
	public Constructor<?> newPacketPlayOutTitle;
	public Class<Enum> EnumTitleAction;

	public Class<?> PacketPlayOutEntityDestroy;
	public Constructor<?> newPacketPlayOutEntityDestroy;
	public Field PacketPlayOutEntityDestroy_ENTITIES;

	public Class<?> PacketPlayInUseEntity;
	public Field PacketPlayInUseEntity_ENTITY;
	public Field PacketPlayInUseEntity_ACTION;

	public Class<?> PacketPlayOutNamedEntitySpawn;
	public Field PacketPlayOutNamedEntitySpawn_ENTITYID;

	public Class<?> PacketPlayOutEntity;
	public Field PacketPlayOutEntity_ENTITYID;

	public Class<?> PacketPlayOutMount;
	public Field PacketPlayOutMount_VEHICLE;
	public Field PacketPlayOutMount_PASSENGERS;

	public Class<?> PacketPlayOutAttachEntity;
	public Field PacketPlayOutAttachEntity_A;
	public Field PacketPlayOutAttachEntity_PASSENGER;
	public Field PacketPlayOutAttachEntity_VEHICLE;

	public Class<?> PacketPlayOutEntityTeleport;
	public Constructor<?> newPacketPlayOutEntityTeleport;
	public Field PacketPlayOutEntityTeleport_ENTITYID;
	public Field PacketPlayOutEntityTeleport_X;
	public Field PacketPlayOutEntityTeleport_Y;
	public Field PacketPlayOutEntityTeleport_Z;
	public Field PacketPlayOutEntityTeleport_YAW;
	public Field PacketPlayOutEntityTeleport_PITCH;

	public Class<?> PacketPlayOutEntityMetadata;
	public Constructor<?> newPacketPlayOutEntityMetadata;
	public Field PacketPlayOutEntityMetadata_LIST;

	public Class<?> PacketPlayOutSpawnEntityLiving;
	public Constructor<?> newPacketPlayOutSpawnEntityLiving;
	public Field PacketPlayOutSpawnEntityLiving_ENTITYID;
	public Field PacketPlayOutSpawnEntityLiving_UUID;
	public Field PacketPlayOutSpawnEntityLiving_ENTITYTYPE;
	public Field PacketPlayOutSpawnEntityLiving_X;
	public Field PacketPlayOutSpawnEntityLiving_Y;
	public Field PacketPlayOutSpawnEntityLiving_Z;
	public Field PacketPlayOutSpawnEntityLiving_YAW;
	public Field PacketPlayOutSpawnEntityLiving_PITCH;
	public Field PacketPlayOutSpawnEntityLiving_DATAWATCHER;

	//DataWatcher
	public Class<?> DataWatcher;
	public Constructor<?> newDataWatcher;
	public Method DataWatcher_REGISTER;

	public Class<?> DataWatcherItem;
	public Constructor<?> newDataWatcherItem;

	public Class<?> DataWatcherObject;
	public Constructor<?> newDataWatcherObject;

	public Class<?> DataWatcherRegistry;

	/**
	 * Creates new instance, initializes required NMS classes and fields
	 * @throws Exception - if something fails
	 */
	public NMSStorage() throws Exception {
		serverPackage = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		minorVersion = Integer.parseInt(serverPackage.split("_")[1]);
		initializeClasses();
		initializeConstructors();
		initializeFields();
		initializeMethods();
	}

	/**
	 * Initializes required NMS classes
	 * @throws Exception - if something fails
	 */
	public void initializeClasses() throws Exception {
		DataWatcher = getNMSClass("DataWatcher");
		EnumChatFormat = (Class<Enum>) getNMSClass("EnumChatFormat");
		IChatBaseComponent = getNMSClass("IChatBaseComponent");
		PacketPlayInUseEntity = getNMSClass("PacketPlayInUseEntity");
		PacketPlayOutChat = getNMSClass("PacketPlayOutChat");
		PacketPlayOutEntity = getNMSClass("PacketPlayOutEntity");
		PacketPlayOutEntityDestroy = getNMSClass("PacketPlayOutEntityDestroy");
		PacketPlayOutEntityMetadata = getNMSClass("PacketPlayOutEntityMetadata");
		PacketPlayOutEntityTeleport = getNMSClass("PacketPlayOutEntityTeleport");
		PacketPlayOutNamedEntitySpawn = getNMSClass("PacketPlayOutNamedEntitySpawn");
		PacketPlayOutScoreboardDisplayObjective = getNMSClass("PacketPlayOutScoreboardDisplayObjective");
		PacketPlayOutScoreboardObjective = getNMSClass("PacketPlayOutScoreboardObjective");
		PacketPlayOutScoreboardScore = getNMSClass("PacketPlayOutScoreboardScore");
		PacketPlayOutScoreboardTeam = getNMSClass("PacketPlayOutScoreboardTeam");
		PacketPlayOutSpawnEntityLiving = getNMSClass("PacketPlayOutSpawnEntityLiving");
		try {
			//v1_8_R2+
			ChatSerializer = getNMSClass("IChatBaseComponent$ChatSerializer");
		} catch (ClassNotFoundException e) {
			//1.7 - 1.8.0
			ChatSerializer = getNMSClass("ChatSerializer");
		}
		if (minorVersion >= 8) {
			//1.8+
			GameProfile = Class.forName("com.mojang.authlib.GameProfile");
			PropertyMap = Class.forName("com.mojang.authlib.properties.PropertyMap");
			PacketPlayOutPlayerInfo = getNMSClass("PacketPlayOutPlayerInfo");
			PacketPlayOutPlayerListHeaderFooter = getNMSClass("PacketPlayOutPlayerListHeaderFooter");
			PacketPlayOutTitle = getNMSClass("PacketPlayOutTitle");
			try {
				//v1_8_R2+
				EnumPlayerInfoAction = (Class<Enum>) getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
				PlayerInfoData = getNMSClass("PacketPlayOutPlayerInfo$PlayerInfoData");
				EnumScoreboardHealthDisplay = (Class<Enum>) getNMSClass("IScoreboardCriteria$EnumScoreboardHealthDisplay");
				EnumTitleAction = (Class<Enum>) getNMSClass("PacketPlayOutTitle$EnumTitleAction");
			} catch (ClassNotFoundException e) {
				//v1_8_R1
				EnumPlayerInfoAction = (Class<Enum>) getNMSClass("EnumPlayerInfoAction");
				PlayerInfoData = getNMSClass("PlayerInfoData");
				EnumScoreboardHealthDisplay = (Class<Enum>) getNMSClass("EnumScoreboardHealthDisplay");
				EnumTitleAction = (Class<Enum>) getNMSClass("EnumTitleAction");
			}
			try {
				EnumGamemode = (Class<Enum>) getNMSClass("EnumGamemode");
			} catch (ClassNotFoundException e) {
				//v1_8_R2 - v1_9_R2
				EnumGamemode = (Class<Enum>) getNMSClass("WorldSettings$EnumGamemode");
			}
		}
		if (minorVersion >= 9) {
			//1.9+
			DataWatcherItem = getNMSClass("DataWatcher$Item");
			DataWatcherObject = getNMSClass("DataWatcherObject");
			DataWatcherRegistry = getNMSClass("DataWatcherRegistry");
			PacketPlayOutBoss = getNMSClass("PacketPlayOutBoss");
			BarColor = (Class<Enum>) getNMSClass("BossBattle$BarColor");
			BarStyle = (Class<Enum>) getNMSClass("BossBattle$BarStyle");
			PacketPlayOutBoss_Action = (Class<Enum>) getNMSClass("PacketPlayOutBoss$Action");
			PacketPlayOutMount = getNMSClass("PacketPlayOutMount");
			(PacketPlayOutMount_VEHICLE = PacketPlayOutMount.getDeclaredField("a")).setAccessible(true);
			(PacketPlayOutMount_PASSENGERS = PacketPlayOutMount.getDeclaredField("b")).setAccessible(true);
		} else {
			PacketPlayOutAttachEntity = getNMSClass("PacketPlayOutAttachEntity");
			(PacketPlayOutAttachEntity_A = PacketPlayOutAttachEntity.getDeclaredField("a")).setAccessible(true);
			(PacketPlayOutAttachEntity_PASSENGER = PacketPlayOutAttachEntity.getDeclaredField("b")).setAccessible(true);
			(PacketPlayOutAttachEntity_VEHICLE = PacketPlayOutAttachEntity.getDeclaredField("c")).setAccessible(true);
			try {
				//v1_8_R2, v1_8_R3
				DataWatcherItem = getNMSClass("DataWatcher$WatchableObject");
			} catch (ClassNotFoundException e) {
				//v1_8_R1-
				DataWatcherItem = getNMSClass("WatchableObject");
			}
		}
		if (minorVersion >= 12) {
			//1.12+
			ChatMessageType = (Class<Enum>) getNMSClass("ChatMessageType");
		}
		if (minorVersion >= 13) {
			EnumScoreboardAction = (Class<Enum>) getNMSClass("ScoreboardServer$Action");
		} else if (minorVersion >= 8) {
			try {
				//v1_8_R2+
				EnumScoreboardAction = (Class<Enum>) getNMSClass("PacketPlayOutScoreboardScore$EnumScoreboardAction");
			} catch (ClassNotFoundException e) {
				//v1_8_R1
				EnumScoreboardAction = (Class<Enum>) getNMSClass("EnumScoreboardAction");
			}
		}
	}

	/**
	 * Initializes required NMS constructors
	 * @throws Exception - if something fails
	 */
	public void initializeConstructors() throws Exception {
		newDataWatcher = DataWatcher.getConstructor(getNMSClass("Entity"));
		newDataWatcherItem = DataWatcherItem.getConstructors()[0];
		newPacketPlayOutChat = PacketPlayOutChat.getConstructor();
		newPacketPlayOutScoreboardDisplayObjective = PacketPlayOutScoreboardDisplayObjective.getConstructor();
		newPacketPlayOutScoreboardObjective = PacketPlayOutScoreboardObjective.getConstructor();
		newPacketPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.getConstructor();
		newPacketPlayOutEntityDestroy = PacketPlayOutEntityDestroy.getConstructor(int[].class);
		newPacketPlayOutEntityMetadata = PacketPlayOutEntityMetadata.getConstructor(int.class, getNMSClass("DataWatcher"), boolean.class);
		newPacketPlayOutEntityTeleport = PacketPlayOutEntityTeleport.getConstructor();
		newPacketPlayOutSpawnEntityLiving = PacketPlayOutSpawnEntityLiving.getConstructor();
		if (minorVersion >= 8) {
			//1.8+
			newGameProfile = GameProfile.getConstructor(UUID.class, String.class);
			newPacketPlayOutPlayerListHeaderFooter = PacketPlayOutPlayerListHeaderFooter.getConstructor();
			newPacketPlayOutTitle = PacketPlayOutTitle.getConstructor(EnumTitleAction, IChatBaseComponent, int.class, int.class, int.class);
			newPacketPlayOutPlayerInfo = PacketPlayOutPlayerInfo.getConstructor();
			try {
				newPlayerInfoData = PlayerInfoData.getConstructor(PacketPlayOutPlayerInfo, GameProfile, int.class, EnumGamemode, IChatBaseComponent);
			} catch (Exception e) {
				//1.8.8 paper
				newPlayerInfoData = PlayerInfoData.getConstructor(GameProfile, int.class, EnumGamemode, IChatBaseComponent);
			}
		}
		if (minorVersion >= 9) {
			newDataWatcherObject = DataWatcherObject.getConstructors()[0];
			newPacketPlayOutBoss = PacketPlayOutBoss.getConstructor();
		}
		if (minorVersion >= 13) {
			newPacketPlayOutScoreboardScore_1_13 = PacketPlayOutScoreboardScore.getConstructor(EnumScoreboardAction, String.class, String.class, int.class);
		} else {
			newPacketPlayOutScoreboardScore0 = PacketPlayOutScoreboardScore.getConstructor();
			newPacketPlayOutScoreboardScore_String = PacketPlayOutScoreboardScore.getConstructor(String.class);
		}
	}

	/**
	 * Initializes required NMS fields
	 * @throws Exception - if something fails
	 */
	public void initializeFields() throws Exception {
		PING = getNMSClass("EntityPlayer").getDeclaredField("ping");
		PLAYER_CONNECTION = getNMSClass("EntityPlayer").getDeclaredField("playerConnection");
		NETWORK_MANAGER = PLAYER_CONNECTION.getType().getField("networkManager");

		(PacketPlayOutChat_MESSAGE = PacketPlayOutChat.getDeclaredField("a")).setAccessible(true);

		(PacketPlayOutScoreboardDisplayObjective_POSITION = PacketPlayOutScoreboardDisplayObjective.getDeclaredField("a")).setAccessible(true);
		(PacketPlayOutScoreboardDisplayObjective_OBJECTIVENAME = PacketPlayOutScoreboardDisplayObjective.getDeclaredField("b")).setAccessible(true);

		(PacketPlayOutScoreboardObjective_OBJECTIVENAME = PacketPlayOutScoreboardObjective.getDeclaredField("a")).setAccessible(true);
		(PacketPlayOutScoreboardObjective_DISPLAYNAME = PacketPlayOutScoreboardObjective.getDeclaredField("b")).setAccessible(true);

		(PacketPlayOutScoreboardScore_PLAYER = PacketPlayOutScoreboardScore.getDeclaredField("a")).setAccessible(true);
		(PacketPlayOutScoreboardScore_OBJECTIVENAME = PacketPlayOutScoreboardScore.getDeclaredField("b")).setAccessible(true);
		(PacketPlayOutScoreboardScore_SCORE = PacketPlayOutScoreboardScore.getDeclaredField("c")).setAccessible(true);
		(PacketPlayOutScoreboardScore_ACTION = PacketPlayOutScoreboardScore.getDeclaredField("d")).setAccessible(true);

		(PacketPlayOutScoreboardTeam_NAME = PacketPlayOutScoreboardTeam.getDeclaredField("a")).setAccessible(true);
		(PacketPlayOutScoreboardTeam_DISPLAYNAME = PacketPlayOutScoreboardTeam.getDeclaredField("b")).setAccessible(true);
		(PacketPlayOutScoreboardTeam_PREFIX = PacketPlayOutScoreboardTeam.getDeclaredField("c")).setAccessible(true);
		(PacketPlayOutScoreboardTeam_SUFFIX = PacketPlayOutScoreboardTeam.getDeclaredField("d")).setAccessible(true);

		(PacketPlayOutEntityTeleport_ENTITYID = PacketPlayOutEntityTeleport.getDeclaredField("a")).setAccessible(true);
		(PacketPlayOutEntityTeleport_X = PacketPlayOutEntityTeleport.getDeclaredField("b")).setAccessible(true);
		(PacketPlayOutEntityTeleport_Y = PacketPlayOutEntityTeleport.getDeclaredField("c")).setAccessible(true);
		(PacketPlayOutEntityTeleport_Z = PacketPlayOutEntityTeleport.getDeclaredField("d")).setAccessible(true);
		(PacketPlayOutEntityTeleport_YAW = PacketPlayOutEntityTeleport.getDeclaredField("e")).setAccessible(true);
		(PacketPlayOutEntityTeleport_PITCH = PacketPlayOutEntityTeleport.getDeclaredField("f")).setAccessible(true);

		(PacketPlayInUseEntity_ENTITY = PacketPlayInUseEntity.getDeclaredField("a")).setAccessible(true);
		(PacketPlayInUseEntity_ACTION = PacketPlayInUseEntity.getDeclaredField("action")).setAccessible(true);
		(PacketPlayOutEntity_ENTITYID = PacketPlayOutEntity.getDeclaredField("a")).setAccessible(true);
		(PacketPlayOutEntityDestroy_ENTITIES = PacketPlayOutEntityDestroy.getDeclaredField("a")).setAccessible(true);
		(PacketPlayOutNamedEntitySpawn_ENTITYID = PacketPlayOutNamedEntitySpawn.getDeclaredField("a")).setAccessible(true);
		(PacketPlayOutEntityMetadata_LIST = PacketPlayOutEntityMetadata.getDeclaredField("b")).setAccessible(true);

		(PacketPlayOutSpawnEntityLiving_ENTITYID = PacketPlayOutSpawnEntityLiving.getDeclaredField("a")).setAccessible(true);

		if (minorVersion >= 8) {
			//1.8+
			CHANNEL = getFields(getNMSClass("NetworkManager"), Channel.class).get(0);

			(PacketPlayOutChat_POSITION = PacketPlayOutChat.getDeclaredField("b")).setAccessible(true);

			(PacketPlayOutScoreboardObjective_RENDERTYPE = PacketPlayOutScoreboardObjective.getDeclaredField("c")).setAccessible(true);
			(PacketPlayOutScoreboardObjective_METHOD = PacketPlayOutScoreboardObjective.getDeclaredField("d")).setAccessible(true);

			(GameProfile_ID = GameProfile.getDeclaredField("id")).setAccessible(true);
			(GameProfile_NAME = GameProfile.getDeclaredField("name")).setAccessible(true);
			(GameProfile_PROPERTIES = GameProfile.getDeclaredField("properties")).setAccessible(true);
			(PacketPlayOutPlayerInfo_ACTION = PacketPlayOutPlayerInfo.getDeclaredField("a")).setAccessible(true);
			(PacketPlayOutPlayerInfo_PLAYERS = PacketPlayOutPlayerInfo.getDeclaredField("b")).setAccessible(true);
			(PlayerInfoData_PING = PlayerInfoData.getDeclaredField("b")).setAccessible(true);
			(PlayerInfoData_GAMEMODE = PlayerInfoData.getDeclaredField("c")).setAccessible(true);
			(PlayerInfoData_PROFILE = PlayerInfoData.getDeclaredField("d")).setAccessible(true);
			(PlayerInfoData_LISTNAME = PlayerInfoData.getDeclaredField("e")).setAccessible(true);

			List<Field> fields = getFields(PacketPlayOutPlayerListHeaderFooter, IChatBaseComponent);
			PacketPlayOutPlayerListHeaderFooter_HEADER = fields.get(0);
			PacketPlayOutPlayerListHeaderFooter_FOOTER = fields.get(1);
		} else {
			//1.7-
			(PacketPlayOutScoreboardObjective_METHOD = PacketPlayOutScoreboardObjective.getDeclaredField("c")).setAccessible(true);
		}
		if (minorVersion >= 9) {
			//1.9+
			(PacketPlayOutBoss_UUID = PacketPlayOutBoss.getDeclaredField("a")).setAccessible(true);
			(PacketPlayOutBoss_ACTION = PacketPlayOutBoss.getDeclaredField("b")).setAccessible(true);
			(PacketPlayOutBoss_NAME = PacketPlayOutBoss.getDeclaredField("c")).setAccessible(true);
			(PacketPlayOutBoss_PROGRESS = PacketPlayOutBoss.getDeclaredField("d")).setAccessible(true);
			(PacketPlayOutBoss_COLOR = PacketPlayOutBoss.getDeclaredField("e")).setAccessible(true);
			(PacketPlayOutBoss_STYLE = PacketPlayOutBoss.getDeclaredField("f")).setAccessible(true);
			(PacketPlayOutBoss_DARKEN_SKY = PacketPlayOutBoss.getDeclaredField("g")).setAccessible(true);
			(PacketPlayOutBoss_PLAY_MUSIC = PacketPlayOutBoss.getDeclaredField("h")).setAccessible(true);
			(PacketPlayOutBoss_CREATE_FOG = PacketPlayOutBoss.getDeclaredField("i")).setAccessible(true);

			(PacketPlayOutScoreboardTeam_VISIBILITY = PacketPlayOutScoreboardTeam.getDeclaredField("e")).setAccessible(true);
			(PacketPlayOutScoreboardTeam_COLLISION = PacketPlayOutScoreboardTeam.getDeclaredField("f")).setAccessible(true);
			(PacketPlayOutScoreboardTeam_PLAYERS = PacketPlayOutScoreboardTeam.getDeclaredField("h")).setAccessible(true);
			(PacketPlayOutScoreboardTeam_ACTION = PacketPlayOutScoreboardTeam.getDeclaredField("i")).setAccessible(true);
			(PacketPlayOutScoreboardTeam_SIGNATURE = PacketPlayOutScoreboardTeam.getDeclaredField("j")).setAccessible(true);

			(PacketPlayOutSpawnEntityLiving_UUID = PacketPlayOutSpawnEntityLiving.getDeclaredField("b")).setAccessible(true);
			(PacketPlayOutSpawnEntityLiving_ENTITYTYPE = PacketPlayOutSpawnEntityLiving.getDeclaredField("c")).setAccessible(true);
			(PacketPlayOutSpawnEntityLiving_X = PacketPlayOutSpawnEntityLiving.getDeclaredField("d")).setAccessible(true);
			(PacketPlayOutSpawnEntityLiving_Y = PacketPlayOutSpawnEntityLiving.getDeclaredField("e")).setAccessible(true);
			(PacketPlayOutSpawnEntityLiving_Z = PacketPlayOutSpawnEntityLiving.getDeclaredField("f")).setAccessible(true);
			(PacketPlayOutSpawnEntityLiving_YAW = PacketPlayOutSpawnEntityLiving.getDeclaredField("j")).setAccessible(true);
			(PacketPlayOutSpawnEntityLiving_PITCH = PacketPlayOutSpawnEntityLiving.getDeclaredField("k")).setAccessible(true);
		} else {
			//1.8-
			(PacketPlayOutSpawnEntityLiving_ENTITYTYPE = PacketPlayOutSpawnEntityLiving.getDeclaredField("b")).setAccessible(true);
			(PacketPlayOutSpawnEntityLiving_X = PacketPlayOutSpawnEntityLiving.getDeclaredField("c")).setAccessible(true);
			(PacketPlayOutSpawnEntityLiving_Y = PacketPlayOutSpawnEntityLiving.getDeclaredField("d")).setAccessible(true);
			(PacketPlayOutSpawnEntityLiving_Z = PacketPlayOutSpawnEntityLiving.getDeclaredField("e")).setAccessible(true);
			(PacketPlayOutSpawnEntityLiving_YAW = PacketPlayOutSpawnEntityLiving.getDeclaredField("i")).setAccessible(true);
			(PacketPlayOutSpawnEntityLiving_PITCH = PacketPlayOutSpawnEntityLiving.getDeclaredField("j")).setAccessible(true);
			if (minorVersion == 8) {
				//1.8.x
				(PacketPlayOutScoreboardTeam_VISIBILITY = PacketPlayOutScoreboardTeam.getDeclaredField("e")).setAccessible(true);
				(PacketPlayOutScoreboardTeam_PLAYERS = PacketPlayOutScoreboardTeam.getDeclaredField("g")).setAccessible(true);
				(PacketPlayOutScoreboardTeam_ACTION = PacketPlayOutScoreboardTeam.getDeclaredField("h")).setAccessible(true);
				(PacketPlayOutScoreboardTeam_SIGNATURE = PacketPlayOutScoreboardTeam.getDeclaredField("i")).setAccessible(true);
			} else {
				//1.7.x
				(PacketPlayOutScoreboardTeam_PLAYERS = PacketPlayOutScoreboardTeam.getDeclaredField("e")).setAccessible(true);
				(PacketPlayOutScoreboardTeam_ACTION = PacketPlayOutScoreboardTeam.getDeclaredField("f")).setAccessible(true);
				(PacketPlayOutScoreboardTeam_SIGNATURE = PacketPlayOutScoreboardTeam.getDeclaredField("g")).setAccessible(true);
			}
		}
		if (minorVersion >= 13) {
			//1.13+
			(PacketPlayOutScoreboardTeam_CHATFORMAT = PacketPlayOutScoreboardTeam.getDeclaredField("g")).setAccessible(true);
		}

		if (minorVersion <= 14) {
			(PacketPlayOutSpawnEntityLiving_DATAWATCHER = getFields(PacketPlayOutSpawnEntityLiving, DataWatcher).get(0)).setAccessible(true);
		}

		if (minorVersion >= 16) {
			//1.16+
			(PacketPlayOutChat_SENDER = PacketPlayOutChat.getDeclaredField("c")).setAccessible(true);
		}
	}

	/**
	 * Initializes required NMS methods
	 * @throws Exception - if something fails
	 */
	public void initializeMethods() throws Exception {
		if (minorVersion >= 8) {
			for (Method m : PropertyMap.getMethods()) {
				if (m.getName().equals("putAll") && m.getParameterCount() == 1) PropertyMap_putAll = m;
			}
			if (PropertyMap_putAll == null) throw new IllegalStateException("putAll method not found");
		}
		ChatSerializer_SERIALIZE = ChatSerializer.getMethod("a", IChatBaseComponent);
		ChatSerializer_DESERIALIZE = ChatSerializer.getMethod("a", String.class);
		getHandle = Class.forName("org.bukkit.craftbukkit." + serverPackage + ".entity.CraftPlayer").getMethod("getHandle");
		sendPacket = getNMSClass("PlayerConnection").getMethod("sendPacket", getNMSClass("Packet"));
		
		if (minorVersion >= 8) {
			getProfile = getNMSClass("EntityHuman").getMethod("getProfile");
		}
		
		if (minorVersion >= 9) {
			//1.9+
			DataWatcher_REGISTER = DataWatcher.getMethod("register", DataWatcherObject, Object.class);
		} else {
			//1.8-
			DataWatcher_REGISTER = DataWatcher.getMethod("a", int.class, Object.class);
		}
	}

	private Class<?> getNMSClass(String name) throws ClassNotFoundException {
		return Class.forName("net.minecraft.server." + serverPackage + "." + name);
	}

	private List<Field> getFields(Class<?> clazz, Class<?> type){
		List<Field> list = new ArrayList<Field>();
		if (clazz == null) return list;
		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			if (field.getType() == type) list.add(field);
		}
		return list;
	}
}