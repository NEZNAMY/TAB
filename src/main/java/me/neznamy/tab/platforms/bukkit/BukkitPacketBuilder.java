package me.neznamy.tab.platforms.bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.neznamy.tab.platforms.bukkit.nms.NMSHook;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityDestroy;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityMetadata;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.features.bossbar.BossBar;
import me.neznamy.tab.shared.packets.EnumChatFormat;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketBuilder;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerListHeaderFooter;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;
import us.myles.ViaVersion.api.type.Type;
import us.myles.viaversion.libs.gson.JsonParser;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss.Action;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;

@SuppressWarnings({"rawtypes", "unchecked"})
public class BukkitPacketBuilder implements PacketBuilder {

	public static Class<Enum> EnumChatFormat_;

	//PacketPlayOutBoss
	private static Class<?> PacketPlayOutBoss;
	private static Class<?> BarColor;
	private static Class<?> BarStyle;
	private static Class<Enum> PacketPlayOutBoss_Action;
	private static Constructor<?> newPacketPlayOutBoss;
	private static Field PacketPlayOutBoss_UUID;
	private static Field PacketPlayOutBoss_ACTION;
	private static Field PacketPlayOutBoss_NAME;
	private static Field PacketPlayOutBoss_PROGRESS;
	private static Field PacketPlayOutBoss_COLOR;
	private static Field PacketPlayOutBoss_STYLE;
	private static Field PacketPlayOutBoss_DARKEN_SKY;
	private static Field PacketPlayOutBoss_PLAY_MUSIC;
	private static Field PacketPlayOutBoss_CREATE_FOG;

	//PacketPlayOutChat
	private static Class<?> PacketPlayOutChat;
	private static Class<?> ChatMessageType;
	private static Constructor<?> newPacketPlayOutChat;

	//PacketPlayOutPlayerListHeaderFooter
	private static Class<?> PacketPlayOutPlayerListHeaderFooter;
	private static Constructor<?> newPacketPlayOutPlayerListHeaderFooter;
	private static Field PacketPlayOutPlayerListHeaderFooter_HEADER;
	private static Field PacketPlayOutPlayerListHeaderFooter_FOOTER;

	//PacketPlayOutScoreboardDisplayObjective
	private static Class<?> PacketPlayOutScoreboardDisplayObjective;
	private static Constructor<?> newPacketPlayOutScoreboardDisplayObjective;
	private static Field PacketPlayOutScoreboardDisplayObjective_POSITION;
	private static Field PacketPlayOutScoreboardDisplayObjective_OBJECTIVENAME;

	//PacketPlayOutScoreboardObjective
	private static Class<?> PacketPlayOutScoreboardObjective;
	private static Class<Enum> EnumScoreboardHealthDisplay;
	private static Constructor<?> newPacketPlayOutScoreboardObjective;
	private static Field PacketPlayOutScoreboardObjective_OBJECTIVENAME;
	private static Field PacketPlayOutScoreboardObjective_DISPLAYNAME;
	private static Field PacketPlayOutScoreboardObjective_RENDERTYPE;
	private static Field PacketPlayOutScoreboardObjective_METHOD;

	//PacketPlayOutScoreboardScore
	private static Class<?> PacketPlayOutScoreboardScore;
	private static Class<Enum> EnumScoreboardAction;
	private static Constructor<?> newPacketPlayOutScoreboardScore0;
	private static Constructor<?> newPacketPlayOutScoreboardScore_String;
	private static Constructor<?> newPacketPlayOutScoreboardScore_1_13;
	private static Field PacketPlayOutScoreboardScore_PLAYER;
	private static Field PacketPlayOutScoreboardScore_OBJECTIVENAME;
	private static Field PacketPlayOutScoreboardScore_SCORE;
	private static Field PacketPlayOutScoreboardScore_ACTION;

	//PacketPlayOutScoreboardTeam
	public static Class<?> PacketPlayOutScoreboardTeam;
	private static Constructor<?> newPacketPlayOutScoreboardTeam;
	private static Field PacketPlayOutScoreboardTeam_NAME;
	private static Field PacketPlayOutScoreboardTeam_DISPLAYNAME;
	private static Field PacketPlayOutScoreboardTeam_PREFIX;
	private static Field PacketPlayOutScoreboardTeam_SUFFIX;
	private static Field PacketPlayOutScoreboardTeam_VISIBILITY; //1.8+
	private static Field PacketPlayOutScoreboardTeam_CHATFORMAT; //1.13+
	private static Field PacketPlayOutScoreboardTeam_COLLISION; //1.9+
	public static Field PacketPlayOutScoreboardTeam_PLAYERS;
	private static Field PacketPlayOutScoreboardTeam_ACTION;
	public static Field PacketPlayOutScoreboardTeam_SIGNATURE;

	//PacketPlayOutPlayerInfo
	private static Class<?> PacketPlayOutPlayerInfo;
	private static Class<Enum> EnumGamemode;
	private static Class<Enum> EnumPlayerInfoAction;
	private static Class<?> PlayerInfoData;
	private static Constructor<?> newPacketPlayOutPlayerInfo0;
	private static Constructor<?> newPacketPlayOutPlayerInfo2;
	private static Constructor<?> newPlayerInfoData;

	private static Class<?> GameProfile;
	private static Constructor<?> newGameProfile;
	private static Field GameProfile_ID;
	private static Field GameProfile_NAME;
	private static Field GameProfile_PROPERTIES;
	private static Class<?> PropertyMap;
	private static Method PropertyMap_putAll;

	private static Field PacketPlayOutPlayerInfo_ACTION;
	private static Field PacketPlayOutPlayerInfo_PLAYERS;

	private static Field PlayerInfoData_PING;
	private static Field PlayerInfoData_GAMEMODE;
	private static Field PlayerInfoData_PROFILE;
	private static Field PlayerInfoData_LISTNAME;

	public static void initializeClass() throws Exception {
		//Initializing classes
		EnumChatFormat_ = (Class<Enum>) getNMSClass("EnumChatFormat");
		try {
			//1.7+
			PacketPlayOutChat = getNMSClass("PacketPlayOutChat");
			PacketPlayOutScoreboardDisplayObjective = getNMSClass("PacketPlayOutScoreboardDisplayObjective");
			PacketPlayOutScoreboardObjective = getNMSClass("PacketPlayOutScoreboardObjective");
			PacketPlayOutScoreboardScore = getNMSClass("PacketPlayOutScoreboardScore");
			PacketPlayOutScoreboardTeam = getNMSClass("PacketPlayOutScoreboardTeam");
		} catch (ClassNotFoundException e) {
			//1.6-
			PacketPlayOutChat = getNMSClass("Packet3Chat");
			PacketPlayOutScoreboardDisplayObjective = getNMSClass("Packet208SetScoreboardDisplayObjective");
			PacketPlayOutScoreboardObjective = getNMSClass("Packet206SetScoreboardObjective");
			PacketPlayOutScoreboardScore = getNMSClass("Packet207SetScoreboardScore");
			PacketPlayOutScoreboardTeam = getNMSClass("Packet209SetScoreboardTeam");
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 12) {
			ChatMessageType = getNMSClass("ChatMessageType");
		}



		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 16) {
			newPacketPlayOutChat = PacketPlayOutChat.getConstructor(NMSHook.IChatBaseComponent, ChatMessageType, UUID.class);
		} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 12) {
			newPacketPlayOutChat = PacketPlayOutChat.getConstructor(NMSHook.IChatBaseComponent, ChatMessageType);
		} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			newPacketPlayOutChat = PacketPlayOutChat.getConstructor(NMSHook.IChatBaseComponent, byte.class);
		} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 6) {
			try {
				//v1_7_R4
				newPacketPlayOutChat = PacketPlayOutChat.getConstructor(NMSHook.IChatBaseComponent, int.class);
			} catch (Exception e) {
				newPacketPlayOutChat = PacketPlayOutChat.getConstructor(NMSHook.IChatBaseComponent, boolean.class);
			}
		} else {
			newPacketPlayOutChat = PacketPlayOutChat.getConstructor(String.class, boolean.class);
		}

		//PacketPlayOutScoreboardDisplayObjective
		newPacketPlayOutScoreboardDisplayObjective = PacketPlayOutScoreboardDisplayObjective.getConstructor();
		(PacketPlayOutScoreboardDisplayObjective_POSITION = PacketPlayOutScoreboardDisplayObjective.getDeclaredField("a")).setAccessible(true);
		(PacketPlayOutScoreboardDisplayObjective_OBJECTIVENAME = PacketPlayOutScoreboardDisplayObjective.getDeclaredField("b")).setAccessible(true);

		//PacketPlayOutScoreboardObjective
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			try {
				//v1_8_R2+
				EnumScoreboardHealthDisplay = (Class<Enum>) getNMSClass("IScoreboardCriteria$EnumScoreboardHealthDisplay");
			} catch (ClassNotFoundException e) {
				//v1_8_R1
				EnumScoreboardHealthDisplay = (Class<Enum>) getNMSClass("EnumScoreboardHealthDisplay");
			}
		}
		newPacketPlayOutScoreboardObjective = PacketPlayOutScoreboardObjective.getConstructor();
		(PacketPlayOutScoreboardObjective_OBJECTIVENAME = PacketPlayOutScoreboardObjective.getDeclaredField("a")).setAccessible(true);
		(PacketPlayOutScoreboardObjective_DISPLAYNAME = PacketPlayOutScoreboardObjective.getDeclaredField("b")).setAccessible(true);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			(PacketPlayOutScoreboardObjective_RENDERTYPE = PacketPlayOutScoreboardObjective.getDeclaredField("c")).setAccessible(true);
			(PacketPlayOutScoreboardObjective_METHOD = PacketPlayOutScoreboardObjective.getDeclaredField("d")).setAccessible(true);
		} else {
			(PacketPlayOutScoreboardObjective_METHOD = PacketPlayOutScoreboardObjective.getDeclaredField("c")).setAccessible(true);
		}

		//PacketPlayOutScoreboardScore
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
			EnumScoreboardAction = (Class<Enum>) getNMSClass("ScoreboardServer$Action");
			newPacketPlayOutScoreboardScore_1_13 = PacketPlayOutScoreboardScore.getConstructor(EnumScoreboardAction, String.class, String.class, int.class);
		} else {
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
				try {
					//v1_8_R2+
					EnumScoreboardAction = (Class<Enum>) getNMSClass("PacketPlayOutScoreboardScore$EnumScoreboardAction");
				} catch (ClassNotFoundException e) {
					//v1_8_R1
					EnumScoreboardAction = (Class<Enum>) getNMSClass("EnumScoreboardAction");
				}
			}
			newPacketPlayOutScoreboardScore0 = PacketPlayOutScoreboardScore.getConstructor();
			newPacketPlayOutScoreboardScore_String = PacketPlayOutScoreboardScore.getConstructor(String.class);
		}
		(PacketPlayOutScoreboardScore_PLAYER = PacketPlayOutScoreboardScore.getDeclaredField("a")).setAccessible(true);
		(PacketPlayOutScoreboardScore_OBJECTIVENAME = PacketPlayOutScoreboardScore.getDeclaredField("b")).setAccessible(true);
		(PacketPlayOutScoreboardScore_SCORE = PacketPlayOutScoreboardScore.getDeclaredField("c")).setAccessible(true);
		(PacketPlayOutScoreboardScore_ACTION = PacketPlayOutScoreboardScore.getDeclaredField("d")).setAccessible(true);

		//PacketPlayOutScoreboardTeam
		newPacketPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.getConstructor();
		(PacketPlayOutScoreboardTeam_NAME = PacketPlayOutScoreboardTeam.getDeclaredField("a")).setAccessible(true);
		(PacketPlayOutScoreboardTeam_DISPLAYNAME = PacketPlayOutScoreboardTeam.getDeclaredField("b")).setAccessible(true);
		(PacketPlayOutScoreboardTeam_PREFIX = PacketPlayOutScoreboardTeam.getDeclaredField("c")).setAccessible(true);
		(PacketPlayOutScoreboardTeam_SUFFIX = PacketPlayOutScoreboardTeam.getDeclaredField("d")).setAccessible(true);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			//1.9+
			(PacketPlayOutScoreboardTeam_VISIBILITY = PacketPlayOutScoreboardTeam.getDeclaredField("e")).setAccessible(true);
			(PacketPlayOutScoreboardTeam_COLLISION = PacketPlayOutScoreboardTeam.getDeclaredField("f")).setAccessible(true);
			(PacketPlayOutScoreboardTeam_PLAYERS = PacketPlayOutScoreboardTeam.getDeclaredField("h")).setAccessible(true);
			(PacketPlayOutScoreboardTeam_ACTION = PacketPlayOutScoreboardTeam.getDeclaredField("i")).setAccessible(true);
			(PacketPlayOutScoreboardTeam_SIGNATURE = PacketPlayOutScoreboardTeam.getDeclaredField("j")).setAccessible(true);
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
				(PacketPlayOutScoreboardTeam_CHATFORMAT = PacketPlayOutScoreboardTeam.getDeclaredField("g")).setAccessible(true);
			}
		} else {
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
				//1.8.x
				(PacketPlayOutScoreboardTeam_VISIBILITY = PacketPlayOutScoreboardTeam.getDeclaredField("e")).setAccessible(true);
				(PacketPlayOutScoreboardTeam_PLAYERS = PacketPlayOutScoreboardTeam.getDeclaredField("g")).setAccessible(true);
				(PacketPlayOutScoreboardTeam_ACTION = PacketPlayOutScoreboardTeam.getDeclaredField("h")).setAccessible(true);
				(PacketPlayOutScoreboardTeam_SIGNATURE = PacketPlayOutScoreboardTeam.getDeclaredField("i")).setAccessible(true);
			} else {
				//1.5.x - 1.7.x
				(PacketPlayOutScoreboardTeam_PLAYERS = PacketPlayOutScoreboardTeam.getDeclaredField("e")).setAccessible(true);
				(PacketPlayOutScoreboardTeam_ACTION = PacketPlayOutScoreboardTeam.getDeclaredField("f")).setAccessible(true);
				(PacketPlayOutScoreboardTeam_SIGNATURE = PacketPlayOutScoreboardTeam.getDeclaredField("g")).setAccessible(true);
			}
		}

		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			//PacketPlayOutPlayerInfo
			PacketPlayOutPlayerInfo = getNMSClass("PacketPlayOutPlayerInfo");
			try {
				EnumGamemode = (Class<Enum>) getNMSClass("EnumGamemode");
			} catch (ClassNotFoundException e) {
				//v1_8_R2 - v1_9_R2
				EnumGamemode = (Class<Enum>) getNMSClass("WorldSettings$EnumGamemode");
			}
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
				//1.8+
				try {
					//v1_8_R2+
					EnumPlayerInfoAction = (Class<Enum>) getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
					PlayerInfoData = getNMSClass("PacketPlayOutPlayerInfo$PlayerInfoData");
				} catch (ClassNotFoundException e) {
					//v1_8_R1
					EnumPlayerInfoAction = (Class<Enum>) getNMSClass("EnumPlayerInfoAction");
					PlayerInfoData = getNMSClass("PlayerInfoData");
				}
				newPacketPlayOutPlayerInfo2 = PacketPlayOutPlayerInfo.getConstructor(EnumPlayerInfoAction, Iterable.class);
				GameProfile = Class.forName("com.mojang.authlib.GameProfile");
				PropertyMap = Class.forName("com.mojang.authlib.properties.PropertyMap");
				newPlayerInfoData = PlayerInfoData.getConstructor(PacketPlayOutPlayerInfo, GameProfile, int.class, EnumGamemode, NMSHook.IChatBaseComponent);
				(PacketPlayOutPlayerInfo_ACTION = PacketPlayOutPlayerInfo.getDeclaredField("a")).setAccessible(true);
				(PacketPlayOutPlayerInfo_PLAYERS = PacketPlayOutPlayerInfo.getDeclaredField("b")).setAccessible(true);
				(PlayerInfoData_PING = PlayerInfoData.getDeclaredField("b")).setAccessible(true);
				(PlayerInfoData_GAMEMODE = PlayerInfoData.getDeclaredField("c")).setAccessible(true);
				(PlayerInfoData_PROFILE = PlayerInfoData.getDeclaredField("d")).setAccessible(true);
				(PlayerInfoData_LISTNAME = PlayerInfoData.getDeclaredField("e")).setAccessible(true);
			} else {
				//1.7
				newPacketPlayOutPlayerInfo0 = PacketPlayOutPlayerInfo.getConstructor();
				GameProfile = Class.forName("net.minecraft.util.com.mojang.authlib.GameProfile");
				PropertyMap = Class.forName("net.minecraft.util.com.mojang.authlib.properties.PropertyMap");
				(PacketPlayOutPlayerInfo_ACTION = PacketPlayOutPlayerInfo.getDeclaredField("action")).setAccessible(true);
				(PlayerInfoData_PING = PacketPlayOutPlayerInfo.getDeclaredField("ping")).setAccessible(true);
				(PlayerInfoData_GAMEMODE = PacketPlayOutPlayerInfo.getDeclaredField("gamemode")).setAccessible(true);
				(PlayerInfoData_PROFILE = PacketPlayOutPlayerInfo.getDeclaredField("player")).setAccessible(true);
				(PlayerInfoData_LISTNAME = PacketPlayOutPlayerInfo.getDeclaredField("username")).setAccessible(true);
			}
			newGameProfile = GameProfile.getConstructor(UUID.class, String.class);
			(GameProfile_ID = GameProfile.getDeclaredField("id")).setAccessible(true);
			(GameProfile_NAME = GameProfile.getDeclaredField("name")).setAccessible(true);
			(GameProfile_PROPERTIES = GameProfile.getDeclaredField("properties")).setAccessible(true);
			for (Method m : PropertyMap.getMethods()) {
				if (m.getName().equals("putAll") && m.getParameterCount() == 1) PropertyMap_putAll = m;
			}
			if (PropertyMap_putAll == null) throw new IllegalStateException("putAll method not found");
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			//PacketPlayOutPlayerListHeaderFooter
			PacketPlayOutPlayerListHeaderFooter = getNMSClass("PacketPlayOutPlayerListHeaderFooter");
			newPacketPlayOutPlayerListHeaderFooter = PacketPlayOutPlayerListHeaderFooter.getConstructor();
			List<Field> fields = getFields(PacketPlayOutPlayerListHeaderFooter, NMSHook.IChatBaseComponent);
			PacketPlayOutPlayerListHeaderFooter_HEADER = fields.get(0);
			PacketPlayOutPlayerListHeaderFooter_FOOTER = fields.get(1);
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			//PacketPlayOutBoss
			PacketPlayOutBoss = getNMSClass("PacketPlayOutBoss");
			BarColor = getNMSClass("BossBattle$BarColor");
			BarStyle = getNMSClass("BossBattle$BarStyle");
			PacketPlayOutBoss_Action = (Class<Enum>) getNMSClass("PacketPlayOutBoss$Action");
			newPacketPlayOutBoss = PacketPlayOutBoss.getConstructor();
			(PacketPlayOutBoss_UUID = PacketPlayOutBoss.getDeclaredField("a")).setAccessible(true);
			(PacketPlayOutBoss_ACTION = PacketPlayOutBoss.getDeclaredField("b")).setAccessible(true);
			(PacketPlayOutBoss_NAME = PacketPlayOutBoss.getDeclaredField("c")).setAccessible(true);
			(PacketPlayOutBoss_PROGRESS = PacketPlayOutBoss.getDeclaredField("d")).setAccessible(true);
			(PacketPlayOutBoss_COLOR = PacketPlayOutBoss.getDeclaredField("e")).setAccessible(true);
			(PacketPlayOutBoss_STYLE = PacketPlayOutBoss.getDeclaredField("f")).setAccessible(true);
			(PacketPlayOutBoss_DARKEN_SKY = PacketPlayOutBoss.getDeclaredField("g")).setAccessible(true);
			(PacketPlayOutBoss_PLAY_MUSIC = PacketPlayOutBoss.getDeclaredField("h")).setAccessible(true);
			(PacketPlayOutBoss_CREATE_FOG = PacketPlayOutBoss.getDeclaredField("i")).setAccessible(true);
		}
	}

	@Override
	public Object build(PacketPlayOutBoss packet, ProtocolVersion clientVersion) throws Exception {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			//1.9+ server
			Object nmsPacket = newPacketPlayOutBoss.newInstance();
			PacketPlayOutBoss_UUID.set(nmsPacket, packet.id);
			PacketPlayOutBoss_ACTION.set(nmsPacket, Enum.valueOf((Class<Enum>)PacketPlayOutBoss_Action, packet.operation.toString()));
			if (packet.operation == Action.UPDATE_PCT || packet.operation == Action.ADD) {
				PacketPlayOutBoss_PROGRESS.set(nmsPacket, packet.pct);
			}
			if (packet.operation == Action.UPDATE_NAME || packet.operation == Action.ADD) {
				PacketPlayOutBoss_NAME.set(nmsPacket, NMSHook.stringToComponent(IChatBaseComponent.optimizedComponent(packet.name).toString(clientVersion)));
			}
			if (packet.operation == Action.UPDATE_STYLE || packet.operation == Action.ADD) {
				PacketPlayOutBoss_COLOR.set(nmsPacket, Enum.valueOf((Class<Enum>)BarColor, packet.color.toString()));
				PacketPlayOutBoss_STYLE.set(nmsPacket, Enum.valueOf((Class<Enum>)BarStyle, packet.overlay.toString()));
			}
			if (packet.operation == Action.UPDATE_PROPERTIES || packet.operation == Action.ADD) {
				PacketPlayOutBoss_DARKEN_SKY.set(nmsPacket, packet.darkenScreen);
				PacketPlayOutBoss_PLAY_MUSIC.set(nmsPacket, packet.playMusic);
				PacketPlayOutBoss_CREATE_FOG.set(nmsPacket, packet.createWorldFog);
			}
			return nmsPacket;
		}
		if (clientVersion.getMinorVersion() >= 9 && Bukkit.getPluginManager().isPluginEnabled("ViaVersion")) {
			//1.9+ client on 1.8 server
			//technically redundant VV check as there is no other way to get 1.9 client on 1.8 server
			ByteBuf buf = Unpooled.buffer();
			Type.VAR_INT.writePrimitive(buf, 0x0C);
			Type.UUID.write(buf, packet.id);
			Type.VAR_INT.writePrimitive(buf, packet.operation.ordinal());
			switch (packet.operation) {
			case ADD:
				Type.COMPONENT.write(buf, JsonParser.parseString(IChatBaseComponent.optimizedComponent(packet.name).toString(clientVersion)));
				Type.FLOAT.writePrimitive(buf, packet.pct);
				Type.VAR_INT.writePrimitive(buf, packet.color.ordinal());
				Type.VAR_INT.writePrimitive(buf, packet.overlay.ordinal());
				Type.BYTE.write(buf, packet.getFlags());
				break;
			case REMOVE:
				break;
			case UPDATE_PCT:
				Type.FLOAT.writePrimitive(buf, packet.pct);
				break;
			case UPDATE_NAME:
				Type.COMPONENT.write(buf, JsonParser.parseString(IChatBaseComponent.optimizedComponent(packet.name).toString(clientVersion)));
				break;
			case UPDATE_STYLE:
				Type.VAR_INT.writePrimitive(buf, packet.color.ordinal());
				Type.VAR_INT.writePrimitive(buf, packet.overlay.ordinal());
				break;
			case UPDATE_PROPERTIES:
				Type.BYTE.write(buf, packet.getFlags());
				break;
			}
			return buf;
		}
		
		//<1.9 client and server
		if (packet.operation == Action.UPDATE_STYLE) return null; //nothing to do here
		
		int entityId = ((BossBar)Shared.featureManager.getFeature("bossbar")).getLine(packet.id).entityId;
		if (packet.operation == Action.REMOVE) {
			return new PacketPlayOutEntityDestroy(entityId).toNMS(clientVersion);
		}
		DataWatcher w = new DataWatcher();
		if (packet.operation == Action.UPDATE_PCT || packet.operation == Action.ADD) {
			float health = (float)300*packet.pct;
			if (health == 0) health = 1;
			w.helper().setHealth(health);
		}
		if (packet.operation == Action.UPDATE_NAME || packet.operation == Action.ADD) {
			w.helper().setCustomName(packet.name, clientVersion);
		}
		if (packet.operation == Action.UPDATE_STYLE || packet.operation == Action.ADD) {
			//shrug
		}
		if (packet.operation == Action.ADD) {
			w.helper().setEntityFlags((byte) 32);
			PacketPlayOutSpawnEntityLiving spawn = new PacketPlayOutSpawnEntityLiving(entityId, null, EntityType.WITHER, new Location(null, 0,0,0));
			spawn.setDataWatcher(w);
			return spawn.toNMS(clientVersion);
		} else {
			return new PacketPlayOutEntityMetadata(entityId, w).toNMS(clientVersion);
		}
	}

	@Override
	public Object build(PacketPlayOutChat packet, ProtocolVersion clientVersion) throws Exception {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 16) {
			return newPacketPlayOutChat.newInstance(NMSHook.stringToComponent(packet.message.toString(clientVersion)), Enum.valueOf((Class<Enum>)ChatMessageType, packet.type.toString()), UUID.randomUUID());
		} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 12) {
			return newPacketPlayOutChat.newInstance(NMSHook.stringToComponent(packet.message.toString(clientVersion)), Enum.valueOf((Class<Enum>)ChatMessageType, packet.type.toString()));
		} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			return newPacketPlayOutChat.newInstance(NMSHook.stringToComponent(packet.message.toString(clientVersion)), (byte)packet.type.ordinal());
		} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 6) {
			try {
				//v1_7_R4
				return newPacketPlayOutChat.newInstance(NMSHook.stringToComponent(packet.message.toString(clientVersion)), packet.type.ordinal());
			} catch (Exception e) {
				return newPacketPlayOutChat.newInstance(NMSHook.stringToComponent(packet.message.toString(clientVersion)), true);
			}
		} else {
			return newPacketPlayOutChat.newInstance(packet.message.toColoredText(), true);
		}
	}

	@Override
	public Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) throws Exception {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			Object nmsPacket = newPacketPlayOutPlayerInfo2.newInstance(Enum.valueOf(EnumPlayerInfoAction, packet.action.toString()), Collections.EMPTY_LIST);
			List<Object> items = new ArrayList<Object>();
			for (PlayerInfoData data : packet.entries) {
				Object profile = newGameProfile.newInstance(data.uniqueId, data.name);
				if (data.skin != null) PropertyMap_putAll.invoke(GameProfile_PROPERTIES.get(profile), data.skin);
				items.add(newPlayerInfoData.newInstance(newPacketPlayOutPlayerInfo2.newInstance(null, Collections.EMPTY_LIST), profile, data.latency, data.gameMode == null ? null : Enum.valueOf(EnumGamemode, data.gameMode.toString()), 
						data.displayName == null ? null : NMSHook.stringToComponent(data.displayName.toString(clientVersion))));
			}
			PacketPlayOutPlayerInfo_PLAYERS.set(nmsPacket, items);
			return nmsPacket;
		} else {
			Object nmsPacket = newPacketPlayOutPlayerInfo0.newInstance();
			PacketPlayOutPlayerInfo_ACTION.set(nmsPacket, packet.action.ordinal());
			PlayerInfoData data = packet.entries.get(0);
			Object profile = newGameProfile.newInstance(data.uniqueId, data.name);
			if (data.skin != null) PropertyMap_putAll.invoke(GameProfile_PROPERTIES.get(profile), data.skin);
			PlayerInfoData_PROFILE.set(nmsPacket, profile);
			PlayerInfoData_GAMEMODE.set(nmsPacket, data.gameMode.ordinal()-1);
			PlayerInfoData_PING.set(nmsPacket, data.latency);
			PlayerInfoData_LISTNAME.set(nmsPacket, cutTo(data.displayName.toColoredText(), 16));
			return nmsPacket;
		}
	}

	@Override
	public Object build(PacketPlayOutPlayerListHeaderFooter packet, ProtocolVersion clientVersion) throws Exception {
		Object nmsPacket = newPacketPlayOutPlayerListHeaderFooter.newInstance();
		PacketPlayOutPlayerListHeaderFooter_HEADER.set(nmsPacket, NMSHook.stringToComponent(packet.header.toString(clientVersion, true)));
		PacketPlayOutPlayerListHeaderFooter_FOOTER.set(nmsPacket, NMSHook.stringToComponent(packet.footer.toString(clientVersion, true)));
		return nmsPacket;
	}

	@Override
	public Object build(PacketPlayOutScoreboardDisplayObjective packet, ProtocolVersion clientVersion) throws Exception {
		Object nmsPacket = newPacketPlayOutScoreboardDisplayObjective.newInstance();
		PacketPlayOutScoreboardDisplayObjective_POSITION.set(nmsPacket, packet.slot);
		PacketPlayOutScoreboardDisplayObjective_OBJECTIVENAME.set(nmsPacket, packet.objectiveName);
		return nmsPacket;
	}

	@Override
	public Object build(PacketPlayOutScoreboardObjective packet, ProtocolVersion clientVersion) throws Exception {
		String displayName = packet.displayName;
		if (clientVersion.getMinorVersion() < 13) {
			displayName = cutTo(displayName, 32);
		}
		Object nmsPacket = newPacketPlayOutScoreboardObjective.newInstance();
		PacketPlayOutScoreboardObjective_OBJECTIVENAME.set(nmsPacket, packet.objectiveName);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
			PacketPlayOutScoreboardObjective_DISPLAYNAME.set(nmsPacket, NMSHook.stringToComponent(IChatBaseComponent.optimizedComponent(displayName).toString(clientVersion)));
		} else {
			PacketPlayOutScoreboardObjective_DISPLAYNAME.set(nmsPacket, displayName);
		}
		if (PacketPlayOutScoreboardObjective_RENDERTYPE != null && packet.renderType != null) {
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
				PacketPlayOutScoreboardObjective_RENDERTYPE.set(nmsPacket, Enum.valueOf(EnumScoreboardHealthDisplay, packet.renderType.toString()));
			} else {
				PacketPlayOutScoreboardObjective_RENDERTYPE.set(nmsPacket, packet.renderType.ordinal());
			}
		}
		PacketPlayOutScoreboardObjective_METHOD.set(nmsPacket, packet.method);
		return nmsPacket;
	}

	@Override
	public Object build(PacketPlayOutScoreboardScore packet, ProtocolVersion clientVersion) throws Exception {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
			return newPacketPlayOutScoreboardScore_1_13.newInstance(Enum.valueOf(EnumScoreboardAction, packet.action.toString()), packet.objectiveName, packet.player, packet.score);
		}
		if (packet.action == me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore.Action.REMOVE) {
			return newPacketPlayOutScoreboardScore_String.newInstance(packet.player);
		}
		Object nmsPacket = newPacketPlayOutScoreboardScore0.newInstance();
		PacketPlayOutScoreboardScore_PLAYER.set(nmsPacket, packet.player);
		PacketPlayOutScoreboardScore_OBJECTIVENAME.set(nmsPacket, packet.objectiveName);
		PacketPlayOutScoreboardScore_SCORE.set(nmsPacket, packet.score);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			PacketPlayOutScoreboardScore_ACTION.set(nmsPacket, Enum.valueOf(EnumScoreboardAction, packet.action.toString()));
		} else {
			PacketPlayOutScoreboardScore_ACTION.set(nmsPacket, packet.action.ordinal());
		}
		return nmsPacket;
	}

	@Override
	public Object build(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion) throws Exception {
		if (packet.name == null || packet.name.length() == 0) throw new IllegalArgumentException("Team name cannot be null/empty");
		String prefix = packet.playerPrefix;
		String suffix = packet.playerSuffix;
		if (clientVersion.getMinorVersion() < 13) {
			prefix = cutTo(prefix, 16);
			suffix = cutTo(suffix, 16);
		}
		Object nmsPacket = newPacketPlayOutScoreboardTeam.newInstance();
		PacketPlayOutScoreboardTeam_NAME.set(nmsPacket, packet.name);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
			PacketPlayOutScoreboardTeam_DISPLAYNAME.set(nmsPacket, NMSHook.stringToComponent(IChatBaseComponent.optimizedComponent(packet.name).toString(clientVersion)));
			if (prefix != null && prefix.length() > 0) PacketPlayOutScoreboardTeam_PREFIX.set(nmsPacket, NMSHook.stringToComponent(IChatBaseComponent.optimizedComponent(prefix).toString(clientVersion)));
			if (suffix != null && suffix.length() > 0) PacketPlayOutScoreboardTeam_SUFFIX.set(nmsPacket, NMSHook.stringToComponent(IChatBaseComponent.optimizedComponent(suffix).toString(clientVersion)));
			EnumChatFormat format = packet.color != null ? packet.color : EnumChatFormat.lastColorsOf(prefix);
			PacketPlayOutScoreboardTeam_CHATFORMAT.set(nmsPacket, Enum.valueOf((Class<Enum>)EnumChatFormat_, format.toString()));
		} else {
			PacketPlayOutScoreboardTeam_DISPLAYNAME.set(nmsPacket, packet.name);
			PacketPlayOutScoreboardTeam_PREFIX.set(nmsPacket, prefix);
			PacketPlayOutScoreboardTeam_SUFFIX.set(nmsPacket, suffix);
		}
		if (PacketPlayOutScoreboardTeam_COLLISION != null) PacketPlayOutScoreboardTeam_COLLISION.set(nmsPacket, packet.collisionRule);
		PacketPlayOutScoreboardTeam_PLAYERS.set(nmsPacket, packet.players);
		PacketPlayOutScoreboardTeam_ACTION.set(nmsPacket, packet.method);
		PacketPlayOutScoreboardTeam_SIGNATURE.set(nmsPacket, packet.options);
		if (PacketPlayOutScoreboardTeam_VISIBILITY != null) PacketPlayOutScoreboardTeam_VISIBILITY.set(nmsPacket, packet.nametagVisibility);
		return nmsPacket;
	}

	private static Class<?> getNMSClass(String name) throws ClassNotFoundException {
		return Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + "." + name);
	}

	private static List<Field> getFields(Class<?> clazz, Class<?> type){
		List<Field> list = new ArrayList<Field>();
		if (clazz == null) return list;
		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			if (field.getType() == type) list.add(field);
		}
		return list;
	}

	public static PacketPlayOutPlayerInfo readPlayerInfo(Object nmsPacket) throws Exception{
		if (!PacketPlayOutPlayerInfo.isInstance(nmsPacket)) return null;
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			EnumPlayerInfoAction action = me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction.valueOf(PacketPlayOutPlayerInfo_ACTION.get(nmsPacket).toString());
			List<PlayerInfoData> listData = new ArrayList<PlayerInfoData>();
			for (Object nmsData : (List) PacketPlayOutPlayerInfo_PLAYERS.get(nmsPacket)) {
				int ping = PlayerInfoData_PING.getInt(nmsData);
				Object nmsGamemode = PlayerInfoData_GAMEMODE.get(nmsData);
				EnumGamemode gamemode = nmsGamemode == null ? null : me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumGamemode.valueOf(nmsGamemode.toString());
				Object profile = PlayerInfoData_PROFILE.get(nmsData);
				Object nmsComponent = PlayerInfoData_LISTNAME.get(nmsData);
				IChatBaseComponent listName = IChatBaseComponent.fromString(NMSHook.componentToString(nmsComponent));
				listData.add(new PlayerInfoData((String) GameProfile_NAME.get(profile), (UUID) GameProfile_ID.get(profile), GameProfile_PROPERTIES.get(profile), ping, gamemode, listName));
			}
			return new PacketPlayOutPlayerInfo(action, listData);
		} else {

			EnumPlayerInfoAction action = me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction.values()[(PacketPlayOutPlayerInfo_ACTION.getInt(nmsPacket))];
			int ping = PlayerInfoData_PING.getInt(nmsPacket);
			EnumGamemode gamemode = me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumGamemode.values()[PlayerInfoData_GAMEMODE.getInt(nmsPacket)+1];
			Object profile = PlayerInfoData_PROFILE.get(nmsPacket);
			IChatBaseComponent listName = IChatBaseComponent.fromColoredText((String) PlayerInfoData_LISTNAME.get(nmsPacket));
			PlayerInfoData data = new PlayerInfoData((String) GameProfile_NAME.get(profile), (UUID) GameProfile_ID.get(profile), GameProfile_PROPERTIES.get(profile), ping, gamemode, listName);
			return new PacketPlayOutPlayerInfo(action, Lists.newArrayList(data));
		}
	}
}
