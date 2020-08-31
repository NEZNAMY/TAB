package me.neznamy.tab.shared.packets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import me.neznamy.tab.platforms.bukkit.packets.NMSHook;
import me.neznamy.tab.platforms.velocity.VelocityUtils;
import me.neznamy.tab.shared.ProtocolVersion;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Action;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;

/**
 * A class representing platform specific packet class
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class PacketPlayOutPlayerInfo extends UniversalPacketPlayOut {

	private static Class<?> PacketPlayOutPlayerInfo;
	private static Class<Enum> EnumGamemode_;
	private static Class<Enum> EnumPlayerInfoAction_;
	private static Class<?> PlayerInfoData_;
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

	private static Field ACTION;
	private static Field PLAYERS;

	private static Field PING;
	private static Field GAMEMODE;
	private static Field PROFILE;
	private static Field LISTNAME;

	public EnumPlayerInfoAction action;
	public PlayerInfoData[] entries;

	public static void initializeClass() throws Exception {
		PacketPlayOutPlayerInfo = getNMSClass("PacketPlayOutPlayerInfo");
		try {
			EnumGamemode_ = (Class<Enum>) getNMSClass("EnumGamemode");
		} catch (ClassNotFoundException e) {
			//v1_8_R2 - v1_9_R2
			EnumGamemode_ = (Class<Enum>) getNMSClass("WorldSettings$EnumGamemode");
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			//1.8+
			try {
				//v1_8_R2+
				EnumPlayerInfoAction_ = (Class<Enum>) getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
				PlayerInfoData_ = getNMSClass("PacketPlayOutPlayerInfo$PlayerInfoData");
			} catch (ClassNotFoundException e) {
				//v1_8_R1
				EnumPlayerInfoAction_ = (Class<Enum>) getNMSClass("EnumPlayerInfoAction");
				PlayerInfoData_ = getNMSClass("PlayerInfoData");
			}
			newPacketPlayOutPlayerInfo2 = PacketPlayOutPlayerInfo.getConstructor(EnumPlayerInfoAction_, Iterable.class);
			GameProfile = Class.forName("com.mojang.authlib.GameProfile");
			PropertyMap = Class.forName("com.mojang.authlib.properties.PropertyMap");
			newPlayerInfoData = PlayerInfoData_.getConstructor(PacketPlayOutPlayerInfo, GameProfile, int.class, EnumGamemode_, NMSHook.IChatBaseComponent);
			(ACTION = PacketPlayOutPlayerInfo.getDeclaredField("a")).setAccessible(true);
			(PLAYERS = PacketPlayOutPlayerInfo.getDeclaredField("b")).setAccessible(true);
			(PING = PlayerInfoData_.getDeclaredField("b")).setAccessible(true);
			(GAMEMODE = PlayerInfoData_.getDeclaredField("c")).setAccessible(true);
			(PROFILE = PlayerInfoData_.getDeclaredField("d")).setAccessible(true);
			(LISTNAME = PlayerInfoData_.getDeclaredField("e")).setAccessible(true);
		} else {
			//1.7
			newPacketPlayOutPlayerInfo0 = PacketPlayOutPlayerInfo.getConstructor();
			GameProfile = Class.forName("net.minecraft.util.com.mojang.authlib.GameProfile");
			PropertyMap = Class.forName("net.minecraft.util.com.mojang.authlib.properties.PropertyMap");
			(ACTION = PacketPlayOutPlayerInfo.getDeclaredField("action")).setAccessible(true);
			(PING = PacketPlayOutPlayerInfo.getDeclaredField("ping")).setAccessible(true);
			(GAMEMODE = PacketPlayOutPlayerInfo.getDeclaredField("gamemode")).setAccessible(true);
			(PROFILE = PacketPlayOutPlayerInfo.getDeclaredField("player")).setAccessible(true);
			(LISTNAME = PacketPlayOutPlayerInfo.getDeclaredField("username")).setAccessible(true);
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

	public PacketPlayOutPlayerInfo(EnumPlayerInfoAction action, PlayerInfoData... entries) {
		this.action = action;
		this.entries = entries;
	}

	public PacketPlayOutPlayerInfo(EnumPlayerInfoAction action, Collection<PlayerInfoData> entries) {
		this.action = action;
		this.entries = entries.toArray(new PlayerInfoData[0]);
	}

	public enum EnumPlayerInfoAction{

		ADD_PLAYER(0),
		UPDATE_GAME_MODE(1),
		UPDATE_LATENCY(2),
		UPDATE_DISPLAY_NAME(3),
		REMOVE_PLAYER(4);

		private Object nmsEquivalent;
		private int networkId;

		private EnumPlayerInfoAction(int networkId) {
			this.networkId = networkId;
			if (EnumPlayerInfoAction_ != null) nmsEquivalent = Enum.valueOf(EnumPlayerInfoAction_, toString());
		}

		public static EnumPlayerInfoAction fromNMS(Object nms) {
			return EnumPlayerInfoAction.valueOf(nms.toString());
		}

		public static EnumPlayerInfoAction fromBungee(Object bungee) {
			return EnumPlayerInfoAction.valueOf(bungee.toString().replace("GAMEMODE", "GAME_MODE"));
		}

		public static EnumPlayerInfoAction fromId(int id) {
			for (EnumPlayerInfoAction action : values()) {
				if (action.networkId == id) return action;
			}
			return null;
		}

		public Object toNMS() {
			return nmsEquivalent;
		}

		public Object toBungee() {
			return PlayerListItem.Action.valueOf(toString().replace("GAME_MODE", "GAMEMODE"));
		}

		public int getNetworkId() {
			return networkId;
		}
	}
	public enum EnumGamemode{

		NOT_SET(-1), 
		SURVIVAL(0), 
		CREATIVE(1), 
		ADVENTURE(2), 
		SPECTATOR(3);

		private Object nmsEquivalent;
		private int networkId;

		private EnumGamemode(int networkId) {
			try {
				this.networkId = networkId;
				if (EnumGamemode_ != null) nmsEquivalent = Enum.valueOf(EnumGamemode_, toString());
			} catch (Exception e) {
				//spectator on <1.8
			}
		}

		public static EnumGamemode fromNMS(Object nms) {
			if (nms == null) return null;
			return EnumGamemode.valueOf(nms.toString());
		}

		public static EnumGamemode fromId(int id) {
			for (EnumGamemode action : values()) {
				if (action.networkId == id) return action;
			}
			return null;
		}

		public Object toNMS() {
			return nmsEquivalent;
		}

		public int getNetworkId() {
			return networkId;
		}
	}

	public static class PlayerInfoData{

		public int latency;
		public EnumGamemode gameMode;
		public IChatBaseComponent displayName;
		public String name;
		public UUID uniqueId;
		public Object skin; //platform-specific skin data

		public PlayerInfoData(String name, UUID uniqueId) {
			this.name = name;
			this.uniqueId = uniqueId;
		}

		public PlayerInfoData(String name, UUID uniqueId, Object skin, int latency, EnumGamemode gameMode, IChatBaseComponent displayName) {
			this.name = name;
			this.uniqueId = uniqueId;
			this.skin = skin;
			this.latency = latency;
			this.gameMode = gameMode;
			this.displayName = displayName;
		}

		public PlayerInfoData clone() {
			return new PlayerInfoData(name, uniqueId, skin, latency, gameMode, displayName);
		}

		public Object toNMS(ProtocolVersion clientVersion) throws Exception{
			Object profile = newGameProfile.newInstance(uniqueId, name);
			if (skin != null) PropertyMap_putAll.invoke(GameProfile_PROPERTIES.get(profile), skin);
			return newPlayerInfoData.newInstance(newPacketPlayOutPlayerInfo2.newInstance(null, Collections.EMPTY_LIST), profile, latency, gameMode == null ? null : gameMode.toNMS(), 
					displayName == null ? null : NMSHook.stringToComponent(displayName.toString(clientVersion)));
		}

		public Object toBungee(ProtocolVersion clientVersion) {
			Item item = new Item();
			if (displayName != null) {
				if (clientVersion.getNetworkId() >= ProtocolVersion.v1_8.getNetworkId()) {
					item.setDisplayName(displayName.toString(clientVersion));
				} else {
					item.setDisplayName(displayName.toColoredText());
				}
			} else if (clientVersion.getNetworkId() < ProtocolVersion.v1_8.getNetworkId()) {
				item.setDisplayName(name); //avoiding NPE, 1.7 client requires this, 1.8 added a leading boolean
			}
			if (gameMode != null) item.setGamemode(gameMode.getNetworkId());
			item.setPing(latency);
			item.setProperties((String[][]) skin);
			item.setUsername(name);
			item.setUuid(uniqueId);
			return item;
		}

		public Object toVelocity(ProtocolVersion clientVersion) {
			com.velocitypowered.proxy.protocol.packet.PlayerListItem.Item item = new com.velocitypowered.proxy.protocol.packet.PlayerListItem.Item(uniqueId);
			item.setDisplayName((Component) VelocityUtils.componentFromString(displayName == null ? null : displayName.toString(clientVersion)));
			if (gameMode != null) item.setGameMode(gameMode.getNetworkId());
			item.setLatency(latency);
			item.setProperties((List<com.velocitypowered.api.util.GameProfile.Property>) skin);
			item.setName(name);
			return item;
		}

		public static PlayerInfoData fromNMS(Object nmsData) throws Exception{
			int ping = PING.getInt(nmsData);
			EnumGamemode gamemode = EnumGamemode.fromNMS(GAMEMODE.get(nmsData));
			Object profile = PROFILE.get(nmsData);
			Object nmsComponent = LISTNAME.get(nmsData);
			IChatBaseComponent listName = IChatBaseComponent.fromString(NMSHook.componentToString(nmsComponent));
			return new PlayerInfoData((String) GameProfile_NAME.get(profile), (UUID) GameProfile_ID.get(profile), GameProfile_PROPERTIES.get(profile), ping, gamemode, listName);
		}

		public static PlayerInfoData fromBungee(Object nmsData, ProtocolVersion clientVersion){
			Item item = (Item) nmsData;
			return new PlayerInfoData(item.getUsername(), item.getUuid(), item.getProperties(), item.getPing(), EnumGamemode.fromId(item.getGamemode()), IChatBaseComponent.fromString(item.getDisplayName()));
		}

		public static PlayerInfoData fromVelocity(Object nmsData){
			com.velocitypowered.proxy.protocol.packet.PlayerListItem.Item item = (com.velocitypowered.proxy.protocol.packet.PlayerListItem.Item) nmsData;
			return new PlayerInfoData(item.getName(), item.getUuid(), item.getProperties(), item.getLatency(), EnumGamemode.fromId(item.getGameMode()), IChatBaseComponent.fromString(VelocityUtils.componentToString(item.getDisplayName())));
		}

		@Override
		public String toString() {
			return "PlayerInfoData{latency=" + latency + ",gameMode=" + gameMode + ",displayName=" + displayName + ",name=" + name + ",uniqueId=" + uniqueId + ",skin=" + skin + "}";
		}
	}

	@Override
	public Object toNMS(ProtocolVersion clientVersion) throws Exception{
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			Object packet = newPacketPlayOutPlayerInfo2.newInstance(action.toNMS(), Collections.EMPTY_LIST);
			List<Object> items = new ArrayList<Object>();
			for (PlayerInfoData data : entries) {
				items.add(data.toNMS(clientVersion));
			}
			PLAYERS.set(packet, items);
			return packet;
		} else {
			Object packet = newPacketPlayOutPlayerInfo0.newInstance();
			PlayerInfoData data = entries[0];
			ACTION.set(packet, action.getNetworkId());
			Object profile = newGameProfile.newInstance(data.uniqueId, data.name);
			if (data.skin != null) PropertyMap_putAll.invoke(GameProfile_PROPERTIES.get(profile), data.skin);
			PROFILE.set(packet, profile);
			GAMEMODE.set(packet, data.gameMode.networkId);
			PING.set(packet, data.latency);
			LISTNAME.set(packet, cutTo(data.displayName.toColoredText(), 16));
			return packet;
		}
	}

	@Override
	public Object toBungee(ProtocolVersion clientVersion) {
		PlayerListItem packet = new PlayerListItem();
		packet.setAction((Action) action.toBungee());
		Item[] items = new Item[entries.length];
		for (int i=0; i<entries.length; i++) {
			items[i] = (Item) entries[i].toBungee(clientVersion);
		}
		packet.setItems(items);
		return packet;
	}

	@Override
	public Object toVelocity(ProtocolVersion clientVersion) {
		List items = new ArrayList();
		for (PlayerInfoData data : entries) {
			items.add(data.toVelocity(clientVersion));
		}
		return new com.velocitypowered.proxy.protocol.packet.PlayerListItem(action.getNetworkId(), (List<com.velocitypowered.proxy.protocol.packet.PlayerListItem.Item>) items);
	}

	public static PacketPlayOutPlayerInfo fromNMS(Object nmsPacket) throws Exception{
		if (!PacketPlayOutPlayerInfo.isInstance(nmsPacket)) return null;
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			EnumPlayerInfoAction action = EnumPlayerInfoAction.fromNMS(ACTION.get(nmsPacket));
			List<PlayerInfoData> listData = new ArrayList<PlayerInfoData>();
			for (Object p : (List) PLAYERS.get(nmsPacket)) {
				listData.add(PlayerInfoData.fromNMS(p));
			}
			return new PacketPlayOutPlayerInfo(action, listData);
		} else {
			EnumPlayerInfoAction action = EnumPlayerInfoAction.fromId(ACTION.getInt(nmsPacket));
			int ping = PING.getInt(nmsPacket);
			EnumGamemode gamemode = EnumGamemode.fromId(GAMEMODE.getInt(nmsPacket));
			Object profile = PROFILE.get(nmsPacket);
			IChatBaseComponent listName = IChatBaseComponent.fromColoredText((String) LISTNAME.get(nmsPacket));
			PlayerInfoData data = new PlayerInfoData((String) GameProfile_NAME.get(profile), (UUID) GameProfile_ID.get(profile), GameProfile_PROPERTIES.get(profile), ping, gamemode, listName);
			return new PacketPlayOutPlayerInfo(action, Lists.newArrayList(data));
		}
	}

	public static PacketPlayOutPlayerInfo fromBungee(Object bungeePacket, ProtocolVersion clientVersion){
		if (!(bungeePacket instanceof PlayerListItem)) return null;
		PlayerListItem item = (PlayerListItem) bungeePacket;
		EnumPlayerInfoAction action = EnumPlayerInfoAction.fromBungee(item.getAction().toString());
		List<PlayerInfoData> listData = new ArrayList<PlayerInfoData>();
		for (Item i : item.getItems()) {
			listData.add(PlayerInfoData.fromBungee(i, clientVersion));
		}
		return new PacketPlayOutPlayerInfo(action, listData);
	}

	public static PacketPlayOutPlayerInfo fromVelocity(Object velocityPacket){
		if (!(velocityPacket instanceof com.velocitypowered.proxy.protocol.packet.PlayerListItem)) return null;
		com.velocitypowered.proxy.protocol.packet.PlayerListItem item = (com.velocitypowered.proxy.protocol.packet.PlayerListItem) velocityPacket;
		EnumPlayerInfoAction action = EnumPlayerInfoAction.fromId(item.getAction());
		List<PlayerInfoData> listData = new ArrayList<PlayerInfoData>();
		for (com.velocitypowered.proxy.protocol.packet.PlayerListItem.Item i : item.getItems()) {
			listData.add(PlayerInfoData.fromVelocity(i));
		}
		return new PacketPlayOutPlayerInfo(action, listData);
	}

	@Override
	public String toString() {
		return "PacketPlayOutPlayerInfo{action=" + action + ",entries=" + Arrays.toString(entries) + "}";
	}
}