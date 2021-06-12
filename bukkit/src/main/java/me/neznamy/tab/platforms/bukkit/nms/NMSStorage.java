package me.neznamy.tab.platforms.bukkit.nms;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;

import com.mojang.authlib.GameProfile;

import io.netty.channel.Channel;
import me.neznamy.tab.platforms.bukkit.Main;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherRegistry;

@SuppressWarnings({"unchecked", "rawtypes"})
public class NMSStorage {

	//instance of this class
	private static NMSStorage instance;

	//data watcher registry
	public DataWatcherRegistry dataWatcherRegistry;

	//server package, such as "v1_16_R3"
	private String serverPackage;

	//server minor version such as "16"
	public int minorVersion;

	public Class<?> Entity;
	public Class<?> EntityLiving;
	private Class<?> Packet;
	private Class<?> PlayerConnection;
	private Class<?> NetworkManager;
	public Class<?> EntityPlayer;
	public Field PING;
	public Field PLAYER_CONNECTION;
	public Field NETWORK_MANAGER;
	public Field CHANNEL;
	public Method getHandle;
	public Method getProfile;
	public Method sendPacket;

	//chat
	public Class<Enum> EnumChatFormat;
	private Class<?> IChatBaseComponent;
	private Class<?> ChatBaseComponent;
	private Class<?> ChatSerializer;
	private Class<?> EnumClickAction;
	private Class<?> EnumHoverAction;
	private Class<?> ChatModifier;
	public Class<?> ChatComponentText;
	private Class<?> ChatClickable;
	private Class<?> ChatHoverable;
	private Class<?> ChatHexColor;
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
	private Class<?> PacketPlayOutBoss;
	private Class<?> BossBattle;
	private Class<?> BossBattleServer;
	public Constructor<?> newBossBattleServer;
	public Class<Enum> BarColor;
	public Class<Enum> BarStyle;
	public Class<Enum> PacketPlayOutBoss_Action;
	public Constructor<?> newPacketPlayOutBoss;
	public Field BossBattle_UUID;
	public Method BossBattleServer_setProgress;
	public Method BossBattleServer_setCreateFog;
	public Method BossBattleServer_setDarkenSky;
	public Method BossBattleServer_setPlayMusic;
	//1.17
	public Method PacketPlayOutBoss_createAddPacket;
	public Method PacketPlayOutBoss_createRemovePacket;
	public Method PacketPlayOutBoss_createUpdateProgressPacket;
	public Method PacketPlayOutBoss_createUpdateNamePacket;
	public Method PacketPlayOutBoss_createUpdateStylePacket;
	public Method PacketPlayOutBoss_createUpdatePropertiesPacket;

	//PacketPlayOutChat
	private Class<?> PacketPlayOutChat;
	public Class<Enum> ChatMessageType;
	public Constructor<?> newPacketPlayOutChat;

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
	public Class<?> IScoreboardCriteria;
	private Class<?> ScoreboardObjective;
	public Class<Enum> EnumScoreboardHealthDisplay;
	public Constructor<?> newPacketPlayOutScoreboardObjective;
	public Constructor<?> newScoreboardObjective;
	public Field PacketPlayOutScoreboardObjective_OBJECTIVENAME;
	public Field PacketPlayOutScoreboardObjective_DISPLAYNAME;
	public Field PacketPlayOutScoreboardObjective_RENDERTYPE;
	public Field PacketPlayOutScoreboardObjective_METHOD;

	//PacketPlayOutScoreboardScore
	private Class<?> PacketPlayOutScoreboardScore;
	private Class<?> ScoreboardScore;
	public Class<Enum> EnumScoreboardAction;
	public Constructor<?> newPacketPlayOutScoreboardScore;
	public Constructor<?> newPacketPlayOutScoreboardScore_String;
	public Constructor<?> newPacketPlayOutScoreboardScore_1_13;
	public Constructor<?> newScoreboardScore;
	public Method ScoreboardScore_setScore;

	//PacketPlayOutScoreboardTeam
	public Class<?> PacketPlayOutScoreboardTeam;
	public Class<?> EnumNameTagVisibility;
	public Class<?> EnumTeamPush;
	public Constructor<?> newPacketPlayOutScoreboardTeam;
	public Field PacketPlayOutScoreboardTeam_NAME;
	public Field PacketPlayOutScoreboardTeam_PLAYERS;
	private Class<?> Scoreboard;
	private Class<?> ScoreboardTeam;
	public Constructor<?> newScoreboard;
	public Constructor<?> newScoreboardTeam;
	public Method ScoreboardTeam_setPrefix;
	public Method ScoreboardTeam_setSuffix;
	public Method ScoreboardTeam_setNameTagVisibility;
	public Method ScoreboardTeam_setCollisionRule;
	public Method ScoreboardTeam_setColor;
	public Method ScoreboardTeam_getPlayerNameSet;
	//1.17
	public Class<?> PacketPlayOutScoreboardTeam_a;
	public Method PacketPlayOutScoreboardTeam_of;
	public Method PacketPlayOutScoreboardTeam_ofBoolean;
	public Method PacketPlayOutScoreboardTeam_ofString;


	//PacketPlayOutPlayerInfo
	public Class<?> PacketPlayOutPlayerInfo;
	public Class<Enum> EnumGamemode;
	public Class<Enum> EnumPlayerInfoAction;
	private Class<?> PlayerInfoData;
	public Constructor<?> newPacketPlayOutPlayerInfo;
	public Constructor<?> newPlayerInfoData;

	public Field PacketPlayOutPlayerInfo_ACTION;
	public Field PacketPlayOutPlayerInfo_PLAYERS;

	public Field PlayerInfoData_PING;
	public Field PlayerInfoData_GAMEMODE;
	public Field PlayerInfoData_PROFILE;
	public Field PlayerInfoData_LISTNAME;

	//PacketPlayOutTitle
	private Class<?> PacketPlayOutTitle;
	public Constructor<?> newPacketPlayOutTitle;
	public Class<Enum> EnumTitleAction;
	//1.17
	private Class<?> ClientboundSetTitleTextPacket;
	private Class<?> ClientboundSetSubtitleTextPacket;
	private Class<?> ClientboundSetActionBarTextPacket;
	private Class<?> ClientboundSetTitlesAnimationPacket;
	private Class<?> ClientboundClearTitlesPacket;
	public Constructor<?> newClientboundSetTitleTextPacket;
	public Constructor<?> newClientboundSetSubtitleTextPacket;
	public Constructor<?> newClientboundSetActionBarTextPacket;
	public Constructor<?> newClientboundSetTitlesAnimationPacket;
	public Constructor<?> newClientboundClearTitlesPacket;

	public Class<?> PacketPlayOutEntityDestroy;
	public Constructor<?> newPacketPlayOutEntityDestroy;
	public Field PacketPlayOutEntityDestroy_ENTITIES;

	public Class<?> PacketPlayInUseEntity;
	private Class<?> EnumEntityUseAction;
	public Field PacketPlayInUseEntity_ENTITY;
	public Field PacketPlayInUseEntity_ACTION;

	public Class<?> PacketPlayOutNamedEntitySpawn;
	public Field PacketPlayOutNamedEntitySpawn_ENTITYID;

	public Class<?> PacketPlayOutEntity;
	public Class<?> PacketPlayOutEntityLook;
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
	private Class<?> DataWatcher;
	public Constructor<?> newDataWatcher;
	public Method DataWatcher_REGISTER;

	private Class<?> DataWatcherItem;
	public Field DataWatcherItem_TYPE;
	public Field DataWatcherItem_VALUE;

	private Class<?> DataWatcherObject;
	public Constructor<?> newDataWatcherObject;
	public Field DataWatcherObject_SLOT;
	public Field DataWatcherObject_SERIALIZER;

	private Class<?> DataWatcherRegistry;
	private Class<?> DataWatcherSerializer;

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
		dataWatcherRegistry = new DataWatcherRegistry(this, DataWatcherRegistry);
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
	 * Initializes required NMS classes
	 * @throws Exception - if something fails
	 */
	private void initializeClasses() throws Exception {
		DataWatcher = getNMSClass("net.minecraft.network.syncher.DataWatcher", "DataWatcher");
		DataWatcherItem = getNMSClass("net.minecraft.network.syncher.DataWatcher$Item", "DataWatcher$Item", "DataWatcher$WatchableObject", "WatchableObject");
		EntityPlayer = getNMSClass("net.minecraft.server.level.EntityPlayer", "EntityPlayer");
		EnumChatFormat = (Class<Enum>) getNMSClass("net.minecraft.EnumChatFormat", "EnumChatFormat");
		Entity = getNMSClass("net.minecraft.world.entity.Entity", "Entity");
		EntityLiving = getNMSClass("net.minecraft.world.entity.EntityLiving", "EntityLiving");
		Packet = getNMSClass("net.minecraft.network.protocol.Packet", "Packet");
		PacketPlayInUseEntity = getNMSClass("net.minecraft.network.protocol.game.PacketPlayInUseEntity", "PacketPlayInUseEntity", "Packet7UseEntity");
		PacketPlayOutChat = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutChat", "PacketPlayOutChat", "Packet3Chat");
		PacketPlayOutEntity = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutEntity", "PacketPlayOutEntity", "Packet30Entity");
		PacketPlayOutEntityDestroy = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy", "PacketPlayOutEntityDestroy", "Packet29DestroyEntity");
		PacketPlayOutEntityLook = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutEntity$PacketPlayOutEntityLook", "PacketPlayOutEntity$PacketPlayOutEntityLook", "PacketPlayOutEntityLook", "Packet32EntityLook");
		PacketPlayOutEntityMetadata = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata", "PacketPlayOutEntityMetadata", "Packet40EntityMetadata");
		PacketPlayOutEntityTeleport = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport", "PacketPlayOutEntityTeleport", "Packet34EntityTeleport");
		PacketPlayOutNamedEntitySpawn = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn", "PacketPlayOutNamedEntitySpawn", "Packet20NamedEntitySpawn");
		PacketPlayOutScoreboardDisplayObjective = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutScoreboardDisplayObjective", "PacketPlayOutScoreboardDisplayObjective", "Packet208SetScoreboardDisplayObjective");
		PacketPlayOutScoreboardObjective = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutScoreboardObjective", "PacketPlayOutScoreboardObjective", "Packet206SetScoreboardObjective");
		PacketPlayOutScoreboardScore = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutScoreboardScore", "PacketPlayOutScoreboardScore", "Packet207SetScoreboardScore");
		PacketPlayOutScoreboardTeam = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam", "PacketPlayOutScoreboardTeam", "Packet209SetScoreboardTeam");
		PacketPlayOutSpawnEntityLiving = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutSpawnEntityLiving", "PacketPlayOutSpawnEntityLiving", "Packet24MobSpawn");
		PlayerConnection = getNMSClass("net.minecraft.server.network.PlayerConnection", "PlayerConnection");
		NetworkManager = getNMSClass("net.minecraft.network.NetworkManager", "NetworkManager");
		Scoreboard = getNMSClass("net.minecraft.world.scores.Scoreboard", "Scoreboard");
		ScoreboardObjective = getNMSClass("net.minecraft.world.scores.ScoreboardObjective", "ScoreboardObjective");
		ScoreboardTeam = getNMSClass("net.minecraft.world.scores.ScoreboardTeam", "ScoreboardTeam");
		ScoreboardScore = getNMSClass("net.minecraft.world.scores.ScoreboardScore", "ScoreboardScore");
		IScoreboardCriteria = getNMSClass("net.minecraft.world.scores.criteria.IScoreboardCriteria", "IScoreboardCriteria");

		if (minorVersion >= 7) {
			ChatBaseComponent = getNMSClass("net.minecraft.network.chat.ChatBaseComponent", "ChatBaseComponent");
			ChatClickable = getNMSClass("net.minecraft.network.chat.ChatClickable", "ChatClickable");
			ChatComponentText = getNMSClass("net.minecraft.network.chat.ChatComponentText", "ChatComponentText");
			ChatHoverable = getNMSClass("net.minecraft.network.chat.ChatHoverable", "ChatHoverable");
			ChatModifier = getNMSClass("net.minecraft.network.chat.ChatModifier", "ChatModifier");
			ChatSerializer = getNMSClass("net.minecraft.network.chat.IChatBaseComponent$ChatSerializer", "IChatBaseComponent$ChatSerializer", "ChatSerializer");
			EnumClickAction = getNMSClass("net.minecraft.network.chat.ChatClickable$EnumClickAction", "ChatClickable$EnumClickAction", "EnumClickAction");
			EnumHoverAction = getNMSClass("net.minecraft.network.chat.ChatHoverable$EnumHoverAction", "ChatHoverable$EnumHoverAction", "EnumHoverAction");
			IChatBaseComponent = getNMSClass("net.minecraft.network.chat.IChatBaseComponent", "IChatBaseComponent");
			EnumEntityUseAction = getNMSClass("net.minecraft.network.protocol.game.PacketPlayInUseEntity$EnumEntityUseAction", "PacketPlayInUseEntity$EnumEntityUseAction", "EnumEntityUseAction");
		}

		if (minorVersion >= 8) {
			PacketPlayOutPlayerInfo = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo", "PacketPlayOutPlayerInfo");
			PacketPlayOutPlayerListHeaderFooter = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutPlayerListHeaderFooter", "PacketPlayOutPlayerListHeaderFooter");
			EnumPlayerInfoAction = (Class<Enum>) getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo$EnumPlayerInfoAction", "PacketPlayOutPlayerInfo$EnumPlayerInfoAction", "EnumPlayerInfoAction");
			PlayerInfoData = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo$PlayerInfoData", "PacketPlayOutPlayerInfo$PlayerInfoData", "PlayerInfoData");
			EnumScoreboardHealthDisplay = (Class<Enum>) getNMSClass("net.minecraft.world.scores.criteria.IScoreboardCriteria$EnumScoreboardHealthDisplay", "IScoreboardCriteria$EnumScoreboardHealthDisplay", "EnumScoreboardHealthDisplay");
			EnumGamemode = (Class<Enum>) getNMSClass("net.minecraft.world.level.EnumGamemode", "EnumGamemode", "WorldSettings$EnumGamemode");
			EnumScoreboardAction = (Class<Enum>) getNMSClass("net.minecraft.server.ScoreboardServer$Action", "ScoreboardServer$Action", "PacketPlayOutScoreboardScore$EnumScoreboardAction", "EnumScoreboardAction");
			EnumNameTagVisibility = getNMSClass("net.minecraft.world.scores.ScoreboardTeamBase$EnumNameTagVisibility", "ScoreboardTeamBase$EnumNameTagVisibility", "EnumNameTagVisibility");
		}
		if (minorVersion >= 9) {
			BossBattle = getNMSClass("net.minecraft.world.BossBattle", "BossBattle");
			BossBattleServer = getNMSClass("net.minecraft.server.level.BossBattleServer", "BossBattleServer");
			BarColor = (Class<Enum>) getNMSClass("net.minecraft.world.BossBattle$BarColor", "BossBattle$BarColor");
			BarStyle = (Class<Enum>) getNMSClass("net.minecraft.world.BossBattle$BarStyle", "BossBattle$BarStyle");
			DataWatcherObject = getNMSClass("net.minecraft.network.syncher.DataWatcherObject", "DataWatcherObject");
			DataWatcherRegistry = getNMSClass("net.minecraft.network.syncher.DataWatcherRegistry", "DataWatcherRegistry");
			DataWatcherSerializer = getNMSClass("net.minecraft.network.syncher.DataWatcherSerializer", "DataWatcherSerializer");
			PacketPlayOutBoss = getNMSClass("net.minecraft.network.protocol.game.PacketPlayOutBoss", "PacketPlayOutBoss");
			EnumTeamPush = getNMSClass("net.minecraft.world.scores.ScoreboardTeamBase$EnumTeamPush", "ScoreboardTeamBase$EnumTeamPush");
			
		}
		if (minorVersion >= 12) {
			ChatMessageType = (Class<Enum>) getNMSClass("net.minecraft.network.chat.ChatMessageType", "ChatMessageType");
		}
		if (minorVersion >= 16) {
			ChatHexColor = getNMSClass("net.minecraft.network.chat.ChatHexColor", "ChatHexColor");
		}
		if (minorVersion >= 17) {
			ClientboundClearTitlesPacket = Class.forName("net.minecraft.network.protocol.game.ClientboundClearTitlesPacket");
			ClientboundSetActionBarTextPacket = Class.forName("net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket");
			ClientboundSetSubtitleTextPacket = Class.forName("net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket");
			ClientboundSetTitleTextPacket = Class.forName("net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket");
			ClientboundSetTitlesAnimationPacket = Class.forName("net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket");
			PacketPlayOutScoreboardTeam_a = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam$a");
		} else {
			if (minorVersion >= 8) {
				PacketPlayOutTitle = getNMSClass("PacketPlayOutTitle");
				EnumTitleAction = (Class<Enum>) getNMSClass("PacketPlayOutTitle$EnumTitleAction", "EnumTitleAction");
			}
			if (minorVersion >= 9) {
				PacketPlayOutBoss_Action = (Class<Enum>) getNMSClass("PacketPlayOutBoss$Action");
			}
		}
	}

	/**
	 * Initializes required NMS constructors
	 * @throws Exception - if something fails
	 */
	private void initializeConstructors() throws Exception {
		newDataWatcher = DataWatcher.getConstructors()[0];
		newPacketPlayOutEntityMetadata = PacketPlayOutEntityMetadata.getConstructor(int.class, DataWatcher, boolean.class);
		newScoreboardObjective = ScoreboardObjective.getConstructors()[0];
		newScoreboard = Scoreboard.getConstructor();
		newScoreboardTeam = ScoreboardTeam.getConstructor(Scoreboard, String.class);
		newScoreboardScore = ScoreboardScore.getConstructor(Scoreboard, ScoreboardObjective, String.class);
		newPacketPlayOutEntityTeleport = PacketPlayOutEntityTeleport.getConstructor(Entity);
		newPacketPlayOutSpawnEntityLiving = PacketPlayOutSpawnEntityLiving.getConstructor(EntityLiving);
		newPacketPlayOutScoreboardDisplayObjective = PacketPlayOutScoreboardDisplayObjective.getConstructor(int.class, ScoreboardObjective);
		if (minorVersion >= 7) {
			newChatComponentText = ChatComponentText.getConstructor(String.class);
			newChatClickable = ChatClickable.getConstructor(EnumClickAction, String.class);
			newChatHoverable = ChatHoverable.getConstructors()[0];
		}
		if (minorVersion >= 8) {
			newPacketPlayOutPlayerListHeaderFooter = PacketPlayOutPlayerListHeaderFooter.getConstructors()[0];
			if (PacketPlayOutTitle != null) newPacketPlayOutTitle = PacketPlayOutTitle.getConstructor(EnumTitleAction, IChatBaseComponent, int.class, int.class, int.class);
			newPacketPlayOutPlayerInfo = PacketPlayOutPlayerInfo.getConstructor(EnumPlayerInfoAction, Array.newInstance(EntityPlayer, 0).getClass());
			newPlayerInfoData = PlayerInfoData.getConstructors()[0];
		}
		if (minorVersion >= 9) {
			newDataWatcherObject = DataWatcherObject.getConstructors()[0];
			newBossBattleServer = BossBattleServer.getConstructor(IChatBaseComponent, BarColor, BarStyle);
		}
		if (minorVersion >= 13) {
			newPacketPlayOutScoreboardObjective = PacketPlayOutScoreboardObjective.getConstructor(ScoreboardObjective, int.class);
			newPacketPlayOutScoreboardScore_1_13 = PacketPlayOutScoreboardScore.getConstructor(EnumScoreboardAction, String.class, String.class, int.class);
		} else {
			newPacketPlayOutScoreboardObjective = PacketPlayOutScoreboardObjective.getConstructor();
			newPacketPlayOutScoreboardScore_String = PacketPlayOutScoreboardScore.getConstructor(String.class);
			if (minorVersion >= 8) {
				newPacketPlayOutScoreboardScore = PacketPlayOutScoreboardScore.getConstructor(ScoreboardScore);
			} else {
				newPacketPlayOutScoreboardScore = PacketPlayOutScoreboardScore.getConstructor(ScoreboardScore, int.class);
			}
		}
		if (minorVersion >= 16) {
			newChatModifier = getConstructor(ChatModifier, 10);
			newPacketPlayOutChat = PacketPlayOutChat.getConstructor(IChatBaseComponent, ChatMessageType, UUID.class);
		} else if (minorVersion >= 7) {
			newChatModifier = ChatModifier.getConstructor();
			if (minorVersion >= 12) {
				newPacketPlayOutChat = PacketPlayOutChat.getConstructor(IChatBaseComponent, ChatMessageType);
			} else if (minorVersion >= 8) {
				newPacketPlayOutChat = PacketPlayOutChat.getConstructor(IChatBaseComponent, byte.class);
			} else {
				newPacketPlayOutChat = PacketPlayOutChat.getConstructor(IChatBaseComponent);
			}
		}
		if (minorVersion >= 17) {
			newPacketPlayOutEntityDestroy = PacketPlayOutEntityDestroy.getConstructor(int.class);
			newClientboundSetTitleTextPacket = ClientboundSetTitleTextPacket.getConstructor(IChatBaseComponent);
			newClientboundSetSubtitleTextPacket = ClientboundSetSubtitleTextPacket.getConstructor(IChatBaseComponent);
			newClientboundSetActionBarTextPacket = ClientboundSetActionBarTextPacket.getConstructor(IChatBaseComponent);
			newClientboundSetTitlesAnimationPacket = ClientboundSetTitlesAnimationPacket.getConstructor(int.class, int.class, int.class);
			newClientboundClearTitlesPacket = ClientboundClearTitlesPacket.getConstructor(boolean.class);
		} else {
			newPacketPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.getConstructor(ScoreboardTeam, int.class);
			newPacketPlayOutEntityDestroy = PacketPlayOutEntityDestroy.getConstructor(int[].class);
			if (minorVersion >= 9) {
				newPacketPlayOutBoss = PacketPlayOutBoss.getConstructor(PacketPlayOutBoss_Action, BossBattle);
			}
		}
	}

	/**
	 * Initializes required NMS fields
	 * @throws Exception - if something fails
	 */
	private void initializeFields() throws Exception {
		PLAYER_CONNECTION = getFields(EntityPlayer, PlayerConnection).get(0);

		PacketPlayOutScoreboardDisplayObjective_POSITION = getFields(PacketPlayOutScoreboardDisplayObjective, int.class).get(0);
		PacketPlayOutScoreboardDisplayObjective_OBJECTIVENAME = getFields(PacketPlayOutScoreboardDisplayObjective, String.class).get(0);
		
		PacketPlayOutScoreboardObjective_OBJECTIVENAME = getFields(PacketPlayOutScoreboardObjective, String.class).get(0);
		List<Field> fields = getFields(PacketPlayOutScoreboardObjective, int.class);
		PacketPlayOutScoreboardObjective_METHOD = fields.get(fields.size()-1);
		
		PacketPlayOutScoreboardTeam_NAME = getFields(PacketPlayOutScoreboardTeam, String.class).get(0);
		PacketPlayOutScoreboardTeam_PLAYERS = getFields(PacketPlayOutScoreboardTeam, Collection.class).get(0);

		PacketPlayOutEntityTeleport_ENTITYID = getFields(PacketPlayOutEntityTeleport, int.class).get(0);
		PacketPlayOutEntityTeleport_YAW = getFields(PacketPlayOutEntityTeleport, byte.class).get(0);
		PacketPlayOutEntityTeleport_PITCH = getFields(PacketPlayOutEntityTeleport, byte.class).get(1);

		PacketPlayOutEntity_ENTITYID = getFields(PacketPlayOutEntity, int.class).get(0);
		PacketPlayOutEntityMetadata_LIST = getFields(PacketPlayOutEntityMetadata, List.class).get(0);

		PacketPlayOutSpawnEntityLiving_ENTITYID = getFields(PacketPlayOutSpawnEntityLiving, int.class).get(0);
		PacketPlayOutSpawnEntityLiving_ENTITYTYPE = getFields(PacketPlayOutSpawnEntityLiving, int.class).get(1);
		PacketPlayOutSpawnEntityLiving_YAW = getFields(PacketPlayOutSpawnEntityLiving, byte.class).get(0);
		PacketPlayOutSpawnEntityLiving_PITCH = getFields(PacketPlayOutSpawnEntityLiving, byte.class).get(0);

		(PacketPlayOutEntityDestroy_ENTITIES = PacketPlayOutEntityDestroy.getDeclaredFields()[0]).setAccessible(true);
		PacketPlayOutNamedEntitySpawn_ENTITYID = getFields(PacketPlayOutNamedEntitySpawn, int.class).get(0);

		if (minorVersion >= 7) {
			NETWORK_MANAGER = getFields(PlayerConnection, NetworkManager).get(0);
			ChatBaseComponent_extra = getFields(ChatBaseComponent, List.class).get(0);
			ChatBaseComponent_modifier = getFields(ChatBaseComponent, ChatModifier).get(0);
			ChatComponentText_text = getFields(ChatComponentText, String.class).get(0);
			ChatClickable_action = getFields(ChatClickable, EnumClickAction).get(0);
			ChatClickable_value = getFields(ChatClickable, String.class).get(0);
			ChatHoverable_action = getFields(ChatHoverable, EnumHoverAction).get(0);
			List<Field> booleans = getFields(ChatModifier, Boolean.class);
			ChatModifier_bold = booleans.get(0);
			ChatModifier_italic = booleans.get(1);
			ChatModifier_underlined = booleans.get(2);
			ChatModifier_strikethrough = booleans.get(3);
			ChatModifier_obfuscated = booleans.get(4);
			ChatModifier_clickEvent = getFields(ChatModifier, ChatClickable).get(0);
			ChatModifier_hoverEvent = getFields(ChatModifier, ChatHoverable).get(0);
			PacketPlayInUseEntity_ENTITY = getFields(PacketPlayInUseEntity, int.class).get(0);
			PacketPlayInUseEntity_ACTION = getFields(PacketPlayInUseEntity, EnumEntityUseAction).get(0);
		}

		if (minorVersion >= 8) {
			CHANNEL = getFields(NetworkManager, Channel.class).get(0);
			PlayerInfoData_PING = getFields(PlayerInfoData, int.class).get(0);
			PlayerInfoData_GAMEMODE = getFields(PlayerInfoData, EnumGamemode).get(0);
			PlayerInfoData_PROFILE = getFields(PlayerInfoData, GameProfile.class).get(0);
			PlayerInfoData_LISTNAME = getFields(PlayerInfoData, IChatBaseComponent).get(0);
			PacketPlayOutPlayerInfo_ACTION = getFields(PacketPlayOutPlayerInfo, EnumPlayerInfoAction).get(0);
			PacketPlayOutPlayerInfo_PLAYERS = getFields(PacketPlayOutPlayerInfo, List.class).get(0);
			PacketPlayOutPlayerListHeaderFooter_HEADER = getFields(PacketPlayOutPlayerListHeaderFooter, IChatBaseComponent).get(0);
			PacketPlayOutPlayerListHeaderFooter_FOOTER = getFields(PacketPlayOutPlayerListHeaderFooter, IChatBaseComponent).get(1);
			PacketPlayOutScoreboardObjective_RENDERTYPE = getFields(PacketPlayOutScoreboardObjective, EnumScoreboardHealthDisplay).get(0);
		}
		
		if (minorVersion >= 9) {
			BossBattle_UUID = getFields(BossBattle, UUID.class).get(0);
			DataWatcherItem_TYPE = getFields(DataWatcherItem, DataWatcherObject).get(0);
			DataWatcherItem_VALUE = getFields(DataWatcherItem, Object.class).get(0);
			DataWatcherObject_SLOT = getFields(DataWatcherObject, int.class).get(0);
			DataWatcherObject_SERIALIZER = getFields(DataWatcherObject, DataWatcherSerializer).get(0);
			PacketPlayOutEntityTeleport_X = getFields(PacketPlayOutEntityTeleport, double.class).get(0);
			PacketPlayOutEntityTeleport_Y = getFields(PacketPlayOutEntityTeleport, double.class).get(1);
			PacketPlayOutEntityTeleport_Z = getFields(PacketPlayOutEntityTeleport, double.class).get(2);
			PacketPlayOutSpawnEntityLiving_UUID = getFields(PacketPlayOutSpawnEntityLiving, UUID.class).get(0);
			PacketPlayOutSpawnEntityLiving_X = getFields(PacketPlayOutSpawnEntityLiving, double.class).get(0);
			PacketPlayOutSpawnEntityLiving_Y = getFields(PacketPlayOutSpawnEntityLiving, double.class).get(1);
			PacketPlayOutSpawnEntityLiving_Z = getFields(PacketPlayOutSpawnEntityLiving, double.class).get(2);
		} else {
			//1.8-
			DataWatcherItem_TYPE = getFields(DataWatcherItem, int.class).get(1);
			DataWatcherItem_VALUE = getFields(DataWatcherItem, Object.class).get(0);
			PacketPlayOutEntityTeleport_X = getFields(PacketPlayOutEntityTeleport, int.class).get(1);
			PacketPlayOutEntityTeleport_Y = getFields(PacketPlayOutEntityTeleport, int.class).get(2);
			PacketPlayOutEntityTeleport_Z = getFields(PacketPlayOutEntityTeleport, int.class).get(3);
			PacketPlayOutSpawnEntityLiving_X = getFields(PacketPlayOutSpawnEntityLiving, int.class).get(2);
			PacketPlayOutSpawnEntityLiving_Y = getFields(PacketPlayOutSpawnEntityLiving, int.class).get(3);
			PacketPlayOutSpawnEntityLiving_Z = getFields(PacketPlayOutSpawnEntityLiving, int.class).get(4);
		}
		
		if (minorVersion >= 13) {
			PacketPlayOutScoreboardObjective_DISPLAYNAME = getFields(PacketPlayOutScoreboardObjective, IChatBaseComponent).get(0);
		} else {
			PacketPlayOutScoreboardObjective_DISPLAYNAME = getFields(PacketPlayOutScoreboardObjective, String.class).get(1);
		}
		
		if (minorVersion <= 14) {
			PacketPlayOutSpawnEntityLiving_DATAWATCHER = getFields(PacketPlayOutSpawnEntityLiving, DataWatcher).get(0);
		}

		if (minorVersion >= 16) {
			//1.16+
			fields = getFields(ChatHexColor, String.class);
			ChatHexColor_name = fields.get(fields.size()-1);
			ChatHexColor_rgb = getFields(ChatHexColor, int.class).get(0);
			ChatHoverable_value = getFields(ChatHoverable, Object.class).get(0);
			ChatModifier_color = getFields(ChatModifier, ChatHexColor).get(0);
		} else if (minorVersion >= 7) {
			ChatHoverable_value = getFields(ChatHoverable, IChatBaseComponent).get(0);
			ChatModifier_color = getFields(ChatModifier, EnumChatFormat).get(0);
		}
		if (minorVersion >= 17) {
			try {
				PING = getField(EntityPlayer, "e");
			} catch (NoSuchFieldException e) {
				//deobfuscated spigot jat
				PING = getField(EntityPlayer, "latency");
			}
		} else {
			PING = getField(EntityPlayer, "ping");
		}
	}

	/**
	 * Initializes required NMS methods
	 * @throws Exception - if something fails
	 */
	private void initializeMethods() throws Exception {
		getHandle = Class.forName("org.bukkit.craftbukkit." + serverPackage + ".entity.CraftPlayer").getMethod("getHandle");
		sendPacket = getMethod(PlayerConnection, new String[]{"sendPacket", "func_147359_a"}, Packet);
		ScoreboardTeam_getPlayerNameSet = getMethod(ScoreboardTeam, new String[]{"getPlayerNameSet", "func_96670_d"});
		ScoreboardScore_setScore = getMethod(ScoreboardScore, new String[]{"setScore", "func_96647_c"}, int.class);
		if (minorVersion >= 7) {
			ChatComponentText_addSibling = getMethod(ChatComponentText, new String[]{"addSibling", "a", "func_150257_a"}, IChatBaseComponent); //v1.7.R4+, v1.7.R3-
			ChatSerializer_DESERIALIZE = getMethod(ChatSerializer, new String[]{"a", "func_150699_a"}, String.class);
			EnumClickAction_a = getMethod(EnumClickAction, new String[]{"a", "func_150672_a"}, String.class);
			EnumHoverAction_a = getMethod(EnumHoverAction, new String[]{"a", "func_150684_a"}, String.class);
		}
		if (minorVersion >= 8) {
			ScoreboardTeam_setNameTagVisibility = getMethod(ScoreboardTeam, new String[]{"setNameTagVisibility", "a"}, EnumNameTagVisibility);
			getProfile = EntityPlayer.getMethod("getProfile");
		}
		if (minorVersion >= 9) {
			ScoreboardTeam_setCollisionRule = ScoreboardTeam.getMethod("setCollisionRule", EnumTeamPush);
			DataWatcher_REGISTER = DataWatcher.getMethod("register", DataWatcherObject, Object.class);
			BossBattleServer_setProgress = BossBattleServer.getMethod("setProgress", float.class);
			BossBattleServer_setCreateFog = BossBattleServer.getMethod("setCreateFog", boolean.class);
			BossBattleServer_setDarkenSky = BossBattleServer.getMethod("setDarkenSky", boolean.class);
			BossBattleServer_setPlayMusic = BossBattleServer.getMethod("setPlayMusic", boolean.class);
		} else {
			DataWatcher_REGISTER = getMethod(DataWatcher, new String[]{"a", "func_75682_a"}, int.class, Object.class);
		}
		if (minorVersion >= 13) {
			ScoreboardTeam_setPrefix = ScoreboardTeam.getMethod("setPrefix", IChatBaseComponent);
			ScoreboardTeam_setSuffix = ScoreboardTeam.getMethod("setSuffix", IChatBaseComponent);
			ScoreboardTeam_setColor = ScoreboardTeam.getMethod("setColor", EnumChatFormat);
		} else {
			ScoreboardTeam_setPrefix = getMethod(ScoreboardTeam, new String[]{"setPrefix", "func_96666_b"}, String.class);
			ScoreboardTeam_setSuffix = getMethod(ScoreboardTeam, new String[]{"setSuffix", "func_96662_c"}, String.class);
		}
		if (minorVersion >= 16) {
			ChatHexColor_ofInt = ChatHexColor.getMethod("a", int.class);
			ChatHexColor_ofString = ChatHexColor.getMethod("a", String.class);
		}
		if (minorVersion >= 17) {
			PacketPlayOutBoss_createAddPacket = PacketPlayOutBoss.getMethod("createAddPacket", BossBattle);
			PacketPlayOutBoss_createRemovePacket = PacketPlayOutBoss.getMethod("createRemovePacket", UUID.class);
			PacketPlayOutBoss_createUpdateProgressPacket = PacketPlayOutBoss.getMethod("createUpdateProgressPacket", BossBattle);
			PacketPlayOutBoss_createUpdateNamePacket = PacketPlayOutBoss.getMethod("createUpdateNamePacket", BossBattle);
			PacketPlayOutBoss_createUpdateStylePacket = PacketPlayOutBoss.getMethod("createUpdateStylePacket", BossBattle);
			PacketPlayOutBoss_createUpdatePropertiesPacket = PacketPlayOutBoss.getMethod("createUpdatePropertiesPacket", BossBattle);
			PacketPlayOutScoreboardTeam_of = PacketPlayOutScoreboardTeam.getMethod("a", ScoreboardTeam);
			PacketPlayOutScoreboardTeam_ofBoolean = PacketPlayOutScoreboardTeam.getMethod("a", ScoreboardTeam, boolean.class);
			PacketPlayOutScoreboardTeam_ofString = PacketPlayOutScoreboardTeam.getMethod("a", ScoreboardTeam, String.class, PacketPlayOutScoreboardTeam_a);
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
		List<Field> list = new ArrayList<Field>();
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
}