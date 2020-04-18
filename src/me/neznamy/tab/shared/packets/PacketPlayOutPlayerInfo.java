package me.neznamy.tab.shared.packets;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import net.kyori.text.Component;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Action;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;

@SuppressWarnings({"rawtypes", "unchecked"})
public class PacketPlayOutPlayerInfo extends UniversalPacketPlayOut{

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
			if (MethodAPI.getInstance() != null && ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
				nmsEquivalent = Enum.valueOf((Class<Enum>)MethodAPI.EnumPlayerInfoAction, toString());
			}
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
			if (MethodAPI.getInstance() != null && ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
				nmsEquivalent = Enum.valueOf((Class<Enum>)MethodAPI.EnumGamemode, toString());
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
		public String displayName;
		public String name;
		public UUID uniqueId;
		public Object skin; //platform-specific skin data

		public PlayerInfoData(String name, UUID uniqueId) {
			this.name = name;
			this.uniqueId = uniqueId;
		}
		public PlayerInfoData(String name, UUID uniqueId, Object skin, int latency, EnumGamemode gameMode, String displayName) {
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
		public Object toNMS(){
			GameProfile profile = new GameProfile(uniqueId, name);
			if (skin != null) profile.getProperties().putAll((Multimap<String, Property>) skin);
			return MethodAPI.getInstance().newPlayerInfoData(profile, latency, gameMode == null ? null : gameMode.toNMS(), MethodAPI.getInstance().ICBC_fromString(new IChatBaseComponent(displayName).toString()));
		}
		public Object toBungee(ProtocolVersion clientVersion) {
			Item item = new Item();
			if (clientVersion.getNetworkId() >= ProtocolVersion.v1_8.getNetworkId()) {
				item.setDisplayName(new IChatBaseComponent(displayName).toString());
			} else {
				item.setDisplayName(displayName == null ? name : displayName);
			}
			if (gameMode != null) item.setGamemode(gameMode.getNetworkId());
			item.setPing(latency);
			item.setProperties((String[][]) skin);
			item.setUsername(name);
			item.setUuid(uniqueId);
			return item;
		}
		public Object toVelocity() {
			com.velocitypowered.proxy.protocol.packet.PlayerListItem.Item item = new com.velocitypowered.proxy.protocol.packet.PlayerListItem.Item(uniqueId);
			item.setDisplayName((Component) me.neznamy.tab.platforms.velocity.Main.componentFromText(displayName));
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
			String listName = (nmsComponent == null ? null : MethodAPI.getInstance().CCM_fromComponent(nmsComponent));
			return new PlayerInfoData(profile.getName(), profile.getId(), profile.getProperties(), ping, gamemode, listName);
		}
		public static PlayerInfoData fromBungee(Object nmsData, ProtocolVersion clientVersion){
			Item item = (Item) nmsData;
			String name = null;
			if (item.getDisplayName() != null) {
				if (clientVersion.getNetworkId() >= ProtocolVersion.v1_8.getNetworkId()) {
					try {
						name = (String) ((JSONObject) new JSONParser().parse(item.getDisplayName())).get("text");
					} catch (ParseException e) {
					}
				} else {
					name = item.getDisplayName();
				}
			}
			return new PlayerInfoData(item.getUsername(), item.getUuid(), item.getProperties(), item.getPing(), EnumGamemode.fromId(item.getGamemode()), item.getDisplayName() == null ? null : name);
		}
		public static PlayerInfoData fromVelocity(Object nmsData){
			com.velocitypowered.proxy.protocol.packet.PlayerListItem.Item item = (com.velocitypowered.proxy.protocol.packet.PlayerListItem.Item) nmsData;
			return new PlayerInfoData(item.getName(), item.getUuid(), item.getProperties(), item.getLatency(), EnumGamemode.fromId(item.getGameMode()), me.neznamy.tab.platforms.velocity.Main.textFromComponent(item.getDisplayName()));
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
			Object packet = MethodAPI.getInstance().newPacketPlayOutPlayerInfo(action.toNMS());
			List<Object> items = new ArrayList<Object>();
			for (PlayerInfoData data : entries) {
				items.add(data.toNMS());
			}
			PLAYERS.set(packet, items);
			return packet;
		} else {
			Object packet = MethodAPI.getInstance().newPacketPlayOutPlayerInfo(null);
			PlayerInfoData data = entries[0];
			ACTION.set(packet, action.getNetworkId());
			net.minecraft.util.com.mojang.authlib.GameProfile profile = new net.minecraft.util.com.mojang.authlib.GameProfile(data.uniqueId, data.name);
			if (data.skin != null) profile.getProperties().putAll((net.minecraft.util.com.google.common.collect.Multimap<String, net.minecraft.util.com.mojang.authlib.properties.Property>) data.skin);
			PROFILE.set(packet, profile);
			GAMEMODE.set(packet, data.gameMode.networkId);
			PING.set(packet, data.latency);
			LISTNAME.set(packet, cutTo(data.displayName, 16));
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
			items.add(data.toVelocity());
		}
		return new com.velocitypowered.proxy.protocol.packet.PlayerListItem(action.getNetworkId(), (List<com.velocitypowered.proxy.protocol.packet.PlayerListItem.Item>) items);
	}
	public static PacketPlayOutPlayerInfo fromNMS(Object nmsPacket) throws Exception{
		if (!MethodAPI.PacketPlayOutPlayerInfo.isInstance(nmsPacket)) return null;
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
			String listName = (String) LISTNAME.get(nmsPacket);
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

	private static Field ACTION;
	private static Field PLAYERS;

	private static Field PING;
	private static Field GAMEMODE;
	private static Field PROFILE;
	private static Field LISTNAME;

	static {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			Map<String, Field> fields = getFields(MethodAPI.PacketPlayOutPlayerInfo);
			ACTION = getField(fields, "a");
			PLAYERS = getField(fields, "b");

			Map<String, Field> infodata = getFields(MethodAPI.PlayerInfoData);
			PING = getField(infodata, "b");
			GAMEMODE = getField(infodata, "c");
			PROFILE = getField(infodata, "d");
			LISTNAME = getField(infodata, "e");
		} else {
			Map<String, Field> fields = getFields(MethodAPI.PacketPlayOutPlayerInfo);
			ACTION = getField(fields, "action");
			PING = getField(fields, "ping");
			GAMEMODE = getField(fields, "gamemode");
			PROFILE = getField(fields, "player");
			LISTNAME = getField(fields, "username");
		}
	}
}