package me.neznamy.tab.shared.packets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import me.neznamy.tab.platforms.bukkit.packets.NMSHook;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOut;
import me.neznamy.tab.platforms.velocity.VelocityUtils;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Action;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;

@SuppressWarnings({"rawtypes", "unchecked"})
public class PacketPlayOutPlayerInfo extends UniversalPacketPlayOut{
	
	private static Class<?> PacketPlayOutPlayerInfo = getNMSClass("PacketPlayOutPlayerInfo", "Packet201PlayerInfo");
	private static Class<Enum> EnumGamemode_ = (Class<Enum>) PacketPlayOut.getNMSClass("EnumGamemode", "WorldSettings$EnumGamemode");
	private static Class<Enum> EnumPlayerInfoAction_ = (Class<Enum>) PacketPlayOut.getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction", "EnumPlayerInfoAction");
	private static Constructor<?> newPacketPlayOutPlayerInfo = getConstructor(PacketPlayOutPlayerInfo, 2, 0);
	
	private static Class<?> PlayerInfoData_ = PacketPlayOut.getNMSClass("PacketPlayOutPlayerInfo$PlayerInfoData", "PlayerInfoData");
	private static Constructor<?> newPlayerInfoData = getConstructor(PlayerInfoData_, 5);
	
	private static final Field ACTION;
	private static Field PLAYERS;

	private static final Field PING;
	private static final Field GAMEMODE;
	private static final Field PROFILE;
	private static final Field LISTNAME;
	
	public EnumPlayerInfoAction action;
	public PlayerInfoData[] entries;

	public PacketPlayOutPlayerInfo(EnumPlayerInfoAction action, PlayerInfoData... entries) {
		this.action = action;
		this.entries = entries;
	}
	public PacketPlayOutPlayerInfo(EnumPlayerInfoAction action, List<PlayerInfoData> entries) {
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
			this.networkId = networkId;
			if (EnumGamemode_ != null) nmsEquivalent = Enum.valueOf(EnumGamemode_, toString());
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
			GameProfile profile = new GameProfile(uniqueId, name);
			if (skin != null) profile.getProperties().putAll((Multimap<String, Property>) skin);
			return newPlayerInfoData.newInstance(newPacketPlayOutPlayerInfo.newInstance(null, Collections.EMPTY_LIST), profile, latency, gameMode == null ? null : gameMode.toNMS(), 
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
			GameProfile profile = (GameProfile) PROFILE.get(nmsData);
			Object nmsComponent = LISTNAME.get(nmsData);
			IChatBaseComponent listName = IChatBaseComponent.fromString(NMSHook.componentToString(nmsComponent));
			return new PlayerInfoData(profile.getName(), profile.getId(), profile.getProperties(), ping, gamemode, listName);
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
			return "PlayerInfoData{latency=" + latency + ",gameMode=" + gameMode + ",displayName=" + displayName + ",name=" + name + ",uniqueId=" + uniqueId + ",skin=" + skin + ",uuid belongs to: " + analyzeUUID(uniqueId) + "}";
		}
		private static String analyzeUUID(UUID uuid) {
			String result = "";
			ITabPlayer p;
			if ((p = Shared.getPlayer(uuid)) != null) {
				result = "[UUID of " + p.getName() + "]";
			}
			if ((p = Shared.getPlayerByTablistUUID(uuid)) != null) {
				result += "[TablistUUID of " + p.getName() + "]";
			}
			if (result.length() == 0) result = "Unknown";
			return result;
		}
	}

	public Object toNMS(ProtocolVersion clientVersion) throws Exception{
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			Object packet = newPacketPlayOutPlayerInfo.newInstance(action.toNMS(), Collections.EMPTY_LIST);
			List<Object> items = new ArrayList<Object>();
			for (PlayerInfoData data : entries) {
				items.add(data.toNMS(clientVersion));
			}
			PLAYERS.set(packet, items);
			return packet;
		} else {
			Object packet = newPacketPlayOutPlayerInfo.newInstance();
			PlayerInfoData data = entries[0];
			ACTION.set(packet, action.getNetworkId());
			net.minecraft.util.com.mojang.authlib.GameProfile profile = new net.minecraft.util.com.mojang.authlib.GameProfile(data.uniqueId, data.name);
			if (data.skin != null) profile.getProperties().putAll((net.minecraft.util.com.google.common.collect.Multimap<String, net.minecraft.util.com.mojang.authlib.properties.Property>) data.skin);
			PROFILE.set(packet, profile);
			GAMEMODE.set(packet, data.gameMode.networkId);
			PING.set(packet, data.latency);
			LISTNAME.set(packet, cutTo(data.displayName.toColoredText(), 16));
			return packet;
		}
	}
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
			net.minecraft.util.com.mojang.authlib.GameProfile profile = (net.minecraft.util.com.mojang.authlib.GameProfile) PROFILE.get(nmsPacket);
			IChatBaseComponent listName = IChatBaseComponent.fromColoredText((String) LISTNAME.get(nmsPacket));
			PlayerInfoData data = new PlayerInfoData(profile.getName(), profile.getId(), profile.getProperties(), ping, gamemode, listName);
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

	static {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			Map<String, Field> fields = getFields(PacketPlayOutPlayerInfo);
			ACTION = getField(fields, "a");
			PLAYERS = getField(fields, "b");

			Map<String, Field> infodata = getFields(PlayerInfoData_);
			PING = getField(infodata, "b");
			GAMEMODE = getField(infodata, "c");
			PROFILE = getField(infodata, "d");
			LISTNAME = getField(infodata, "e");
		} else {
			Map<String, Field> fields = getFields(PacketPlayOutPlayerInfo);
			ACTION = getField(fields, "action");
			PING = getField(fields, "ping");
			GAMEMODE = getField(fields, "gamemode");
			PROFILE = getField(fields, "player");
			LISTNAME = getField(fields, "username");
		}
	}
}