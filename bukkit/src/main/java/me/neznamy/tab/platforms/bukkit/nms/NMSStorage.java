package me.neznamy.tab.platforms.bukkit.nms;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;

import io.netty.channel.Channel;
import me.neznamy.tab.platforms.bukkit.Main;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherRegistry;
import me.neznamy.tab.shared.TAB;

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
	private Map<String, Enum[]> enums = new HashMap<>();

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
		classes.put("Packet", getNMSClass("net.minecraft.network.protocol.Packet", "Packet"));
		classes.put("PlayerConnection", getNMSClass("net.minecraft.server.network.PlayerConnection", "PlayerConnection"));
		classes.put("NetworkManager", getNMSClass("net.minecraft.network.NetworkManager", "NetworkManager"));
		fields.put("PING", getField(getClass("EntityPlayer"), "ping", "latency", "e"));
		fields.put("PLAYER_CONNECTION", getFields(getClass("EntityPlayer"), getClass("PlayerConnection")).get(0));
		methods.put("getHandle", Class.forName("org.bukkit.craftbukkit." + serverPackage + ".entity.CraftPlayer").getMethod("getHandle"));
		methods.put("sendPacket", getMethod(getClass("PlayerConnection"), new String[]{"sendPacket", "func_147359_a"}, getClass("Packet")));
		if (minorVersion >= 7) {
			fields.put("NETWORK_MANAGER", getFields(getClass("PlayerConnection"), getClass("NetworkManager")).get(0));
		}
		if (minorVersion >= 8) {
			fields.put("CHANNEL", getFields(getClass("NetworkManager"), Channel.class).get(0));
			methods.put("getProfile", getClass("EntityPlayer").getMethod("getProfile"));
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
			constructors.put("ChatHoverable", getClass("ChatHoverable").getConstructors()[0]);
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
			methods.put("ChatComponentText_addSibling", getMethod(getClass("ChatComponentText"), new String[]{"addSibling", "a", "func_150257_a"}, getClass("IChatBaseComponent"))); //v1.7.R4+, v1.7.R3-
			methods.put("ChatSerializer_DESERIALIZE", getMethod(getClass("ChatSerializer"), new String[]{"a", "func_150699_a"}, String.class));
			methods.put("EnumClickAction_a", getMethod(getClass("EnumClickAction"), new String[]{"a", "func_150672_a"}, String.class));
			methods.put("EnumHoverAction_a", getMethod(getClass("EnumHoverAction"), new String[]{"a", "func_150684_a"}, String.class));
		}
		if (minorVersion >= 16) {
			classes.put("ChatHexColor", getNMSClass("net.minecraft.network.chat.ChatHexColor", "ChatHexColor"));
			constructors.put("ChatModifier", getConstructor(getClass("ChatModifier"), 10));
			List<Field> list = getFields(getClass("ChatHexColor"), String.class);
			fields.put("ChatHexColor_name", list.get(list.size()-1));
			fields.put("ChatHexColor_rgb", getFields(getClass("ChatHexColor"), int.class).get(0));
			fields.put("ChatHoverable_value", getFields(getClass("ChatHoverable"), Object.class).get(0));
			fields.put("ChatModifier_color", getFields(getClass("ChatModifier"), getClass("ChatHexColor")).get(0));
			methods.put("ChatHexColor_ofInt", getClass("ChatHexColor").getMethod("a", int.class));
			methods.put("ChatHexColor_ofString", getClass("ChatHexColor").getMethod("a", String.class));
		} else if (minorVersion >= 7) {
			constructors.put("ChatModifier", getClass("ChatModifier").getConstructor());
			fields.put("ChatHoverable_value", getFields(getClass("ChatHoverable"), getClass("IChatBaseComponent")).get(0));
			fields.put("ChatModifier_color", getFields(getClass("ChatModifier"), getClass("EnumChatFormat")).get(0));
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
			methods.put("DataWatcher_REGISTER", getClass("DataWatcher").getMethod("register", getClass("DataWatcherObject"), Object.class));
		} else {
			fields.put("DataWatcherItem_TYPE", getFields(getClass("DataWatcherItem"), int.class).get(1));
			fields.put("DataWatcherItem_VALUE", getFields(getClass("DataWatcherItem"), Object.class).get(0));
			methods.put("DataWatcher_REGISTER", getMethod(getClass("DataWatcher"), new String[]{"a", "func_75682_a"}, int.class, Object.class));
		}
		dataWatcherRegistry = new DataWatcherRegistry(this, getClass("DataWatcherRegistry"));
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
			if (minorVersion >= 17) {
				constructors.put("PacketPlayOutPlayerListHeaderFooter", getClass("PacketPlayOutPlayerListHeaderFooter").getConstructor(getClass("IChatBaseComponent"), getClass("IChatBaseComponent")));
			} else {
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
		fields.get("PacketPlayOutEntityDestroy_ENTITIES").setAccessible(true);
		fields.put("PacketPlayOutEntityMetadata_LIST", getFields(getClass("PacketPlayOutEntityMetadata"), List.class).get(0));
		fields.put("PacketPlayOutNamedEntitySpawn_ENTITYID", getFields(getClass("PacketPlayOutNamedEntitySpawn"), int.class).get(0));
		if (minorVersion >= 7) {
			classes.put("EnumEntityUseAction", getNMSClass("net.minecraft.network.protocol.game.PacketPlayInUseEntity$EnumEntityUseAction", "PacketPlayInUseEntity$EnumEntityUseAction", "EnumEntityUseAction"));
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
			classes.put("EnumPlayerInfoAction", getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo$EnumPlayerInfoAction", "PacketPlayOutPlayerInfo$EnumPlayerInfoAction", "EnumPlayerInfoAction"));
			classes.put("PlayerInfoData", getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo$PlayerInfoData", "PacketPlayOutPlayerInfo$PlayerInfoData", "PlayerInfoData"));
			classes.put("EnumGamemode", getNMSClass("net.minecraft.world.level.EnumGamemode", "EnumGamemode", "WorldSettings$EnumGamemode"));
			constructors.put("PacketPlayOutPlayerInfo", getClass("PacketPlayOutPlayerInfo").getConstructor(getClass("EnumPlayerInfoAction"), Array.newInstance(getClass("EntityPlayer"), 0).getClass()));
			constructors.put("PlayerInfoData", getClass("PlayerInfoData").getConstructors()[0]);
			fields.put("PacketPlayOutPlayerInfo_ACTION", getFields(getClass("PacketPlayOutPlayerInfo"), getClass("EnumPlayerInfoAction")).get(0));
			fields.put("PacketPlayOutPlayerInfo_PLAYERS", getFields(getClass("PacketPlayOutPlayerInfo"), List.class).get(0));
			methods.put("PlayerInfoData_getProfile", getClass("PlayerInfoData").getMethod("a"));
			methods.put("PlayerInfoData_getLatency", getClass("PlayerInfoData").getMethod("b"));
			methods.put("PlayerInfoData_getGamemode", getClass("PlayerInfoData").getMethod("c"));
			methods.put("PlayerInfoData_getDisplayName", getClass("PlayerInfoData").getMethod("d"));
		}
	}
	
	private void initializeScoreboardPackets() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		classes.put("PacketPlayOutScoreboardDisplayObjective", getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutScoreboardDisplayObjective", "PacketPlayOutScoreboardDisplayObjective", "Packet208SetScoreboardDisplayObjective"));
		classes.put("PacketPlayOutScoreboardObjective", getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutScoreboardObjective", "PacketPlayOutScoreboardObjective", "Packet206SetScoreboardObjective"));
		classes.put("PacketPlayOutScoreboardScore", getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutScoreboardScore", "PacketPlayOutScoreboardScore", "Packet207SetScoreboardScore"));
		classes.put("PacketPlayOutScoreboardTeam", getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam", "PacketPlayOutScoreboardTeam", "Packet209SetScoreboardTeam"));
		classes.put("Scoreboard", getNMSClass("net.minecraft.world.scores.Scoreboard", "Scoreboard"));
		classes.put("ScoreboardObjective", getNMSClass("net.minecraft.world.scores.ScoreboardObjective", "ScoreboardObjective"));
		classes.put("ScoreboardTeam", getNMSClass("net.minecraft.world.scores.ScoreboardTeam", "ScoreboardTeam"));
		classes.put("ScoreboardScore", getNMSClass("net.minecraft.world.scores.ScoreboardScore", "ScoreboardScore"));
		classes.put("IScoreboardCriteria", getNMSClass("net.minecraft.world.scores.criteria.IScoreboardCriteria", "IScoreboardCriteria"));
		constructors.put("ScoreboardObjective", getClass("ScoreboardObjective").getConstructors()[0]);
		constructors.put("Scoreboard", getClass("Scoreboard").getConstructor());
		constructors.put("ScoreboardTeam", getClass("ScoreboardTeam").getConstructor(getClass("Scoreboard"), String.class));
		constructors.put("ScoreboardScore", getClass("ScoreboardScore").getConstructor(getClass("Scoreboard"), getClass("ScoreboardObjective"), String.class));
		constructors.put("PacketPlayOutScoreboardDisplayObjective", getClass("PacketPlayOutScoreboardDisplayObjective").getConstructor(int.class, getClass("ScoreboardObjective")));
		fields.put("PacketPlayOutScoreboardDisplayObjective_POSITION", getFields(getClass("PacketPlayOutScoreboardDisplayObjective"), int.class).get(0));
		fields.put("PacketPlayOutScoreboardDisplayObjective_OBJECTIVENAME", getFields(getClass("PacketPlayOutScoreboardDisplayObjective"), String.class).get(0));
		fields.put("PacketPlayOutScoreboardObjective_OBJECTIVENAME", getFields(getClass("PacketPlayOutScoreboardObjective"), String.class).get(0));
		List<Field> list = getFields(getClass("PacketPlayOutScoreboardObjective"), int.class);
		fields.put("PacketPlayOutScoreboardObjective_METHOD", list.get(list.size()-1));
		fields.put("PacketPlayOutScoreboardTeam_NAME", getFields(getClass("PacketPlayOutScoreboardTeam"), String.class).get(0));
		fields.put("PacketPlayOutScoreboardTeam_PLAYERS", getFields(getClass("PacketPlayOutScoreboardTeam"), Collection.class).get(0));
		methods.put("ScoreboardTeam_getPlayerNameSet", getMethod(getClass("ScoreboardTeam"), new String[]{"getPlayerNameSet", "func_96670_d"}));
		methods.put("ScoreboardScore_setScore", getMethod(getClass("ScoreboardScore"), new String[]{"setScore", "func_96647_c"}, int.class));
		if (minorVersion >= 8) {
			classes.put("EnumScoreboardHealthDisplay", getNMSClass("net.minecraft.world.scores.criteria.IScoreboardCriteria$EnumScoreboardHealthDisplay", "IScoreboardCriteria$EnumScoreboardHealthDisplay", "EnumScoreboardHealthDisplay"));
			classes.put("EnumScoreboardAction", getNMSClass("net.minecraft.server.ScoreboardServer$Action", "ScoreboardServer$Action", "PacketPlayOutScoreboardScore$EnumScoreboardAction", "EnumScoreboardAction"));
			classes.put("EnumNameTagVisibility", getNMSClass("net.minecraft.world.scores.ScoreboardTeamBase$EnumNameTagVisibility", "ScoreboardTeamBase$EnumNameTagVisibility", "EnumNameTagVisibility"));
			fields.put("PacketPlayOutScoreboardObjective_RENDERTYPE", getFields(getClass("PacketPlayOutScoreboardObjective"), getClass("EnumScoreboardHealthDisplay")).get(0));
			methods.put("ScoreboardTeam_setNameTagVisibility", getMethod(getClass("ScoreboardTeam"), new String[]{"setNameTagVisibility", "a"}, getClass("EnumNameTagVisibility")));
		}
		if (minorVersion >= 9) {
			classes.put("EnumTeamPush", getNMSClass("net.minecraft.world.scores.ScoreboardTeamBase$EnumTeamPush", "ScoreboardTeamBase$EnumTeamPush"));
			methods.put("ScoreboardTeam_setCollisionRule", getClass("ScoreboardTeam").getMethod("setCollisionRule", getClass("EnumTeamPush")));
		}
		if (minorVersion >= 13) {
			constructors.put("PacketPlayOutScoreboardObjective", getClass("PacketPlayOutScoreboardObjective").getConstructor(getClass("ScoreboardObjective"), int.class));
			constructors.put("PacketPlayOutScoreboardScore_1_13", getClass("PacketPlayOutScoreboardScore").getConstructor(getClass("EnumScoreboardAction"), String.class, String.class, int.class));
			fields.put("PacketPlayOutScoreboardObjective_DISPLAYNAME", getFields(getClass("PacketPlayOutScoreboardObjective"), getClass("IChatBaseComponent")).get(0));
			methods.put("ScoreboardTeam_setPrefix", getClass("ScoreboardTeam").getMethod("setPrefix", getClass("IChatBaseComponent")));
			methods.put("ScoreboardTeam_setSuffix", getClass("ScoreboardTeam").getMethod("setSuffix", getClass("IChatBaseComponent")));
			methods.put("ScoreboardTeam_setColor", getClass("ScoreboardTeam").getMethod("setColor", getClass("EnumChatFormat")));
		} else {
			constructors.put("PacketPlayOutScoreboardObjective", getClass("PacketPlayOutScoreboardObjective").getConstructor());
			constructors.put("PacketPlayOutScoreboardScore_String", getClass("PacketPlayOutScoreboardScore").getConstructor(String.class));
			fields.put("PacketPlayOutScoreboardObjective_DISPLAYNAME", getFields(getClass("PacketPlayOutScoreboardObjective"), String.class).get(1));
			methods.put("ScoreboardTeam_setPrefix", getMethod(getClass("ScoreboardTeam"), new String[]{"setPrefix", "func_96666_b"}, String.class));
			methods.put("ScoreboardTeam_setSuffix", getMethod(getClass("ScoreboardTeam"), new String[]{"setSuffix", "func_96662_c"}, String.class));
			if (minorVersion >= 8) {
				constructors.put("PacketPlayOutScoreboardScore", getClass("PacketPlayOutScoreboardScore").getConstructor(getClass("ScoreboardScore")));
			} else {
				constructors.put("PacketPlayOutScoreboardScore", getClass("PacketPlayOutScoreboardScore").getConstructor(getClass("ScoreboardScore"), int.class));
			}
		}
		if (minorVersion >= 17) {
			classes.put("PacketPlayOutScoreboardTeam_a", Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam$a"));
			methods.put("PacketPlayOutScoreboardTeam_of", getClass("PacketPlayOutScoreboardTeam").getMethod("a", getClass("ScoreboardTeam")));
			methods.put("PacketPlayOutScoreboardTeam_ofBoolean", getClass("PacketPlayOutScoreboardTeam").getMethod("a", getClass("ScoreboardTeam"), boolean.class));
			methods.put("PacketPlayOutScoreboardTeam_ofString", getClass("PacketPlayOutScoreboardTeam").getMethod("a", getClass("ScoreboardTeam"), String.class, getClass("PacketPlayOutScoreboardTeam_a")));
		} else {
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
		TAB.getInstance().getPlatform().sendConsoleMessage("--- " + clazz.getSimpleName() + " ---", false);
		for (Method m : clazz.getMethods()) {
			TAB.getInstance().getPlatform().sendConsoleMessage(m.getReturnType().getName() + " " + m.getName() + "(" + Arrays.toString(m.getParameterTypes()) + ")", false);
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
			} catch (ClassNotFoundException e) {
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
		if (minorVersion >= 17) {
			return Class.forName(name);
		} else {
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
				return clazz.getMethod(name, parameterTypes);
			} catch (Exception e) {
				//not the first method in array
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
	public List<Field> getFields(Class<?> clazz, Class<?> type){
		if (clazz == null) throw new IllegalArgumentException("Source class cannot be null");
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
		if (name.equals("ping")){
			return getField(clazz, "field_71138_i"); //thermos
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
}