package me.neznamy.tab.platforms.bukkit.nms;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;

import io.netty.channel.Channel;
import me.neznamy.tab.platforms.bukkit.Main;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherRegistry;

@SuppressWarnings("rawtypes")
public class NMSStorage {

	//instance of this class
	private static NMSStorage instance;

	//data watcher registry
	private DataWatcherRegistry registry;

	//server package, such as "v1_16_R3"
	private String serverPackage;

	//server minor version such as "16"
	private int minorVersion;
	
	//base
	public Class<?> EnumChatFormat;
	public Class<?> EntityPlayer;
	private Class<?> Entity;
	private Class<?> EntityLiving;
	public Field PING;
	public Field PLAYER_CONNECTION;
	public Field NETWORK_MANAGER;
	public Field CHANNEL;
	public Method getHandle;
	public Method sendPacket;
	public Method getProfile;
	public Enum[] EnumChatFormat_values;
	
	//chat
	public Class<?> ChatComponentText;
	public Class<?> ChatHoverable;
	public Class<?> EnumClickAction;
	private Class<?> IChatBaseComponent;
	public Constructor<?> newChatComponentText;
	public Constructor<?> newChatClickable;
	public Constructor<?> newChatModifier;
	public Constructor<?> newChatHoverable;
	public Field ChatBaseComponent_extra;
	public Field ChatBaseComponent_modifier;
	public Field ChatComponentText_text;
	public Field ChatClickable_action;
	public Field ChatClickable_value;
	public Field ChatModifier_bold;
	public Field ChatModifier_italic;
	public Field ChatModifier_underlined;
	public Field ChatModifier_strikethrough;
	public Field ChatModifier_obfuscated;
	public Field ChatModifier_clickEvent;
	public Field ChatModifier_hoverEvent;
	public Field ChatHexColor_name;
	public Field ChatHexColor_rgb;
	public Field ChatModifier_color;
	public Method ChatComponentText_addSibling;
	public Method EnumHoverAction_a;
	public Method ChatHoverable_getAction;
	public Method ChatHexColor_ofInt;
	public Method ChatHexColor_ofString;
	public Method ChatHoverable_serialize;
	public Method ChatHoverable_getValue;
	public Method EnumHoverAction_fromJson;
	public Method EnumHoverAction_fromLegacyComponent;
	
	//PacketPlayOutChat
	public Class<?> ChatMessageType;
	public Constructor<?> newPacketPlayOutChat;
	public Enum[] ChatMessageType_values;
	
	//DataWatcher
	private Class<?> DataWatcher;
	public Class<?> DataWatcherRegistry;
	public Constructor<?> newDataWatcher;
	public Constructor<?> newDataWatcherObject;
	public Field DataWatcherItem_TYPE;
	public Field DataWatcherItem_VALUE;
	public Field DataWatcherObject_SLOT;
	public Field DataWatcherObject_SERIALIZER;
	public Method DataWatcher_REGISTER;
	
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
	public Constructor<?> newPacketPlayOutPlayerListHeaderFooter;
	public Field PacketPlayOutPlayerListHeaderFooter_HEADER;
	public Field PacketPlayOutPlayerListHeaderFooter_FOOTER;
	
	//other entity packets
	public Class<?> PacketPlayInUseEntity;
	public Class<?> PacketPlayInUseEntity$d;
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
	private Class<?> Scoreboard;
	public Class<?> EnumScoreboardHealthDisplay;
	public Constructor<?> newScoreboardObjective;
	public Constructor<?> newScoreboard;
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
	public Constructor<?> newScoreboardTeam;
	public Constructor<?> newPacketPlayOutScoreboardTeam;
	public Field PacketPlayOutScoreboardTeam_NAME;
	public Field PacketPlayOutScoreboardTeam_PLAYERS;
	public Method ScoreboardTeam_getPlayerNameSet;
	public Method ScoreboardTeam_setNameTagVisibility;
	public Method ScoreboardTeam_setCollisionRule;
	public Method ScoreboardTeam_setPrefix;
	public Method ScoreboardTeam_setSuffix;
	public Method ScoreboardTeam_setColor;
	public Method PacketPlayOutScoreboardTeam_of;
	public Method PacketPlayOutScoreboardTeam_ofBoolean;
	public Method PacketPlayOutScoreboardTeam_ofString;
	public Enum[] EnumNameTagVisibility_values;
	public Enum[] EnumTeamPush_values;
	public Enum[] PacketPlayOutScoreboardTeam_PlayerAction_values;

	/**
	 * Creates new instance, initializes required NMS classes and fields
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws ClassNotFoundException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	public NMSStorage() throws NoSuchFieldException, NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		serverPackage = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		minorVersion = Integer.parseInt(serverPackage.split("_")[1]);
		EnumChatFormat = getNMSClass("net.minecraft.EnumChatFormat", "EnumChatFormat");
		EnumChatFormat_values = getEnumValues(EnumChatFormat);
		EntityPlayer = getNMSClass("net.minecraft.server.level.EntityPlayer", "EntityPlayer");
		Entity = getNMSClass("net.minecraft.world.entity.Entity", "Entity");
		EntityLiving = getNMSClass("net.minecraft.world.entity.EntityLiving", "EntityLiving");
		Class<?> EntityHuman = getNMSClass("net.minecraft.world.entity.player.EntityHuman", "EntityHuman");
		Class<?> Packet = getNMSClass("net.minecraft.network.protocol.Packet", "Packet");
		Class<?> PlayerConnection = getNMSClass("net.minecraft.server.network.PlayerConnection", "PlayerConnection");
		Class<?> NetworkManager = getNMSClass("net.minecraft.network.NetworkManager", "NetworkManager");
		PING = getField(EntityPlayer, "ping", "latency", "field_71138_i", "field_13967", "e");
		PLAYER_CONNECTION = getFields(EntityPlayer, PlayerConnection).get(0);
		getHandle = Class.forName("org.bukkit.craftbukkit." + serverPackage + ".entity.CraftPlayer").getMethod("getHandle");
		sendPacket = getMethods(PlayerConnection, void.class, Packet).get(0);
		if (minorVersion >= 7) {
			NETWORK_MANAGER = getFields(PlayerConnection, NetworkManager).get(0);
		}
		if (minorVersion >= 8) {
			CHANNEL = getFields(NetworkManager, Channel.class).get(0);
			getProfile = getMethods(EntityHuman, GameProfile.class).get(0);
		}
		initializeChatComponents();
		initializeChatPacket();
		initializeDataWatcher();
		initializeEntitySpawnPacket();
		initializeEntityTeleportPacket();
		initializeHeaderFooterPacket();
		initializeOtherEntityPackets();
		initializePlayerInfoPacket();
		initializeScoreboardPackets();
		try {
			initializeTeamPackets();
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
			Bukkit.getConsoleSender().sendMessage("\u00a7c[TAB] Failed to load PacketPlayOutScoreboardTeam");
			//fabric with missing team packet
		}
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

	private void initializeChatComponents() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		if (minorVersion < 7) return;
		Class<?> ChatBaseComponent = getNMSClass("net.minecraft.network.chat.ChatBaseComponent", "ChatBaseComponent");
		Class<?> ChatClickable = getNMSClass("net.minecraft.network.chat.ChatClickable", "ChatClickable");
		ChatComponentText = getNMSClass("net.minecraft.network.chat.ChatComponentText", "ChatComponentText");
		ChatHoverable = getNMSClass("net.minecraft.network.chat.ChatHoverable", "ChatHoverable");
		Class<?> ChatModifier = getNMSClass("net.minecraft.network.chat.ChatModifier", "ChatModifier");
		EnumClickAction = getNMSClass("net.minecraft.network.chat.ChatClickable$EnumClickAction", "ChatClickable$EnumClickAction", "EnumClickAction");
		Class<?> EnumHoverAction = getNMSClass("net.minecraft.network.chat.ChatHoverable$EnumHoverAction", "ChatHoverable$EnumHoverAction", "EnumHoverAction");
		IChatBaseComponent = getNMSClass("net.minecraft.network.chat.IChatBaseComponent", "IChatBaseComponent");
		newChatComponentText = ChatComponentText.getConstructor(String.class);
		newChatClickable = ChatClickable.getConstructor(EnumClickAction, String.class);
		ChatBaseComponent_extra = getFields(ChatBaseComponent, List.class).get(0);
		ChatBaseComponent_modifier = getFields(ChatBaseComponent, ChatModifier).get(0);
		ChatComponentText_text = getFields(ChatComponentText, String.class).get(0);
		ChatClickable_action = getFields(ChatClickable, EnumClickAction).get(0);
		ChatClickable_value = getFields(ChatClickable, String.class).get(0);
		List<Field> booleans = getFields(ChatModifier, Boolean.class);
		ChatModifier_bold = booleans.get(0);
		ChatModifier_italic = booleans.get(1);
		ChatModifier_underlined = booleans.get(2);
		ChatModifier_strikethrough = booleans.get(3);
		ChatModifier_obfuscated = booleans.get(4);
		ChatModifier_clickEvent = getFields(ChatModifier, ChatClickable).get(0);
		ChatModifier_hoverEvent = getFields(ChatModifier, ChatHoverable).get(0);
		ChatComponentText_addSibling = getMethod(ChatComponentText, new String[]{"addSibling", "a", "func_150257_a", "method_10852"}, IChatBaseComponent);
		EnumHoverAction_a = getMethod(EnumHoverAction, new String[]{"a", "func_150684_a", "method_27670"}, String.class);
		ChatHoverable_getAction = getMethods(ChatHoverable, EnumHoverAction).get(0);
		if (minorVersion >= 16) {
			Class<?> ChatHexColor = getNMSClass("net.minecraft.network.chat.ChatHexColor", "ChatHexColor");
			Class<?> MinecraftKey = getNMSClass("net.minecraft.resources.MinecraftKey", "MinecraftKey");
			newChatModifier = setAccessible(ChatModifier.getDeclaredConstructor(ChatHexColor, Boolean.class, Boolean.class, Boolean.class, Boolean.class, Boolean.class, ChatClickable, ChatHoverable, String.class, MinecraftKey));
			newChatHoverable = ChatHoverable.getConstructor(EnumHoverAction, Object.class);
			List<Field> list = getFields(ChatHexColor, String.class);
			ChatHexColor_name = list.get(list.size()-1);
			ChatHexColor_rgb = getFields(ChatHexColor, int.class).get(0);
			ChatModifier_color = getFields(ChatModifier, ChatHexColor).get(0);
			ChatHexColor_ofInt = getMethods(ChatHexColor, ChatHexColor, int.class).get(0);
			ChatHexColor_ofString = getMethods(ChatHexColor, ChatHexColor, String.class).get(0);
			ChatHoverable_serialize = getMethods(ChatHoverable, JsonObject.class).get(0);
			ChatHoverable_getValue = getMethods(ChatHoverable, Object.class, EnumHoverAction).get(0);
			EnumHoverAction_fromJson = getMethods(EnumHoverAction, ChatHoverable, JsonElement.class).get(0);
			EnumHoverAction_fromLegacyComponent = getMethods(EnumHoverAction, ChatHoverable, IChatBaseComponent).get(0);
		} else {
			newChatModifier = ChatModifier.getConstructor();
			newChatHoverable = ChatHoverable.getConstructor(EnumHoverAction, IChatBaseComponent);
			ChatModifier_color = getFields(ChatModifier, EnumChatFormat).get(0);
			ChatHoverable_getValue = getMethods(ChatHoverable, IChatBaseComponent).get(0);
		}
	}
	
	private void initializeChatPacket() throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
		Class<?> PacketPlayOutChat = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutChat", "PacketPlayOutChat", "Packet3Chat");
		if (minorVersion >= 12) {
			ChatMessageType = getNMSClass("net.minecraft.network.chat.ChatMessageType", "ChatMessageType");
			ChatMessageType_values = getEnumValues(ChatMessageType);
		}
		if (minorVersion >= 16) {
			newPacketPlayOutChat = PacketPlayOutChat.getConstructor(IChatBaseComponent, ChatMessageType, UUID.class);
		} else if (minorVersion >= 12) {
			newPacketPlayOutChat = PacketPlayOutChat.getConstructor(IChatBaseComponent, ChatMessageType);
		} else if (minorVersion >= 8) {
			newPacketPlayOutChat = PacketPlayOutChat.getConstructor(IChatBaseComponent, byte.class);
		} else if (minorVersion >= 7){
			newPacketPlayOutChat = PacketPlayOutChat.getConstructor(IChatBaseComponent);
		}
	}
	
	private void initializeDataWatcher() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		DataWatcher = getNMSClass("net.minecraft.network.syncher.DataWatcher", "DataWatcher");
		Class<?> DataWatcherItem = getNMSClass("net.minecraft.network.syncher.DataWatcher$Item", "DataWatcher$Item", "DataWatcher$WatchableObject", "WatchableObject");
		if (minorVersion >= 7) {
			newDataWatcher = DataWatcher.getConstructor(Entity);
		} else {
			newDataWatcher = DataWatcher.getConstructor();
		}
		DataWatcherItem_VALUE = getFields(DataWatcherItem, Object.class).get(0);
		if (minorVersion >= 9) {
			Class<?> DataWatcherObject = getNMSClass("net.minecraft.network.syncher.DataWatcherObject", "DataWatcherObject");
			DataWatcherRegistry = getNMSClass("net.minecraft.network.syncher.DataWatcherRegistry", "DataWatcherRegistry");
			Class<?> DataWatcherSerializer = getNMSClass("net.minecraft.network.syncher.DataWatcherSerializer", "DataWatcherSerializer");
			newDataWatcherObject = DataWatcherObject.getConstructor(int.class, DataWatcherSerializer);
			DataWatcherItem_TYPE = getFields(DataWatcherItem, DataWatcherObject).get(0);
			DataWatcherObject_SLOT = getFields(DataWatcherObject, int.class).get(0);
			DataWatcherObject_SERIALIZER = getFields(DataWatcherObject, DataWatcherSerializer).get(0);
			DataWatcher_REGISTER = getMethod(DataWatcher, new String[]{"register", "method_12784"}, DataWatcherObject, Object.class);
		} else {
			DataWatcherItem_TYPE = getFields(DataWatcherItem, int.class).get(1);
			DataWatcher_REGISTER = getMethod(DataWatcher, new String[]{"a", "func_75682_a"}, int.class, Object.class);
		}
		registry = new DataWatcherRegistry(this);
	}
	
	private void initializeEntitySpawnPacket() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		PacketPlayOutSpawnEntityLiving = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutSpawnEntityLiving", "PacketPlayOutSpawnEntityLiving", "Packet24MobSpawn");
		newPacketPlayOutSpawnEntityLiving = PacketPlayOutSpawnEntityLiving.getConstructor(EntityLiving);
		PacketPlayOutSpawnEntityLiving_ENTITYID = getFields(PacketPlayOutSpawnEntityLiving, int.class).get(0);
		PacketPlayOutSpawnEntityLiving_ENTITYTYPE = getFields(PacketPlayOutSpawnEntityLiving, int.class).get(1);
		PacketPlayOutSpawnEntityLiving_YAW = getFields(PacketPlayOutSpawnEntityLiving, byte.class).get(0);
		PacketPlayOutSpawnEntityLiving_PITCH = getFields(PacketPlayOutSpawnEntityLiving, byte.class).get(0);
		if (minorVersion >= 9) {
			PacketPlayOutSpawnEntityLiving_UUID = getFields(PacketPlayOutSpawnEntityLiving, UUID.class).get(0);
			PacketPlayOutSpawnEntityLiving_X = getFields(PacketPlayOutSpawnEntityLiving, double.class).get(0);
			PacketPlayOutSpawnEntityLiving_Y = getFields(PacketPlayOutSpawnEntityLiving, double.class).get(1);
			PacketPlayOutSpawnEntityLiving_Z = getFields(PacketPlayOutSpawnEntityLiving, double.class).get(2);
		} else {
			PacketPlayOutSpawnEntityLiving_X = getFields(PacketPlayOutSpawnEntityLiving, int.class).get(2);
			PacketPlayOutSpawnEntityLiving_Y = getFields(PacketPlayOutSpawnEntityLiving, int.class).get(3);
			PacketPlayOutSpawnEntityLiving_Z = getFields(PacketPlayOutSpawnEntityLiving, int.class).get(4);
		}
		if (minorVersion <= 14) {
			PacketPlayOutSpawnEntityLiving_DATAWATCHER = getFields(PacketPlayOutSpawnEntityLiving, DataWatcher).get(0);
		}
	}
	
	private void initializeEntityTeleportPacket() throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		PacketPlayOutEntityTeleport = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport", "PacketPlayOutEntityTeleport", "Packet34EntityTeleport");
		newPacketPlayOutEntityTeleport = PacketPlayOutEntityTeleport.getConstructor(Entity);
		PacketPlayOutEntityTeleport_ENTITYID = getFields(PacketPlayOutEntityTeleport, int.class).get(0);
		PacketPlayOutEntityTeleport_YAW = getFields(PacketPlayOutEntityTeleport, byte.class).get(0);
		PacketPlayOutEntityTeleport_PITCH = getFields(PacketPlayOutEntityTeleport, byte.class).get(1);
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
	
	private void initializeHeaderFooterPacket() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		if (minorVersion < 8) return;
		Class<?> PacketPlayOutPlayerListHeaderFooter = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutPlayerListHeaderFooter", "PacketPlayOutPlayerListHeaderFooter");
		PacketPlayOutPlayerListHeaderFooter_HEADER = getFields(PacketPlayOutPlayerListHeaderFooter, IChatBaseComponent).get(0);
		PacketPlayOutPlayerListHeaderFooter_FOOTER = getFields(PacketPlayOutPlayerListHeaderFooter, IChatBaseComponent).get(1);
		try {
			newPacketPlayOutPlayerListHeaderFooter = PacketPlayOutPlayerListHeaderFooter.getConstructor(IChatBaseComponent, IChatBaseComponent);
		} catch (NoSuchMethodException e) {
			newPacketPlayOutPlayerListHeaderFooter = PacketPlayOutPlayerListHeaderFooter.getConstructor();
		}
	}
	
	private void initializeOtherEntityPackets() throws ClassNotFoundException, NoSuchMethodException {
		PacketPlayInUseEntity = getNMSClass("net.minecraft.network.protocol.game.PacketPlayInUseEntity", "PacketPlayInUseEntity", "Packet7UseEntity");
		PacketPlayOutEntity = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutEntity", "PacketPlayOutEntity", "Packet30Entity");
		PacketPlayOutEntityDestroy = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy", "PacketPlayOutEntityDestroy", "Packet29DestroyEntity");
		PacketPlayOutEntityLook = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutEntity$PacketPlayOutEntityLook", "PacketPlayOutEntity$PacketPlayOutEntityLook", "PacketPlayOutEntityLook", "Packet32EntityLook");
		PacketPlayOutEntityMetadata = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata", "PacketPlayOutEntityMetadata", "Packet40EntityMetadata");
		PacketPlayOutNamedEntitySpawn = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn", "PacketPlayOutNamedEntitySpawn", "Packet20NamedEntitySpawn");
		newPacketPlayOutEntityMetadata = PacketPlayOutEntityMetadata.getConstructor(int.class, DataWatcher, boolean.class);
		PacketPlayOutEntity_ENTITYID = getFields(PacketPlayOutEntity, int.class).get(0);
		PacketPlayOutEntityDestroy_ENTITIES = setAccessible(PacketPlayOutEntityDestroy.getDeclaredFields()[0]);
		PacketPlayOutEntityMetadata_LIST = getFields(PacketPlayOutEntityMetadata, List.class).get(0);
		PacketPlayOutNamedEntitySpawn_ENTITYID = getFields(PacketPlayOutNamedEntitySpawn, int.class).get(0);
		if (minorVersion >= 7) {
			Class<?> EnumEntityUseAction = getNMSClass("net.minecraft.network.protocol.game.PacketPlayInUseEntity$EnumEntityUseAction", "PacketPlayInUseEntity$EnumEntityUseAction", "EnumEntityUseAction", "net.minecraft.class_2824$class_5906");
			PacketPlayInUseEntity_ENTITY = getFields(PacketPlayInUseEntity, int.class).get(0);
			PacketPlayInUseEntity_ACTION = getFields(PacketPlayInUseEntity, EnumEntityUseAction).get(0);
		}
		if (minorVersion >= 17) {
			PacketPlayInUseEntity$d = Class.forName("net.minecraft.network.protocol.game.PacketPlayInUseEntity$d");
		}
		try {
			newPacketPlayOutEntityDestroy = PacketPlayOutEntityDestroy.getConstructor(int[].class);
		} catch (NoSuchMethodException e) {
			//1.17.0
			newPacketPlayOutEntityDestroy = PacketPlayOutEntityDestroy.getConstructor(int.class);
		}
	}
	
	private void initializePlayerInfoPacket() throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
		if (minorVersion < 8) return; //TODO
		PacketPlayOutPlayerInfo = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo", "PacketPlayOutPlayerInfo");
		Class<?> EnumPlayerInfoAction = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo$EnumPlayerInfoAction", "PacketPlayOutPlayerInfo$EnumPlayerInfoAction", "EnumPlayerInfoAction", "net.minecraft.class_2703$class_5893");
		Class<?> PlayerInfoData = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo$PlayerInfoData", "PacketPlayOutPlayerInfo$PlayerInfoData", "PlayerInfoData");
		Class<?> EnumGamemode = getNMSClass("net.minecraft.world.level.EnumGamemode", "EnumGamemode", "WorldSettings$EnumGamemode");
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

	private void initializeScoreboardPackets() throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
		PacketPlayOutScoreboardDisplayObjective = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutScoreboardDisplayObjective", "PacketPlayOutScoreboardDisplayObjective", "Packet208SetScoreboardDisplayObjective");
		PacketPlayOutScoreboardObjective = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutScoreboardObjective", "PacketPlayOutScoreboardObjective", "Packet206SetScoreboardObjective");
		Class<?> PacketPlayOutScoreboardScore = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutScoreboardScore", "PacketPlayOutScoreboardScore", "Packet207SetScoreboardScore");
		Scoreboard = getNMSClass("net.minecraft.world.scores.Scoreboard", "Scoreboard");
		Class<?> ScoreboardObjective = getNMSClass("net.minecraft.world.scores.ScoreboardObjective", "ScoreboardObjective");
		Class<?> ScoreboardScore = getNMSClass("net.minecraft.world.scores.ScoreboardScore", "ScoreboardScore");
		Class<?> IScoreboardCriteria = getNMSClass("net.minecraft.world.scores.criteria.IScoreboardCriteria", "IScoreboardCriteria");
		newScoreboardObjective = ScoreboardObjective.getConstructors()[0];
		newScoreboard = Scoreboard.getConstructor();
		newScoreboardScore = ScoreboardScore.getConstructor(Scoreboard, ScoreboardObjective, String.class);
		newPacketPlayOutScoreboardDisplayObjective = PacketPlayOutScoreboardDisplayObjective.getConstructor(int.class, ScoreboardObjective);
		PacketPlayOutScoreboardDisplayObjective_POSITION = getFields(PacketPlayOutScoreboardDisplayObjective, int.class).get(0);
		PacketPlayOutScoreboardDisplayObjective_OBJECTIVENAME = getFields(PacketPlayOutScoreboardDisplayObjective, String.class).get(0);
		PacketPlayOutScoreboardObjective_OBJECTIVENAME = getFields(PacketPlayOutScoreboardObjective, String.class).get(0);
		List<Field> list = getFields(PacketPlayOutScoreboardObjective, int.class);
		PacketPlayOutScoreboardObjective_METHOD = list.get(list.size()-1);
		IScoreboardCriteria_self = getFields(IScoreboardCriteria, IScoreboardCriteria).get(0);
		ScoreboardScore_setScore = getMethod(ScoreboardScore, new String[]{"setScore", "func_96647_c", "method_1128"}, int.class);
		Class<?> EnumScoreboardAction = null;
		if (minorVersion >= 8) {
			EnumScoreboardHealthDisplay = getNMSClass("net.minecraft.world.scores.criteria.IScoreboardCriteria$EnumScoreboardHealthDisplay", "IScoreboardCriteria$EnumScoreboardHealthDisplay", "EnumScoreboardHealthDisplay");
			EnumScoreboardHealthDisplay_values = getEnumValues(EnumScoreboardHealthDisplay);
			EnumScoreboardAction = getNMSClass("net.minecraft.server.ScoreboardServer$Action", "ScoreboardServer$Action", "PacketPlayOutScoreboardScore$EnumScoreboardAction", "EnumScoreboardAction");
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
	
	private void initializeTeamPackets() throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
		PacketPlayOutScoreboardTeam = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam", "PacketPlayOutScoreboardTeam", "Packet209SetScoreboardTeam");
		Class<?> ScoreboardTeam = getNMSClass("net.minecraft.world.scores.ScoreboardTeam", "ScoreboardTeam");
		newScoreboardTeam = ScoreboardTeam.getConstructor(Scoreboard, String.class);
		PacketPlayOutScoreboardTeam_NAME = getFields(PacketPlayOutScoreboardTeam, String.class).get(0);
		PacketPlayOutScoreboardTeam_PLAYERS = getFields(PacketPlayOutScoreboardTeam, Collection.class).get(0);
		ScoreboardTeam_getPlayerNameSet = getMethods(ScoreboardTeam, Collection.class).get(0);
		if (minorVersion >= 8) {
			Class<?> EnumNameTagVisibility = getNMSClass("net.minecraft.world.scores.ScoreboardTeamBase$EnumNameTagVisibility", "ScoreboardTeamBase$EnumNameTagVisibility", "EnumNameTagVisibility");
			EnumNameTagVisibility_values = getEnumValues(EnumNameTagVisibility);
			ScoreboardTeam_setNameTagVisibility = getMethod(ScoreboardTeam, new String[]{"setNameTagVisibility", "a", "method_1149"}, EnumNameTagVisibility);
		}
		if (minorVersion >= 9) {
			Class<?> EnumTeamPush = getNMSClass("net.minecraft.world.scores.ScoreboardTeamBase$EnumTeamPush", "ScoreboardTeamBase$EnumTeamPush");
			EnumTeamPush_values = getEnumValues(EnumTeamPush);
			ScoreboardTeam_setCollisionRule = getMethods(ScoreboardTeam, void.class, EnumTeamPush).get(0);
		}
		if (minorVersion >= 13) {
			ScoreboardTeam_setPrefix = getMethod(ScoreboardTeam, new String[]{"setPrefix", "method_1138"}, IChatBaseComponent);
			ScoreboardTeam_setSuffix = getMethod(ScoreboardTeam, new String[]{"setSuffix", "method_1139"}, IChatBaseComponent);
			ScoreboardTeam_setColor = getMethods(ScoreboardTeam, void.class, EnumChatFormat).get(0);
		} else {
			ScoreboardTeam_setPrefix = getMethod(ScoreboardTeam, new String[]{"setPrefix", "func_96666_b"}, String.class);
			ScoreboardTeam_setSuffix = getMethod(ScoreboardTeam, new String[]{"setSuffix", "func_96662_c"}, String.class);
		}
		try {
			Class<?> PacketPlayOutScoreboardTeam_PlayerAction = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam$a");
			PacketPlayOutScoreboardTeam_of = getMethods(PacketPlayOutScoreboardTeam, PacketPlayOutScoreboardTeam, ScoreboardTeam).get(0);
			PacketPlayOutScoreboardTeam_ofBoolean = getMethods(PacketPlayOutScoreboardTeam, PacketPlayOutScoreboardTeam, ScoreboardTeam, boolean.class).get(0);
			PacketPlayOutScoreboardTeam_ofString = getMethods(PacketPlayOutScoreboardTeam, PacketPlayOutScoreboardTeam, ScoreboardTeam, String.class, PacketPlayOutScoreboardTeam_PlayerAction).get(0);
			PacketPlayOutScoreboardTeam_PlayerAction_values = getEnumValues(PacketPlayOutScoreboardTeam_PlayerAction);
		} catch (ClassNotFoundException e) {
			newPacketPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.getConstructor(ScoreboardTeam, int.class);
		}
	}

	/**
	 * Returns class with given potential names in same order
	 * @param names - possible class names
	 * @return class for specified name(s)
	 * @throws ClassNotFoundException if class does not exist
	 */
	private Class<?> getNMSClass(String fullPath_1_17, String... names) throws ClassNotFoundException {
		if (minorVersion >= 17) {
			return Class.forName(fullPath_1_17);
		}
		for (String name : names) {
			try {
				return getLegacyClass(name);
			} catch (ClassNotFoundException | NullPointerException e) {
				//not the first class name in array
			}
		}
		throw new ClassNotFoundException("No class found with possible names " + Arrays.toString(names));
	}

	/**
	 * Returns class from given name
	 * @param name - class name
	 * @return class from given name
	 * @throws ClassNotFoundException if class was not found
	 */
	private Class<?> getLegacyClass(String name) throws ClassNotFoundException {
		try {
			return Class.forName("net.minecraft.server." + serverPackage + "." + name);
		} catch (ClassNotFoundException e) {
			try {
				//modded server?
				Class<?> clazz = Main.class.getClassLoader().loadClass("net.minecraft.server." + serverPackage + "." + name);
				if (clazz != null) return clazz;
				throw new ClassNotFoundException(name);
			} catch (ClassNotFoundException e1) {
				//fabric
				return Class.forName(name);
			}
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
	private Method getMethod(Class<?> clazz, String[] names, Class<?>... parameterTypes) throws NoSuchMethodException {
		for (String name : names) {
			try {
				return getMethod(clazz, name, parameterTypes);
			} catch (Exception e) {
				//not the first method in array
			}
		}
		throw new NoSuchMethodException("No method found with possible names " + Arrays.toString(names) + " with parameters " + Arrays.toString(parameterTypes) + " in class " + clazz.getName());
	}
	
	private Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
		main:
		for (Method m : clazz.getMethods()) {
			if (!m.getName().equals(name) || m.getParameterCount() != parameterTypes.length) continue;
			Class<?>[] types = m.getParameterTypes();
			for (int i=0; i<types.length; i++) {
				if (types[i] != parameterTypes[i]) continue main;
			}
			return m;
		}
		throw new NoSuchMethodException("No method found with name " + name + " in class " + clazz.getName() + " with parameters " + Arrays.toString(parameterTypes));
	}
	
	private List<Method> getMethods(Class<?> clazz, Class<?> returnType, Class<?>... parameterTypes){
		List<Method> list = new ArrayList<>();
		main:
		for (Method m : clazz.getDeclaredMethods()) {
			if (m.getReturnType() != returnType || m.getParameterCount() != parameterTypes.length || !Modifier.isPublic(m.getModifiers())) continue;
			Class<?>[] types = m.getParameterTypes();
			for (int i=0; i<types.length; i++) {
				if (types[i] != parameterTypes[i]) continue main;
			}
			list.add(m);
		}
		return list;
	}

	/**
	 * Returns all fields of class with defined class type
	 * @param clazz - class to check fields of
	 * @param type - field type to check for
	 * @return list of all fields with specified class type
	 */
	private List<Field> getFields(Class<?> clazz, Class<?> type){
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
	 * Returns field with specified name and makes it accessible
	 * @param clazz - class to get field from
	 * @param name - field name
	 * @return accessible field with defined name
	 * @throws NoSuchFieldException if field was not found
	 */
	private Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
		for (Field f : clazz.getDeclaredFields()) {
			if (f.getName().equals(name) || (f.getName().split("_").length == 3 && f.getName().split("_")[2].equals(name))) {
				return setAccessible(f);
			}
		}
		throw new NoSuchFieldException("Field \"" + name + "\" was not found in class " + clazz.getName());
	}

	private Field getField(Class<?> clazz, String... potentialNames) throws NoSuchFieldException {
		for (String name : potentialNames) {
			try {
				return getField(clazz, name);
			} catch (NoSuchFieldException e) {
				//not the first field name from array
			}
		}
		throw new NoSuchFieldException("No field found in class " + clazz.getName() + " with potential names " + Arrays.toString(potentialNames));
	}
	
	private Enum[] getEnumValues(Class<?> clazz) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return (Enum[]) clazz.getMethod("values").invoke(null);
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
}