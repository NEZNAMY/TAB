package me.neznamy.tab.platforms.bukkit.nms;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;

import io.netty.channel.Channel;
import me.neznamy.tab.platforms.bukkit.Main;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherRegistry;

@SuppressWarnings({"unchecked", "rawtypes"})
public class NMSStorage {

	//instance of this class
	private static NMSStorage instance;
	
	//field mapping changes in thermos
	private Map<String, String> thermosFieldMappings = new HashMap<String, String>();
	
	//data watcher registry
	public DataWatcherRegistry dataWatcherRegistry;
	
	//server package, such as "v1_16_R3"
	private String serverPackage;
	
	//server minor version such as "16"
	public int minorVersion;

	public Field PING;
	public Field PLAYER_CONNECTION;
	public Field NETWORK_MANAGER;
	public Field CHANNEL;
	public Method getHandle;
	public Method getProfile;
	public Method sendPacket;

	//chat
	public Class<Enum> EnumChatFormat;
	public Class<?> IChatBaseComponent;
	public Class<?> ChatBaseComponent;
	public Class<?> ChatSerializer;
	public Class<?> EnumClickAction;
	public Class<?> EnumHoverAction;
	public Class<?> ChatModifier;
	public Class<?> ChatComponentText;
	public Class<?> ChatClickable;
	public Class<?> ChatHoverable;
	public Class<?> ChatHexColor;
	public Constructor<?> newChatComponentText;
	public Constructor<?> newChatClickable;
	public Constructor<?> newChatHoverable;
	public Constructor<?> newChatModifier;
	public Field ChatBaseComponent_extra;
	public Field ChatBaseComponent_modifier;
	public Field ChatClickable_action;
	public Field ChatClickable_value;
	public Field ChatHexColor_name;
	public Field ChatHexColor_rgb;
	public Field ChatHoverable_action;
	public Field ChatHoverable_value;
	public Field ChatModifier_color;
	public Field ChatModifier_bold;
	public Field ChatModifier_italic;
	public Field ChatModifier_underlined;
	public Field ChatModifier_strikethrough;
	public Field ChatModifier_obfuscated;
	public Field ChatModifier_clickEvent;
	public Field ChatModifier_hoverEvent;
	public Field ChatComponentText_text;
	public Method ChatSerializer_DESERIALIZE;
	public Method EnumClickAction_a;
	public Method EnumHoverAction_a;
	public Method ChatHexColor_ofInt;
	public Method ChatHexColor_ofString;
	public Method ChatComponentText_addSibling;

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

	public Class<?> PacketPlayInUseEntity;
	public Field PacketPlayInUseEntity_ENTITY;
	public Field PacketPlayInUseEntity_ACTION;

	public Class<?> PacketPlayOutEntity;
	public Field PacketPlayOutEntity_ENTITYID;

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
	public Field DataWatcherItem_TYPE;
	public Field DataWatcherItem_VALUE;

	public Class<?> DataWatcherObject;
	public Constructor<?> newDataWatcherObject;
	public Field DataWatcherObject_SLOT;
	public Field DataWatcherObject_SERIALIZER;

	public Class<?> DataWatcherRegistry;

	/**
	 * Creates new instance, initializes required NMS classes and fields
	 * @throws Exception - if something fails
	 */
	public NMSStorage() throws Exception {
		serverPackage = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		minorVersion = Integer.parseInt(serverPackage.split("_")[1]);
		loadThermosMappings();
		initializeClasses();
		initializeConstructors();
		initializeFields();
		initializeMethods();
		dataWatcherRegistry = new DataWatcherRegistry(DataWatcherRegistry);
	}
	
	/**
	 * Sets new instance
	 * @param instance - new instance
	 */
	public static void setInstance(NMSStorage instance) {
		NMSStorage.instance = instance;
	}
	
	/**
	 * Returns instance
	 * @return instance
	 */
	public static NMSStorage getInstance() {
		return instance;
	}

	/**
	 * Puts thermos mapping changes into map
	 */
	private void loadThermosMappings() {
		thermosFieldMappings.put("ping", "field_71138_i");
		thermosFieldMappings.put("playerConnection", "field_71135_a");
		thermosFieldMappings.put("networkManager", "field_147371_a");
	}
	
	/**
	 * Initializes required NMS classes
	 * @throws Exception - if something fails
	 */
	public void initializeClasses() throws Exception {
		ChatModifier = getNMSClass("ChatModifier");
		ChatBaseComponent = getNMSClass("ChatBaseComponent");
		ChatComponentText = getNMSClass("ChatComponentText");
		ChatClickable = getNMSClass("ChatClickable");
		ChatHoverable = getNMSClass("ChatHoverable");
		DataWatcher = getNMSClass("DataWatcher");
		//1.9+; v1_8_R2 & v1_8_R3; 1.7 - v1_8_R1
		DataWatcherItem = getNMSClass("DataWatcher$Item", "DataWatcher$WatchableObject", "WatchableObject");
		EnumChatFormat = (Class<Enum>) getNMSClass("EnumChatFormat");
		IChatBaseComponent = getNMSClass("IChatBaseComponent");
		PacketPlayInUseEntity = getNMSClass("PacketPlayInUseEntity");
		PacketPlayOutChat = getNMSClass("PacketPlayOutChat");
		PacketPlayOutEntity = getNMSClass("PacketPlayOutEntity");
		PacketPlayOutEntityDestroy = getNMSClass("PacketPlayOutEntityDestroy");
		PacketPlayOutEntityMetadata = getNMSClass("PacketPlayOutEntityMetadata");
		PacketPlayOutEntityTeleport = getNMSClass("PacketPlayOutEntityTeleport");
		PacketPlayOutScoreboardDisplayObjective = getNMSClass("PacketPlayOutScoreboardDisplayObjective");
		PacketPlayOutScoreboardObjective = getNMSClass("PacketPlayOutScoreboardObjective");
		PacketPlayOutScoreboardScore = getNMSClass("PacketPlayOutScoreboardScore");
		PacketPlayOutScoreboardTeam = getNMSClass("PacketPlayOutScoreboardTeam");
		PacketPlayOutSpawnEntityLiving = getNMSClass("PacketPlayOutSpawnEntityLiving");
		
		//v1_8_R2+; //v1.8.R1-
		ChatSerializer = getNMSClass("IChatBaseComponent$ChatSerializer", "ChatSerializer");
		EnumClickAction = getNMSClass("ChatClickable$EnumClickAction", "EnumClickAction");
		EnumHoverAction = getNMSClass("ChatHoverable$EnumHoverAction", "EnumHoverAction");
		if (minorVersion >= 8) {
			GameProfile = Class.forName("com.mojang.authlib.GameProfile");
			PropertyMap = Class.forName("com.mojang.authlib.properties.PropertyMap");
			PacketPlayOutPlayerInfo = getNMSClass("PacketPlayOutPlayerInfo");
			PacketPlayOutPlayerListHeaderFooter = getNMSClass("PacketPlayOutPlayerListHeaderFooter");
			PacketPlayOutTitle = getNMSClass("PacketPlayOutTitle");
			
			//v1_8_R2+; v1_8_R1
			EnumPlayerInfoAction = (Class<Enum>) getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction", "EnumPlayerInfoAction");
			PlayerInfoData = getNMSClass("PacketPlayOutPlayerInfo$PlayerInfoData", "PlayerInfoData");
			EnumScoreboardHealthDisplay = (Class<Enum>) getNMSClass("IScoreboardCriteria$EnumScoreboardHealthDisplay", "EnumScoreboardHealthDisplay");
			EnumTitleAction = (Class<Enum>) getNMSClass("PacketPlayOutTitle$EnumTitleAction", "EnumTitleAction");
			
			//everything; v1_8_R2 - v1_9_R2
			EnumGamemode = (Class<Enum>) getNMSClass("EnumGamemode", "WorldSettings$EnumGamemode");
			
			//1.13+; 1_8_R2 - 1.12; 1_8_R1
			EnumScoreboardAction = (Class<Enum>) getNMSClass("ScoreboardServer$Action", "PacketPlayOutScoreboardScore$EnumScoreboardAction", "EnumScoreboardAction");
		}
		if (minorVersion >= 9) {
			DataWatcherObject = getNMSClass("DataWatcherObject");
			DataWatcherRegistry = getNMSClass("DataWatcherRegistry");
			PacketPlayOutBoss = getNMSClass("PacketPlayOutBoss");
			BarColor = (Class<Enum>) getNMSClass("BossBattle$BarColor");
			BarStyle = (Class<Enum>) getNMSClass("BossBattle$BarStyle");
			PacketPlayOutBoss_Action = (Class<Enum>) getNMSClass("PacketPlayOutBoss$Action");
		}
		if (minorVersion >= 12) {
			ChatMessageType = (Class<Enum>) getNMSClass("ChatMessageType");
		}
		if (minorVersion >= 16) {
			ChatHexColor = getNMSClass("ChatHexColor");
		}
	}

	/**
	 * Initializes required NMS constructors
	 * @throws Exception - if something fails
	 */
	public void initializeConstructors() throws Exception {
		newChatComponentText = ChatComponentText.getConstructor(String.class);
		newChatClickable = ChatClickable.getConstructor(EnumClickAction, String.class);
		newDataWatcher = DataWatcher.getConstructor(getNMSClass("Entity"));
		newDataWatcherItem = DataWatcherItem.getConstructors()[0];
		newPacketPlayOutChat = PacketPlayOutChat.getConstructor();
		newPacketPlayOutScoreboardDisplayObjective = PacketPlayOutScoreboardDisplayObjective.getConstructor();
		newPacketPlayOutScoreboardObjective = PacketPlayOutScoreboardObjective.getConstructor();
		newPacketPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.getConstructor();
		newPacketPlayOutEntityDestroy = PacketPlayOutEntityDestroy.getConstructor(int[].class);
		newPacketPlayOutEntityMetadata = PacketPlayOutEntityMetadata.getConstructor(int.class, DataWatcher, boolean.class);
		newPacketPlayOutEntityTeleport = PacketPlayOutEntityTeleport.getConstructor();
		newPacketPlayOutSpawnEntityLiving = PacketPlayOutSpawnEntityLiving.getConstructor();
		if (minorVersion >= 8) {
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
		if (minorVersion >= 16) {
			newChatHoverable = ChatHoverable.getConstructor(EnumHoverAction, Object.class);
			newChatModifier = getConstructor(ChatModifier, 10);
		} else {
			newChatHoverable = ChatHoverable.getConstructor(EnumHoverAction, IChatBaseComponent);
			newChatModifier = ChatModifier.getConstructor();
		}
	}

	/**
	 * Initializes required NMS fields
	 * @throws Exception - if something fails
	 */
	public void initializeFields() throws Exception {
		ChatBaseComponent_modifier = getFields(ChatBaseComponent, ChatModifier).get(0);
		ChatComponentText_text = getFields(ChatComponentText, String.class).get(0);
		ChatClickable_action = getField(ChatClickable, "a");
		ChatClickable_value = getField(ChatClickable, "b");
		try {
			//1.14.4+
			ChatBaseComponent_extra = getField(ChatBaseComponent, "siblings");
			ChatModifier_color = getField(ChatModifier, "color");
			ChatModifier_bold = getField(ChatModifier, "bold");
			ChatModifier_italic = getField(ChatModifier, "italic");
			ChatModifier_underlined = getField(ChatModifier, "underlined");
			ChatModifier_strikethrough = getField(ChatModifier, "strikethrough");
			ChatModifier_obfuscated = getField(ChatModifier, "obfuscated");
			ChatModifier_clickEvent = getField(ChatModifier, "clickEvent");
			ChatModifier_hoverEvent = getField(ChatModifier, "hoverEvent");
		} catch (NoSuchFieldException e) {
			//1.14.3-
			ChatBaseComponent_extra = getField(ChatBaseComponent, "a");
			ChatModifier_color = getField(ChatModifier, "b");
			ChatModifier_bold = getField(ChatModifier, "c");
			ChatModifier_italic = getField(ChatModifier, "d");
			ChatModifier_underlined = getField(ChatModifier, "e");
			ChatModifier_strikethrough = getField(ChatModifier, "f");
			ChatModifier_obfuscated = getField(ChatModifier, "g");
			ChatModifier_clickEvent = getField(ChatModifier, "h");
			ChatModifier_hoverEvent = getField(ChatModifier, "i");
		}
		
		PING = getField(getNMSClass("EntityPlayer"), "ping");
		PLAYER_CONNECTION = getField(getNMSClass("EntityPlayer"), "playerConnection");
		NETWORK_MANAGER = getField(PLAYER_CONNECTION.getType(), "networkManager");

		PacketPlayOutChat_MESSAGE = getField(PacketPlayOutChat, "a");

		PacketPlayOutScoreboardDisplayObjective_POSITION = getField(PacketPlayOutScoreboardDisplayObjective, "a");
		PacketPlayOutScoreboardDisplayObjective_OBJECTIVENAME = getField(PacketPlayOutScoreboardDisplayObjective, "b");

		PacketPlayOutScoreboardObjective_OBJECTIVENAME = getField(PacketPlayOutScoreboardObjective, "a");
		PacketPlayOutScoreboardObjective_DISPLAYNAME = getField(PacketPlayOutScoreboardObjective, "b");

		PacketPlayOutScoreboardScore_PLAYER = getField(PacketPlayOutScoreboardScore, "a");
		PacketPlayOutScoreboardScore_OBJECTIVENAME = getField(PacketPlayOutScoreboardScore, "b");
		PacketPlayOutScoreboardScore_SCORE = getField(PacketPlayOutScoreboardScore, "c");
		PacketPlayOutScoreboardScore_ACTION = getField(PacketPlayOutScoreboardScore, "d");

		PacketPlayOutScoreboardTeam_NAME = getField(PacketPlayOutScoreboardTeam, "a");
		PacketPlayOutScoreboardTeam_DISPLAYNAME = getField(PacketPlayOutScoreboardTeam, "b");
		PacketPlayOutScoreboardTeam_PREFIX = getField(PacketPlayOutScoreboardTeam, "c");
		PacketPlayOutScoreboardTeam_SUFFIX = getField(PacketPlayOutScoreboardTeam, "d");

		PacketPlayOutEntityTeleport_ENTITYID = getField(PacketPlayOutEntityTeleport, "a");
		PacketPlayOutEntityTeleport_X = getField(PacketPlayOutEntityTeleport, "b");
		PacketPlayOutEntityTeleport_Y = getField(PacketPlayOutEntityTeleport, "c");
		PacketPlayOutEntityTeleport_Z = getField(PacketPlayOutEntityTeleport, "d");
		PacketPlayOutEntityTeleport_YAW = getField(PacketPlayOutEntityTeleport, "e");
		PacketPlayOutEntityTeleport_PITCH = getField(PacketPlayOutEntityTeleport, "f");

		PacketPlayOutEntity_ENTITYID = getField(PacketPlayOutEntity, "a");
		PacketPlayOutEntityMetadata_LIST = getField(PacketPlayOutEntityMetadata, "b");

		PacketPlayOutSpawnEntityLiving_ENTITYID = getField(PacketPlayOutSpawnEntityLiving, "a");
		
		PacketPlayInUseEntity_ENTITY = getField(PacketPlayInUseEntity, "a");

		if (minorVersion >= 8) {
			//1.8+
			CHANNEL = getFields(getNMSClass("NetworkManager"), Channel.class).get(0);

			PacketPlayOutChat_POSITION = getField(PacketPlayOutChat, "b");

			PacketPlayOutScoreboardObjective_RENDERTYPE = getField(PacketPlayOutScoreboardObjective, "c");
			PacketPlayOutScoreboardObjective_METHOD = getField(PacketPlayOutScoreboardObjective, "d");

			GameProfile_ID = getField(GameProfile, "id");
			GameProfile_NAME = getField(GameProfile, "name");
			GameProfile_PROPERTIES = getField(GameProfile, "properties");
			PacketPlayOutPlayerInfo_ACTION = getField(PacketPlayOutPlayerInfo, "a");
			PacketPlayOutPlayerInfo_PLAYERS = getField(PacketPlayOutPlayerInfo, "b");
			PlayerInfoData_PING = getField(PlayerInfoData, "b");
			PlayerInfoData_GAMEMODE = getField(PlayerInfoData, "c");
			PlayerInfoData_PROFILE = getField(PlayerInfoData, "d");
			PlayerInfoData_LISTNAME = getField(PlayerInfoData, "e");

			List<Field> fields = getFields(PacketPlayOutPlayerListHeaderFooter, IChatBaseComponent);
			PacketPlayOutPlayerListHeaderFooter_HEADER = fields.get(0);
			PacketPlayOutPlayerListHeaderFooter_FOOTER = fields.get(1);
		} else {
			//1.7-
			PacketPlayOutScoreboardObjective_METHOD = getField(PacketPlayOutScoreboardObjective, "c");
		}
		if (minorVersion >= 9) {
			//1.9+
			DataWatcherItem_TYPE = getField(DataWatcherItem, "a");
			DataWatcherItem_VALUE = getField(DataWatcherItem, "b");
			DataWatcherObject_SLOT = getField(DataWatcherObject, "a");
			DataWatcherObject_SERIALIZER = getField(DataWatcherObject, "b");
			
			PacketPlayOutBoss_UUID = getField(PacketPlayOutBoss, "a");
			PacketPlayOutBoss_ACTION = getField(PacketPlayOutBoss, "b");
			PacketPlayOutBoss_NAME = getField(PacketPlayOutBoss, "c");
			PacketPlayOutBoss_PROGRESS = getField(PacketPlayOutBoss, "d");
			PacketPlayOutBoss_COLOR = getField(PacketPlayOutBoss, "e");
			PacketPlayOutBoss_STYLE = getField(PacketPlayOutBoss, "f");
			PacketPlayOutBoss_DARKEN_SKY = getField(PacketPlayOutBoss, "g");
			PacketPlayOutBoss_PLAY_MUSIC = getField(PacketPlayOutBoss, "h");
			PacketPlayOutBoss_CREATE_FOG = getField(PacketPlayOutBoss, "i");

			PacketPlayOutScoreboardTeam_VISIBILITY = getField(PacketPlayOutScoreboardTeam, "e");
			PacketPlayOutScoreboardTeam_COLLISION = getField(PacketPlayOutScoreboardTeam, "f");
			PacketPlayOutScoreboardTeam_PLAYERS = getField(PacketPlayOutScoreboardTeam, "h");
			PacketPlayOutScoreboardTeam_ACTION = getField(PacketPlayOutScoreboardTeam, "i");
			PacketPlayOutScoreboardTeam_SIGNATURE = getField(PacketPlayOutScoreboardTeam, "j");

			PacketPlayOutSpawnEntityLiving_UUID = getField(PacketPlayOutSpawnEntityLiving, "b");
			PacketPlayOutSpawnEntityLiving_ENTITYTYPE = getField(PacketPlayOutSpawnEntityLiving, "c");
			PacketPlayOutSpawnEntityLiving_X = getField(PacketPlayOutSpawnEntityLiving, "d");
			PacketPlayOutSpawnEntityLiving_Y = getField(PacketPlayOutSpawnEntityLiving, "e");
			PacketPlayOutSpawnEntityLiving_Z = getField(PacketPlayOutSpawnEntityLiving, "f");
			PacketPlayOutSpawnEntityLiving_YAW = getField(PacketPlayOutSpawnEntityLiving, "j");
			PacketPlayOutSpawnEntityLiving_PITCH = getField(PacketPlayOutSpawnEntityLiving, "k");
			
			PacketPlayInUseEntity_ACTION = getField(PacketPlayInUseEntity, "action");
		} else {
			//1.8-
			DataWatcherItem_TYPE = getField(DataWatcherItem, "b");
			DataWatcherItem_VALUE = getField(DataWatcherItem, "c");
			
			PacketPlayOutSpawnEntityLiving_ENTITYTYPE = getField(PacketPlayOutSpawnEntityLiving, "b");
			PacketPlayOutSpawnEntityLiving_X = getField(PacketPlayOutSpawnEntityLiving, "c");
			PacketPlayOutSpawnEntityLiving_Y = getField(PacketPlayOutSpawnEntityLiving, "d");
			PacketPlayOutSpawnEntityLiving_Z = getField(PacketPlayOutSpawnEntityLiving, "e");
			PacketPlayOutSpawnEntityLiving_YAW = getField(PacketPlayOutSpawnEntityLiving, "i");
			PacketPlayOutSpawnEntityLiving_PITCH = getField(PacketPlayOutSpawnEntityLiving, "j");
			if (minorVersion == 8) {
				//1.8.x
				PacketPlayOutScoreboardTeam_VISIBILITY = getField(PacketPlayOutScoreboardTeam, "e");
				PacketPlayOutScoreboardTeam_PLAYERS = getField(PacketPlayOutScoreboardTeam, "g");
				PacketPlayOutScoreboardTeam_ACTION = getField(PacketPlayOutScoreboardTeam, "h");
				PacketPlayOutScoreboardTeam_SIGNATURE = getField(PacketPlayOutScoreboardTeam, "i");
			} else {
				//1.7.x
				PacketPlayOutScoreboardTeam_PLAYERS = getField(PacketPlayOutScoreboardTeam, "e");
				PacketPlayOutScoreboardTeam_ACTION = getField(PacketPlayOutScoreboardTeam, "f");
				PacketPlayOutScoreboardTeam_SIGNATURE = getField(PacketPlayOutScoreboardTeam, "g");
			}
		}
		if (minorVersion >= 13) {
			//1.13+
			PacketPlayOutScoreboardTeam_CHATFORMAT = getField(PacketPlayOutScoreboardTeam, "g");
		}

		if (minorVersion <= 14) {
			PacketPlayOutSpawnEntityLiving_DATAWATCHER = getFields(PacketPlayOutSpawnEntityLiving, DataWatcher).get(0);
		}

		if (minorVersion >= 16) {
			//1.16+
			ChatHexColor_name = getField(ChatHexColor, "name");
			ChatHexColor_rgb = getField(ChatHexColor, "rgb");
			ChatHoverable_action = getField(ChatHoverable, "b");
			ChatHoverable_value = getField(ChatHoverable, "c");
			PacketPlayOutChat_SENDER = getField(PacketPlayOutChat, "c");
		} else {
			ChatHoverable_action = getField(ChatHoverable, "a");
			ChatHoverable_value = getField(ChatHoverable, "b");
		}
	}

	/**
	 * Initializes required NMS methods
	 * @throws Exception - if something fails
	 */
	public void initializeMethods() throws Exception {
		ChatComponentText_addSibling = getMethod(ChatComponentText, new String[]{"addSibling", "a", "func_150257_a"}, IChatBaseComponent); //v1.7.R4+, v1.7.R3-
		ChatSerializer_DESERIALIZE = getMethod(ChatSerializer, new String[]{"a", "func_150699_a"}, String.class);
		EnumClickAction_a = getMethod(EnumClickAction, new String[]{"a", "func_150672_a"}, String.class);
		EnumHoverAction_a = getMethod(EnumHoverAction, new String[]{"a", "func_150684_a"}, String.class);
		getHandle = Class.forName("org.bukkit.craftbukkit." + serverPackage + ".entity.CraftPlayer").getMethod("getHandle");
		sendPacket = getMethod(getNMSClass("PlayerConnection"), new String[]{"sendPacket", "func_147359_a"}, getNMSClass("Packet"));
		if (minorVersion >= 8) {
			getProfile = getNMSClass("EntityHuman").getMethod("getProfile");
			for (Method m : PropertyMap.getMethods()) {
				if (m.getName().equals("putAll") && m.getParameterCount() == 1) PropertyMap_putAll = m;
			}
			if (PropertyMap_putAll == null) throw new IllegalStateException("putAll method not found");
		}
		if (minorVersion >= 9) {
			//1.9+
			DataWatcher_REGISTER = DataWatcher.getMethod("register", DataWatcherObject, Object.class);
		} else {
			//1.7.x - 1.8.x
			DataWatcher_REGISTER = getMethod(DataWatcher, new String[]{"a", "func_75682_a"}, int.class, Object.class);
		}
		if (minorVersion >= 16) {
			ChatHexColor_ofInt = ChatHexColor.getMethod("a", int.class);
			ChatHexColor_ofString = ChatHexColor.getMethod("a", String.class);
		}
	}
	
	/**
	 * A helper method that prints all methods of class into console, including their return type, name and parameters
	 * Useful for modded servers which code I can not access
	 * @param clazz - class to show methods of
	 */
	public void showMethods(Class<?> clazz) {
		System.out.println("--- " + clazz.getSimpleName() + " ---");
		for (Method m : clazz.getMethods()) {
			System.out.println(m.getReturnType().getName() + " " + m.getName() + "(" + Arrays.toString(m.getParameterTypes()) + ")");
		}
	}

	/**
	 * Returns class with given potential names in same order
	 * @param names - possible class names
	 * @return class for specified name(s)
	 * @throws ClassNotFoundException if class does not exist
	 */
	public Class<?> getNMSClass(String... names) throws ClassNotFoundException {
		for (String name : names) {
			try {
				return getNMSClass(name);
			} catch (ClassNotFoundException e) {
			}
		}
		throw new ClassNotFoundException("No class found with possible names " + names);
	}
	
	/**
	 * Returns class from given name
	 * @param name - class name
	 * @return class from given name
	 * @throws ClassNotFoundException if class was not found
	 */
	public Class<?> getNMSClass(String name) throws ClassNotFoundException {
		try {
			return Class.forName("net.minecraft.server." + serverPackage + "." + name);
		} catch (ClassNotFoundException e) {
			//modded server?
			return Main.class.getClassLoader().loadClass("net.minecraft.server." + serverPackage + "." + name);
		} catch (NullPointerException e) {
			//nested class in modded server
			throw new ClassNotFoundException(name);
		}
	}
	
	/**
	 * Returns method with specified possible names and parameters. Throws exception if no such method was found
	 * @param clazz - class to get method from
	 * @param names - possible method names
	 * @param parameterTypes - parameter types of the method
	 * @return method with specified name and parameters
	 * @throws NoSuchMethodException if no such method exists
	 */
	public Method getMethod(Class<?> clazz, String[] names, Class<?>... parameterTypes) throws NoSuchMethodException {
		for (String name : names) {
			try {
				return clazz.getMethod(name, parameterTypes);
			} catch (Exception e) {
			}
		}
		throw new NoSuchMethodException("No method found with possible names " + Arrays.toString(names) + " in class " + clazz.getName());
	}

	/**
	 * Returns all fields of class with defined class type
	 * @param clazz - class to check fields of
	 * @param type - field type to check for
	 * @return list of all fields with specified class type
	 */
	private List<Field> getFields(Class<?> clazz, Class<?> type){
		List<Field> list = new ArrayList<Field>();
		if (clazz == null) return list;
		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			if (field.getType() == type) list.add(field);
		}
		return list;
	}
	
	/**
	 * Returns field with specified name and makes it accessible
	 * @param clazz - class to get field from
	 * @param name - field name
	 * @return accessible field with defined name
	 * @throws NoSuchFieldException if field was not found
	 */
	private Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
		for (Field f : clazz.getDeclaredFields()) {
			if (f.getName().equals(name) || (f.getName().split("_").length == 3 && f.getName().split("_")[2].equals(name))) {
				f.setAccessible(true);
				return f;
			}
		}
		if (thermosFieldMappings.containsKey(name)) {
			return getField(clazz, thermosFieldMappings.get(name));
		}
		throw new NoSuchFieldException("Field \"" + name + "\" was not found in class " + clazz.getName());
	}
	
	private Constructor<?> getConstructor(Class<?> clazz, int parameterCount) throws NoSuchMethodException {
		for (Constructor<?> c : clazz.getDeclaredConstructors()) {
			if (c.getParameterCount() == parameterCount) {
				c.setAccessible(true);
				return c;
			}
		}
		throw new NoSuchMethodException("No constructor found in class " + clazz.getName() + " with " + parameterCount + " parameters");
	}
}