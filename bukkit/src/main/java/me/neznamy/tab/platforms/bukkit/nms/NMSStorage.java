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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	private DataWatcherRegistry dataWatcherRegistry;

	//server package, such as "v1_16_R3"
	private String serverPackage;

	//server minor version such as "16"
	private int minorVersion;

	private Map<String, Class<?>> classes = new HashMap<>();
	private Map<String, Constructor<?>> constructors = new HashMap<>();
	private Map<String, Field> fields = new HashMap<>();
	private Map<String, Method> methods = new HashMap<>();
	private Map<String, Enum<?>[]> enums = new HashMap<>();

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
		classes.put("EnumChatFormat", getNMSClass("net.minecraft.EnumChatFormat", "EnumChatFormat"));
		classes.put("EntityPlayer", getNMSClass("net.minecraft.server.level.EntityPlayer", "EntityPlayer"));
		classes.put("Entity", getNMSClass("net.minecraft.world.entity.Entity", "Entity"));
		classes.put("EntityLiving", getNMSClass("net.minecraft.world.entity.EntityLiving", "EntityLiving"));
		classes.put("EntityHuman", getNMSClass("net.minecraft.world.entity.player.EntityHuman", "EntityHuman"));
		classes.put("Packet", getNMSClass("net.minecraft.network.protocol.Packet", "Packet"));
		classes.put("PlayerConnection", getNMSClass("net.minecraft.server.network.PlayerConnection", "PlayerConnection"));
		classes.put("NetworkManager", getNMSClass("net.minecraft.network.NetworkManager", "NetworkManager"));
		fields.put("PING", getField(getClass("EntityPlayer"), "ping", "latency", "field_71138_i", "field_13967", "e"));
		fields.put("PLAYER_CONNECTION", getFields(getClass("EntityPlayer"), getClass("PlayerConnection")).get(0));
		methods.put("getHandle", Class.forName("org.bukkit.craftbukkit." + serverPackage + ".entity.CraftPlayer").getMethod("getHandle"));
		methods.put("sendPacket", getMethods(getClass("PlayerConnection"), void.class, getClass("Packet")).get(0));
		if (minorVersion >= 7) {
			fields.put("NETWORK_MANAGER", getFields(getClass("PlayerConnection"), getClass("NetworkManager")).get(0));
		}
		if (minorVersion >= 8) {
			fields.put("CHANNEL", getFields(getClass("NetworkManager"), Channel.class).get(0));
			methods.put("getProfile", getMethods(getClass("EntityHuman"), GameProfile.class).get(0));
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
			//fabric with missing team packet
		}
		initializeEnums();
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

	private void initializeEnums() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
		for (String name : Arrays.asList("ChatMessageType", "EnumPlayerInfoAction", "EnumGamemode", "EnumScoreboardHealthDisplay", "EnumScoreboardAction", 
				"PacketPlayOutScoreboardTeam_a", "EnumChatFormat", "EnumNameTagVisibility", "EnumTeamPush")) {
			if (classes.containsKey(name)) enums.put(name, (Enum[]) getClass(name).getMethod("values").invoke(null));
		}
	}
	
	private void initializeChatComponents() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		if (minorVersion >= 7) {
			classes.put("ChatBaseComponent", getNMSClass("net.minecraft.network.chat.ChatBaseComponent", "ChatBaseComponent"));
			classes.put("ChatClickable", getNMSClass("net.minecraft.network.chat.ChatClickable", "ChatClickable"));
			classes.put("ChatComponentText", getNMSClass("net.minecraft.network.chat.ChatComponentText", "ChatComponentText"));
			classes.put("ChatHoverable", getNMSClass("net.minecraft.network.chat.ChatHoverable", "ChatHoverable"));
			classes.put("ChatModifier", getNMSClass("net.minecraft.network.chat.ChatModifier", "ChatModifier"));
			classes.put("ChatSerializer", getNMSClass("net.minecraft.network.chat.IChatBaseComponent$ChatSerializer", "IChatBaseComponent$ChatSerializer", "ChatSerializer"));
			classes.put("EnumClickAction", getNMSClass("net.minecraft.network.chat.ChatClickable$EnumClickAction", "ChatClickable$EnumClickAction", "EnumClickAction"));
			classes.put("EnumHoverAction", getNMSClass("net.minecraft.network.chat.ChatHoverable$EnumHoverAction", "ChatHoverable$EnumHoverAction", "EnumHoverAction"));
			classes.put("IChatBaseComponent", getNMSClass("net.minecraft.network.chat.IChatBaseComponent", "IChatBaseComponent"));
			constructors.put("ChatComponentText", getClass("ChatComponentText").getConstructor(String.class));
			constructors.put("ChatClickable", getClass("ChatClickable").getConstructor(getClass("EnumClickAction"), String.class));
			fields.put("ChatBaseComponent_extra", getFields(getClass("ChatBaseComponent"), List.class).get(0));
			fields.put("ChatBaseComponent_modifier", getFields(getClass("ChatBaseComponent"), getClass("ChatModifier")).get(0));
			fields.put("ChatComponentText_text", getFields(getClass("ChatComponentText"), String.class).get(0));
			fields.put("ChatClickable_action", getFields(getClass("ChatClickable"), getClass("EnumClickAction")).get(0));
			fields.put("ChatClickable_value", getFields(getClass("ChatClickable"), String.class).get(0));
			fields.put("ChatHoverable_action", getFields(getClass("ChatHoverable"), getClass("EnumHoverAction")).get(0));
			List<Field> booleans = getFields(getClass("ChatModifier"), Boolean.class);
			fields.put("ChatModifier_bold", booleans.get(0));
			fields.put("ChatModifier_italic", booleans.get(1));
			fields.put("ChatModifier_underlined", booleans.get(2));
			fields.put("ChatModifier_strikethrough", booleans.get(3));
			fields.put("ChatModifier_obfuscated", booleans.get(4));
			fields.put("ChatModifier_clickEvent", getFields(getClass("ChatModifier"), getClass("ChatClickable")).get(0));
			fields.put("ChatModifier_hoverEvent", getFields(getClass("ChatModifier"), getClass("ChatHoverable")).get(0));
			methods.put("ChatComponentText_addSibling", getMethod(getClass("ChatComponentText"), new String[]{"addSibling", "a", "func_150257_a", "method_10852"}, getClass("IChatBaseComponent")));
			methods.put("EnumHoverAction_a", getMethod(getClass("EnumHoverAction"), new String[]{"a", "func_150684_a", "method_27670"}, String.class));
			methods.put("ChatHoverable_getAction", getMethods(getClass("ChatHoverable"), getClass("EnumHoverAction")).get(0));
		}
		if (minorVersion >= 16) {
			classes.put("ChatHexColor", getNMSClass("net.minecraft.network.chat.ChatHexColor", "ChatHexColor"));
			constructors.put("ChatModifier", getConstructor(getClass("ChatModifier"), 10));
			constructors.put("ChatHoverable", getClass("ChatHoverable").getConstructor(getClass("EnumHoverAction"), Object.class));
			List<Field> list = getFields(getClass("ChatHexColor"), String.class);
			fields.put("ChatHexColor_name", list.get(list.size()-1));
			fields.put("ChatHexColor_rgb", getFields(getClass("ChatHexColor"), int.class).get(0));
			fields.put("ChatHoverable_value", getFields(getClass("ChatHoverable"), Object.class).get(0));
			fields.put("ChatModifier_color", getFields(getClass("ChatModifier"), getClass("ChatHexColor")).get(0));
			methods.put("ChatHexColor_ofInt", getMethods(getClass("ChatHexColor"), getClass("ChatHexColor"), int.class).get(0));
			methods.put("ChatHexColor_ofString", getMethods(getClass("ChatHexColor"), getClass("ChatHexColor"), String.class).get(0));
			methods.put("ChatHoverable_serialize", getMethods(getClass("ChatHoverable"), JsonObject.class).get(0));
			methods.put("ChatHoverable_getValue", getMethods(getClass("ChatHoverable"), Object.class, getClass("EnumHoverAction")).get(0));
			methods.put("EnumHoverAction_fromJson", getMethods(getClass("EnumHoverAction"), getClass("ChatHoverable"), JsonElement.class).get(0));
			methods.put("EnumHoverAction_fromLegacyComponent", getMethods(getClass("EnumHoverAction"), getClass("ChatHoverable"), getClass("IChatBaseComponent")).get(0));
		} else if (minorVersion >= 7) {
			constructors.put("ChatModifier", getClass("ChatModifier").getConstructor());
			constructors.put("ChatHoverable", getClass("ChatHoverable").getConstructor(getClass("EnumHoverAction"), getClass("IChatBaseComponent")));
			fields.put("ChatHoverable_value", getFields(getClass("ChatHoverable"), getClass("IChatBaseComponent")).get(0));
			fields.put("ChatModifier_color", getFields(getClass("ChatModifier"), getClass("EnumChatFormat")).get(0));
			methods.put("ChatHoverable_getValue", getMethods(getClass("ChatHoverable"), getClass("IChatBaseComponent")).get(0));
		}
	}
	
	private void initializeChatPacket() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		classes.put("PacketPlayOutChat", getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutChat", "PacketPlayOutChat", "Packet3Chat"));
		if (minorVersion >= 12) {
			classes.put("ChatMessageType", getNMSClass("net.minecraft.network.chat.ChatMessageType", "ChatMessageType"));
		}
		if (minorVersion >= 16) {
			constructors.put("PacketPlayOutChat", getClass("PacketPlayOutChat").getConstructor(getClass("IChatBaseComponent"), getClass("ChatMessageType"), UUID.class));
		} else if (minorVersion >= 12) {
			constructors.put("PacketPlayOutChat", getClass("PacketPlayOutChat").getConstructor(getClass("IChatBaseComponent"), getClass("ChatMessageType")));
		} else if (minorVersion >= 8) {
			constructors.put("PacketPlayOutChat", getClass("PacketPlayOutChat").getConstructor(getClass("IChatBaseComponent"), byte.class));
		} else if (minorVersion >= 7){
			constructors.put("PacketPlayOutChat", getClass("PacketPlayOutChat").getConstructor(getClass("IChatBaseComponent")));
		}
	}
	
	private void initializeDataWatcher() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		classes.put("DataWatcher", getNMSClass("net.minecraft.network.syncher.DataWatcher", "DataWatcher"));
		classes.put("DataWatcherItem", getNMSClass("net.minecraft.network.syncher.DataWatcher$Item", "DataWatcher$Item", "DataWatcher$WatchableObject", "WatchableObject"));
		if (minorVersion >= 7) {
			constructors.put("DataWatcher", getClass("DataWatcher").getConstructor(getClass("Entity")));
		} else {
			constructors.put("DataWatcher", getClass("DataWatcher").getConstructor());
		}
		if (minorVersion >= 9) {
			classes.put("DataWatcherObject", getNMSClass("net.minecraft.network.syncher.DataWatcherObject", "DataWatcherObject"));
			classes.put("DataWatcherRegistry", getNMSClass("net.minecraft.network.syncher.DataWatcherRegistry", "DataWatcherRegistry"));
			classes.put("DataWatcherSerializer", getNMSClass("net.minecraft.network.syncher.DataWatcherSerializer", "DataWatcherSerializer"));
			constructors.put("DataWatcherObject", getClass("DataWatcherObject").getConstructor(int.class, getClass("DataWatcherSerializer")));
			fields.put("DataWatcherItem_TYPE", getFields(getClass("DataWatcherItem"), getClass("DataWatcherObject")).get(0));
			fields.put("DataWatcherItem_VALUE", getFields(getClass("DataWatcherItem"), Object.class).get(0));
			fields.put("DataWatcherObject_SLOT", getFields(getClass("DataWatcherObject"), int.class).get(0));
			fields.put("DataWatcherObject_SERIALIZER", getFields(getClass("DataWatcherObject"), getClass("DataWatcherSerializer")).get(0));
			methods.put("DataWatcher_REGISTER", getMethod(getClass("DataWatcher"), new String[]{"register", "method_12784"}, getClass("DataWatcherObject"), Object.class));
		} else {
			fields.put("DataWatcherItem_TYPE", getFields(getClass("DataWatcherItem"), int.class).get(1));
			fields.put("DataWatcherItem_VALUE", getFields(getClass("DataWatcherItem"), Object.class).get(0));
			methods.put("DataWatcher_REGISTER", getMethod(getClass("DataWatcher"), new String[]{"a", "func_75682_a"}, int.class, Object.class));
		}
		dataWatcherRegistry = new DataWatcherRegistry(this);
	}
	
	private void initializeEntitySpawnPacket() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		classes.put("PacketPlayOutSpawnEntityLiving", getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutSpawnEntityLiving", "PacketPlayOutSpawnEntityLiving", "Packet24MobSpawn"));
		constructors.put("PacketPlayOutSpawnEntityLiving", getClass("PacketPlayOutSpawnEntityLiving").getConstructor(getClass("EntityLiving")));
		fields.put("PacketPlayOutSpawnEntityLiving_ENTITYID", getFields(getClass("PacketPlayOutSpawnEntityLiving"), int.class).get(0));
		fields.put("PacketPlayOutSpawnEntityLiving_ENTITYTYPE", getFields(getClass("PacketPlayOutSpawnEntityLiving"), int.class).get(1));
		fields.put("PacketPlayOutSpawnEntityLiving_YAW", getFields(getClass("PacketPlayOutSpawnEntityLiving"), byte.class).get(0));
		fields.put("PacketPlayOutSpawnEntityLiving_PITCH", getFields(getClass("PacketPlayOutSpawnEntityLiving"), byte.class).get(0));
		if (minorVersion >= 9) {
			fields.put("PacketPlayOutSpawnEntityLiving_UUID", getFields(getClass("PacketPlayOutSpawnEntityLiving"), UUID.class).get(0));
			fields.put("PacketPlayOutSpawnEntityLiving_X", getFields(getClass("PacketPlayOutSpawnEntityLiving"), double.class).get(0));
			fields.put("PacketPlayOutSpawnEntityLiving_Y", getFields(getClass("PacketPlayOutSpawnEntityLiving"), double.class).get(1));
			fields.put("PacketPlayOutSpawnEntityLiving_Z", getFields(getClass("PacketPlayOutSpawnEntityLiving"), double.class).get(2));
		} else {
			fields.put("PacketPlayOutSpawnEntityLiving_X", getFields(getClass("PacketPlayOutSpawnEntityLiving"), int.class).get(2));
			fields.put("PacketPlayOutSpawnEntityLiving_Y", getFields(getClass("PacketPlayOutSpawnEntityLiving"), int.class).get(3));
			fields.put("PacketPlayOutSpawnEntityLiving_Z", getFields(getClass("PacketPlayOutSpawnEntityLiving"), int.class).get(4));
		}
		if (minorVersion <= 14) {
			fields.put("PacketPlayOutSpawnEntityLiving_DATAWATCHER", getFields(getClass("PacketPlayOutSpawnEntityLiving"), getClass("DataWatcher")).get(0));
		}
	}
	
	private void initializeEntityTeleportPacket() throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		classes.put("PacketPlayOutEntityTeleport", getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport", "PacketPlayOutEntityTeleport", "Packet34EntityTeleport"));
		constructors.put("PacketPlayOutEntityTeleport", getClass("PacketPlayOutEntityTeleport").getConstructor(getClass("Entity")));
		fields.put("PacketPlayOutEntityTeleport_ENTITYID", getFields(getClass("PacketPlayOutEntityTeleport"), int.class).get(0));
		fields.put("PacketPlayOutEntityTeleport_YAW", getFields(getClass("PacketPlayOutEntityTeleport"), byte.class).get(0));
		fields.put("PacketPlayOutEntityTeleport_PITCH", getFields(getClass("PacketPlayOutEntityTeleport"), byte.class).get(1));
		if (minorVersion >= 9) {
			fields.put("PacketPlayOutEntityTeleport_X", getFields(getClass("PacketPlayOutEntityTeleport"), double.class).get(0));
			fields.put("PacketPlayOutEntityTeleport_Y", getFields(getClass("PacketPlayOutEntityTeleport"), double.class).get(1));
			fields.put("PacketPlayOutEntityTeleport_Z", getFields(getClass("PacketPlayOutEntityTeleport"), double.class).get(2));
		} else {
			fields.put("PacketPlayOutEntityTeleport_X", getFields(getClass("PacketPlayOutEntityTeleport"), int.class).get(1));
			fields.put("PacketPlayOutEntityTeleport_Y", getFields(getClass("PacketPlayOutEntityTeleport"), int.class).get(2));
			fields.put("PacketPlayOutEntityTeleport_Z", getFields(getClass("PacketPlayOutEntityTeleport"), int.class).get(3));
		}
	}
	
	private void initializeHeaderFooterPacket() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		if (minorVersion >= 8) {
			classes.put("PacketPlayOutPlayerListHeaderFooter", getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutPlayerListHeaderFooter", "PacketPlayOutPlayerListHeaderFooter"));
			fields.put("PacketPlayOutPlayerListHeaderFooter_HEADER", getFields(getClass("PacketPlayOutPlayerListHeaderFooter"), getClass("IChatBaseComponent")).get(0));
			fields.put("PacketPlayOutPlayerListHeaderFooter_FOOTER", getFields(getClass("PacketPlayOutPlayerListHeaderFooter"), getClass("IChatBaseComponent")).get(1));
			try {
				constructors.put("PacketPlayOutPlayerListHeaderFooter", getClass("PacketPlayOutPlayerListHeaderFooter").getConstructor(getClass("IChatBaseComponent"), getClass("IChatBaseComponent")));
			} catch (NoSuchMethodException e) {
				constructors.put("PacketPlayOutPlayerListHeaderFooter", getClass("PacketPlayOutPlayerListHeaderFooter").getConstructor());
			}
		}
	}
	
	private void initializeOtherEntityPackets() throws ClassNotFoundException, NoSuchMethodException {
		classes.put("PacketPlayInUseEntity", getNMSClass("net.minecraft.network.protocol.game.PacketPlayInUseEntity", "PacketPlayInUseEntity", "Packet7UseEntity"));
		classes.put("PacketPlayOutEntity", getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutEntity", "PacketPlayOutEntity", "Packet30Entity"));
		classes.put("PacketPlayOutEntityDestroy", getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy", "PacketPlayOutEntityDestroy", "Packet29DestroyEntity"));
		classes.put("PacketPlayOutEntityLook", getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutEntity$PacketPlayOutEntityLook", "PacketPlayOutEntity$PacketPlayOutEntityLook", "PacketPlayOutEntityLook", "Packet32EntityLook"));
		classes.put("PacketPlayOutEntityMetadata", getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata", "PacketPlayOutEntityMetadata", "Packet40EntityMetadata"));
		classes.put("PacketPlayOutNamedEntitySpawn", getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn", "PacketPlayOutNamedEntitySpawn", "Packet20NamedEntitySpawn"));
		constructors.put("PacketPlayOutEntityMetadata", getClass("PacketPlayOutEntityMetadata").getConstructor(int.class, getClass("DataWatcher"), boolean.class));
		fields.put("PacketPlayOutEntity_ENTITYID", getFields(getClass("PacketPlayOutEntity"), int.class).get(0));
		fields.put("PacketPlayOutEntityDestroy_ENTITIES", getClass("PacketPlayOutEntityDestroy").getDeclaredFields()[0]);
		setAccessible(fields.get("PacketPlayOutEntityDestroy_ENTITIES"));
		fields.put("PacketPlayOutEntityMetadata_LIST", getFields(getClass("PacketPlayOutEntityMetadata"), List.class).get(0));
		fields.put("PacketPlayOutNamedEntitySpawn_ENTITYID", getFields(getClass("PacketPlayOutNamedEntitySpawn"), int.class).get(0));
		if (minorVersion >= 7) {
			classes.put("EnumEntityUseAction", getNMSClass("net.minecraft.network.protocol.game.PacketPlayInUseEntity$EnumEntityUseAction", "PacketPlayInUseEntity$EnumEntityUseAction", "EnumEntityUseAction", "net.minecraft.class_2824$class_5906"));
			fields.put("PacketPlayInUseEntity_ENTITY", getFields(getClass("PacketPlayInUseEntity"), int.class).get(0));
			fields.put("PacketPlayInUseEntity_ACTION", getFields(getClass("PacketPlayInUseEntity"), getClass("EnumEntityUseAction")).get(0));
		}
		if (minorVersion >= 17) {
			classes.put("PacketPlayInUseEntity$d", Class.forName("net.minecraft.network.protocol.game.PacketPlayInUseEntity$d"));
		}
		try {
			constructors.put("PacketPlayOutEntityDestroy", getClass("PacketPlayOutEntityDestroy").getConstructor(int[].class));
		} catch (NoSuchMethodException e) {
			//1.17.0
			constructors.put("PacketPlayOutEntityDestroy", getClass("PacketPlayOutEntityDestroy").getConstructor(int.class));
		}
	}
	
	private void initializePlayerInfoPacket() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		if (minorVersion >= 8) {
			classes.put("PacketPlayOutPlayerInfo", getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo", "PacketPlayOutPlayerInfo"));
			classes.put("EnumPlayerInfoAction", getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo$EnumPlayerInfoAction", "PacketPlayOutPlayerInfo$EnumPlayerInfoAction", "EnumPlayerInfoAction", "net.minecraft.class_2703$class_5893"));
			classes.put("PlayerInfoData", getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo$PlayerInfoData", "PacketPlayOutPlayerInfo$PlayerInfoData", "PlayerInfoData"));
			classes.put("EnumGamemode", getNMSClass("net.minecraft.world.level.EnumGamemode", "EnumGamemode", "WorldSettings$EnumGamemode"));
			constructors.put("PacketPlayOutPlayerInfo", getClass("PacketPlayOutPlayerInfo").getConstructor(getClass("EnumPlayerInfoAction"), Array.newInstance(getClass("EntityPlayer"), 0).getClass()));
			constructors.put("PlayerInfoData", getClass("PlayerInfoData").getConstructors()[0]);
			fields.put("PacketPlayOutPlayerInfo_ACTION", getFields(getClass("PacketPlayOutPlayerInfo"), getClass("EnumPlayerInfoAction")).get(0));
			fields.put("PacketPlayOutPlayerInfo_PLAYERS", getFields(getClass("PacketPlayOutPlayerInfo"), List.class).get(0));
			methods.put("PlayerInfoData_getProfile", getMethods(getClass("PlayerInfoData"), GameProfile.class).get(0));
			methods.put("PlayerInfoData_getLatency", getMethods(getClass("PlayerInfoData"), int.class).get(0));
			methods.put("PlayerInfoData_getGamemode", getMethods(getClass("PlayerInfoData"), getClass("EnumGamemode")).get(0));
			methods.put("PlayerInfoData_getDisplayName", getMethods(getClass("PlayerInfoData"), getClass("IChatBaseComponent")).get(0));
		}
	}
	
	private void initializeScoreboardPackets() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		classes.put("PacketPlayOutScoreboardDisplayObjective", getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutScoreboardDisplayObjective", "PacketPlayOutScoreboardDisplayObjective", "Packet208SetScoreboardDisplayObjective"));
		classes.put("PacketPlayOutScoreboardObjective", getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutScoreboardObjective", "PacketPlayOutScoreboardObjective", "Packet206SetScoreboardObjective"));
		classes.put("PacketPlayOutScoreboardScore", getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutScoreboardScore", "PacketPlayOutScoreboardScore", "Packet207SetScoreboardScore"));
		classes.put("Scoreboard", getNMSClass("net.minecraft.world.scores.Scoreboard", "Scoreboard"));
		classes.put("ScoreboardObjective", getNMSClass("net.minecraft.world.scores.ScoreboardObjective", "ScoreboardObjective"));
		classes.put("ScoreboardScore", getNMSClass("net.minecraft.world.scores.ScoreboardScore", "ScoreboardScore"));
		classes.put("IScoreboardCriteria", getNMSClass("net.minecraft.world.scores.criteria.IScoreboardCriteria", "IScoreboardCriteria"));
		constructors.put("ScoreboardObjective", getClass("ScoreboardObjective").getConstructors()[0]);
		constructors.put("Scoreboard", getClass("Scoreboard").getConstructor());
		constructors.put("ScoreboardScore", getClass("ScoreboardScore").getConstructor(getClass("Scoreboard"), getClass("ScoreboardObjective"), String.class));
		constructors.put("PacketPlayOutScoreboardDisplayObjective", getClass("PacketPlayOutScoreboardDisplayObjective").getConstructor(int.class, getClass("ScoreboardObjective")));
		fields.put("PacketPlayOutScoreboardDisplayObjective_POSITION", getFields(getClass("PacketPlayOutScoreboardDisplayObjective"), int.class).get(0));
		fields.put("PacketPlayOutScoreboardDisplayObjective_OBJECTIVENAME", getFields(getClass("PacketPlayOutScoreboardDisplayObjective"), String.class).get(0));
		fields.put("PacketPlayOutScoreboardObjective_OBJECTIVENAME", getFields(getClass("PacketPlayOutScoreboardObjective"), String.class).get(0));
		List<Field> list = getFields(getClass("PacketPlayOutScoreboardObjective"), int.class);
		fields.put("PacketPlayOutScoreboardObjective_METHOD", list.get(list.size()-1));
		fields.put("IScoreboardCriteria", getFields(getClass("IScoreboardCriteria"), getClass("IScoreboardCriteria")).get(0));
		methods.put("ScoreboardScore_setScore", getMethod(getClass("ScoreboardScore"), new String[]{"setScore", "func_96647_c", "method_1128"}, int.class));
		if (minorVersion >= 8) {
			classes.put("EnumScoreboardHealthDisplay", getNMSClass("net.minecraft.world.scores.criteria.IScoreboardCriteria$EnumScoreboardHealthDisplay", "IScoreboardCriteria$EnumScoreboardHealthDisplay", "EnumScoreboardHealthDisplay"));
			classes.put("EnumScoreboardAction", getNMSClass("net.minecraft.server.ScoreboardServer$Action", "ScoreboardServer$Action", "PacketPlayOutScoreboardScore$EnumScoreboardAction", "EnumScoreboardAction"));
			fields.put("PacketPlayOutScoreboardObjective_RENDERTYPE", getFields(getClass("PacketPlayOutScoreboardObjective"), getClass("EnumScoreboardHealthDisplay")).get(0));
		}
		if (minorVersion >= 13) {
			constructors.put("PacketPlayOutScoreboardObjective", getClass("PacketPlayOutScoreboardObjective").getConstructor(getClass("ScoreboardObjective"), int.class));
			constructors.put("PacketPlayOutScoreboardScore_1_13", getClass("PacketPlayOutScoreboardScore").getConstructor(getClass("EnumScoreboardAction"), String.class, String.class, int.class));
			fields.put("PacketPlayOutScoreboardObjective_DISPLAYNAME", getFields(getClass("PacketPlayOutScoreboardObjective"), getClass("IChatBaseComponent")).get(0));
		} else {
			constructors.put("PacketPlayOutScoreboardObjective", getClass("PacketPlayOutScoreboardObjective").getConstructor());
			constructors.put("PacketPlayOutScoreboardScore_String", getClass("PacketPlayOutScoreboardScore").getConstructor(String.class));
			fields.put("PacketPlayOutScoreboardObjective_DISPLAYNAME", getFields(getClass("PacketPlayOutScoreboardObjective"), String.class).get(1));
			if (minorVersion >= 8) {
				constructors.put("PacketPlayOutScoreboardScore", getClass("PacketPlayOutScoreboardScore").getConstructor(getClass("ScoreboardScore")));
			} else {
				constructors.put("PacketPlayOutScoreboardScore", getClass("PacketPlayOutScoreboardScore").getConstructor(getClass("ScoreboardScore"), int.class));
			}
		}
	}
	
	private void initializeTeamPackets() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		classes.put("PacketPlayOutScoreboardTeam", getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam", "PacketPlayOutScoreboardTeam", "Packet209SetScoreboardTeam"));
		classes.put("Scoreboard", getNMSClass("net.minecraft.world.scores.Scoreboard", "Scoreboard"));
		classes.put("ScoreboardTeam", getNMSClass("net.minecraft.world.scores.ScoreboardTeam", "ScoreboardTeam"));
		constructors.put("Scoreboard", getClass("Scoreboard").getConstructor());
		constructors.put("ScoreboardTeam", getClass("ScoreboardTeam").getConstructor(getClass("Scoreboard"), String.class));
		fields.put("PacketPlayOutScoreboardTeam_NAME", getFields(getClass("PacketPlayOutScoreboardTeam"), String.class).get(0));
		fields.put("PacketPlayOutScoreboardTeam_PLAYERS", getFields(getClass("PacketPlayOutScoreboardTeam"), Collection.class).get(0));
		methods.put("ScoreboardTeam_getPlayerNameSet", getMethods(getClass("ScoreboardTeam"), Collection.class).get(0));
		if (minorVersion >= 8) {
			classes.put("EnumNameTagVisibility", getNMSClass("net.minecraft.world.scores.ScoreboardTeamBase$EnumNameTagVisibility", "ScoreboardTeamBase$EnumNameTagVisibility", "EnumNameTagVisibility"));
			methods.put("ScoreboardTeam_setNameTagVisibility", getMethod(getClass("ScoreboardTeam"), new String[]{"setNameTagVisibility", "a", "method_1149"}, getClass("EnumNameTagVisibility")));
		}
		if (minorVersion >= 9) {
			classes.put("EnumTeamPush", getNMSClass("net.minecraft.world.scores.ScoreboardTeamBase$EnumTeamPush", "ScoreboardTeamBase$EnumTeamPush"));
			methods.put("ScoreboardTeam_setCollisionRule", getMethods(getClass("ScoreboardTeam"), void.class, getClass("EnumTeamPush")).get(0));
		}
		if (minorVersion >= 13) {
			methods.put("ScoreboardTeam_setPrefix", getMethod(getClass("ScoreboardTeam"), new String[]{"setPrefix", "method_1138"}, getClass("IChatBaseComponent")));
			methods.put("ScoreboardTeam_setSuffix", getMethod(getClass("ScoreboardTeam"), new String[]{"setPrefix", "method_1139"}, getClass("IChatBaseComponent")));
			methods.put("ScoreboardTeam_setColor", getMethods(getClass("ScoreboardTeam"), void.class, getClass("EnumChatFormat")).get(0));
		} else {
			methods.put("ScoreboardTeam_setPrefix", getMethod(getClass("ScoreboardTeam"), new String[]{"setPrefix", "func_96666_b"}, String.class));
			methods.put("ScoreboardTeam_setSuffix", getMethod(getClass("ScoreboardTeam"), new String[]{"setSuffix", "func_96662_c"}, String.class));
		}
		try {
			classes.put("PacketPlayOutScoreboardTeam_a", Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam$a"));
			methods.put("PacketPlayOutScoreboardTeam_of", getMethods(getClass("PacketPlayOutScoreboardTeam"), getClass("PacketPlayOutScoreboardTeam"), getClass("ScoreboardTeam")).get(0));
			methods.put("PacketPlayOutScoreboardTeam_ofBoolean", getMethods(getClass("PacketPlayOutScoreboardTeam"), getClass("PacketPlayOutScoreboardTeam"), getClass("ScoreboardTeam"), boolean.class).get(0));
			methods.put("PacketPlayOutScoreboardTeam_ofString", getMethods(getClass("PacketPlayOutScoreboardTeam"), getClass("PacketPlayOutScoreboardTeam"), getClass("ScoreboardTeam"), String.class, getClass("PacketPlayOutScoreboardTeam_a")).get(0));
		} catch (ClassNotFoundException e) {
			constructors.put("PacketPlayOutScoreboardTeam", getClass("PacketPlayOutScoreboardTeam").getConstructor(getClass("ScoreboardTeam"), int.class));
		}
	}

	/**
	 * A helper method that prints all methods of class into console, including their return type, name and parameters
	 * Useful for modded servers which code I can not access
	 * @param clazz - class to show methods of
	 */
	@SuppressWarnings("unused")
	private void showMethods(Class<?> clazz) {
		Bukkit.getConsoleSender().sendMessage("--- " + clazz.getSimpleName() + " ---");
		for (Method m : clazz.getMethods()) {
			Bukkit.getConsoleSender().sendMessage(m.getReturnType().getName() + " " + m.getName() + "(" + Arrays.toString(m.getParameterTypes()) + ")");
		}
	}

	/**
	 * Returns class with given potential names in same order
	 * @param names - possible class names
	 * @return class for specified name(s)
	 * @throws ClassNotFoundException if class does not exist
	 */
	private Class<?> getNMSClass(String... names) throws ClassNotFoundException {
		for (String name : names) {
			try {
				return getNMSClass(name);
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
	private Class<?> getNMSClass(String name) throws ClassNotFoundException {
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException ex) {
			try {
				return Class.forName("net.minecraft.server." + serverPackage + "." + name);
			} catch (ClassNotFoundException | NullPointerException e) {
				//modded server?
				Class<?> clazz = Main.class.getClassLoader().loadClass("net.minecraft.server." + serverPackage + "." + name);
				if (clazz != null) return clazz;
			}
		}
		throw new ClassNotFoundException(name);
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
		new NoSuchMethodException("No method found with possible names " + Arrays.toString(names) + " with parameters " + Arrays.toString(parameterTypes) + " in class " + clazz.getName()).printStackTrace();
		return null;
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
	public List<Field> getFields(Class<?> clazz, Class<?> type){
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

	private Constructor<?> getConstructor(Class<?> clazz, int parameterCount) throws NoSuchMethodException {
		for (Constructor<?> c : clazz.getDeclaredConstructors()) {
			if (c.getParameterCount() == parameterCount) {
				return setAccessible(c);
			}
		}
		throw new NoSuchMethodException("No constructor found in class " + clazz.getName() + " with " + parameterCount + " parameters");
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

	public int getMinorVersion() {
		return minorVersion;
	}

	public Class<?> getClass(String name){
		return classes.get(name);
	}

	public Constructor getConstructor(String name){
		return constructors.get(name);
	}

	public Field getField(String name){
		return fields.get(name);
	}

	public Method getMethod(String name) {
		return methods.get(name);
	}

	public Enum[] getEnum(String name) {
		return enums.get(name);
	}

	public void setField(Object obj, String field, Object value) throws IllegalAccessException {
		fields.get(field).set(obj, value);
	}

	public DataWatcherRegistry getDataWatcherRegistry() {
		return dataWatcherRegistry;
	}
	
	public <T extends AccessibleObject> T setAccessible(T o) {
		o.setAccessible(true);
		return o;
	}
}