package me.neznamy.tab.shared.packets;

import java.lang.reflect.Field;
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
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import net.kyori.text.Component;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;

@SuppressWarnings({"rawtypes", "unchecked"})
public class PacketPlayOutPlayerInfo extends UniversalPacketPlayOut{

	public EnumPlayerInfoAction action;
	public List<PlayerInfoData> players = Lists.newArrayList();

	public PacketPlayOutPlayerInfo(EnumPlayerInfoAction action, List<PlayerInfoData> players) {
		this.action = action;
		this.players = players;
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
			if (MethodAPI.getInstance() != null) {
				nmsEquivalent = Enum.valueOf((Class<Enum>)MethodAPI.EnumPlayerInfoAction, toString());
			}
		}
		public static EnumPlayerInfoAction fromNMS(Object nms) {
			return EnumPlayerInfoAction.valueOf(nms.toString());
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
			if (MethodAPI.getInstance() != null) {
				nmsEquivalent = Enum.valueOf((Class<Enum>)MethodAPI.EnumGamemode, toString());
			}
		}
		public static EnumGamemode fromNMS(Object nms) {
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

		public int ping;
		public EnumGamemode gamemode;
		public String listName;
		public String name;
		public UUID uniqueId;
		public Object properties; //platform-specific skin data

		public PlayerInfoData(String name, UUID uniqueId, Object properties, int ping, EnumGamemode gamemode, String listName) {
			this.ping = ping;
			this.gamemode = gamemode;
			this.listName = listName;
			this.name = name;
			this.uniqueId = uniqueId;
			this.properties = properties;
		}
		public Object toNMS(){
			GameProfile profile = new GameProfile(uniqueId, name);
			profile.getProperties().clear();
			profile.getProperties().putAll((Multimap<String, Property>) properties);
			return MethodAPI.getInstance().newPlayerInfoData(profile, ping, gamemode.toNMS(), MethodAPI.getInstance().ICBC_fromString(Shared.jsonFromText(listName)));
		}
		public Object toBungee() {
			Item item = new Item();
			item.setDisplayName(Shared.jsonFromText(listName));
			item.setGamemode(gamemode.getNetworkId());
			item.setPing(ping);
			item.setProperties((String[][]) properties);
			item.setUsername(name);
			item.setUuid(uniqueId);
			return item;
		}
		public Object toVelocity() {
			com.velocitypowered.proxy.protocol.packet.PlayerListItem.Item item = new com.velocitypowered.proxy.protocol.packet.PlayerListItem.Item(uniqueId);
			item.setDisplayName((Component) me.neznamy.tab.platforms.velocity.Main.componentFromText(listName));
			item.setGameMode(gamemode.getNetworkId());
			item.setLatency(ping);
			item.setProperties((List<com.velocitypowered.api.util.GameProfile.Property>) properties);
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
		public static PlayerInfoData fromBungee(Object nmsData){
			Item item = (Item) nmsData;
			String name;
			try {
				name = (String) ((JSONObject) new JSONParser().parse(item.getDisplayName())).get("text");
			} catch (ParseException | NullPointerException e) {
				name = null;
			}
			return new PlayerInfoData(item.getUsername(), item.getUuid(), item.getProperties(), item.getPing(), EnumGamemode.fromId(item.getGamemode()), item.getDisplayName() == null ? null : name);
		}
		public static PlayerInfoData fromVelocity(Object nmsData){
			com.velocitypowered.proxy.protocol.packet.PlayerListItem.Item item = (com.velocitypowered.proxy.protocol.packet.PlayerListItem.Item) nmsData;
			return new PlayerInfoData(item.getName(), item.getUuid(), item.getProperties(), item.getLatency(), EnumGamemode.fromId(item.getGameMode()), me.neznamy.tab.platforms.velocity.Main.textFromComponent(item.getDisplayName()));
		}
	}

	public Object toNMS(ProtocolVersion clientVersion) throws Exception{
		Object packet = MethodAPI.getInstance().newPacketPlayOutPlayerInfo(action.toNMS());
		List<Object> items = Lists.newArrayList();
		for (PlayerInfoData data : players) {
			items.add(data.toNMS());
		}
		PLAYERS.set(packet, items);
		return packet;
	}
	public Object toBungee(ProtocolVersion clientVersion) {
		PlayerListItem packet = new PlayerListItem();
		packet.setAction(PlayerListItem.Action.valueOf(action.toString()));
		List items = Lists.newArrayList();
		for (PlayerInfoData data : players) {
			items.add(data.toBungee());
		}
		packet.setItems((Item[]) items.toArray(new Item[0]));
		return packet;
	}
	public Object toVelocity(ProtocolVersion clientVersion) {
		List items = Lists.newArrayList();
		for (PlayerInfoData data : players) {
			items.add(data.toVelocity());
		}
		return new com.velocitypowered.proxy.protocol.packet.PlayerListItem(action.getNetworkId(), (List<com.velocitypowered.proxy.protocol.packet.PlayerListItem.Item>) items);
	}
	public static PacketPlayOutPlayerInfo fromNMS(Object nmsPacket) throws Exception{
		if (!MethodAPI.PacketPlayOutPlayerInfo.isInstance(nmsPacket)) return null;
		EnumPlayerInfoAction action = EnumPlayerInfoAction.fromNMS(ACTION.get(nmsPacket));
		List<PlayerInfoData> listData = Lists.newArrayList();
		for (Object p : (List) PLAYERS.get(nmsPacket)) {
			listData.add(PlayerInfoData.fromNMS(p));
		}
		return new PacketPlayOutPlayerInfo(action, listData);
	}
	public static PacketPlayOutPlayerInfo fromBungee(Object bungeePacket){
		PlayerListItem item = (PlayerListItem) bungeePacket;
		EnumPlayerInfoAction action = EnumPlayerInfoAction.valueOf(item.getAction().toString());
		List<PlayerInfoData> listData = Lists.newArrayList();
		for (Item i : item.getItems()) {
			listData.add(PlayerInfoData.fromBungee(i));
		}
		return new PacketPlayOutPlayerInfo(action, listData);
	}
	public static PacketPlayOutPlayerInfo fromVelocity(Object velocityPacket){
		com.velocitypowered.proxy.protocol.packet.PlayerListItem item = (com.velocitypowered.proxy.protocol.packet.PlayerListItem) velocityPacket;
		EnumPlayerInfoAction action = EnumPlayerInfoAction.fromId(item.getAction());
		List<PlayerInfoData> listData = Lists.newArrayList();
		for (com.velocitypowered.proxy.protocol.packet.PlayerListItem.Item i : item.getItems()) {
			listData.add(PlayerInfoData.fromVelocity(i));
		}
		return new PacketPlayOutPlayerInfo(action, listData);
	}
	
	private static Map<String, Field> fields = getFields(MethodAPI.PacketPlayOutPlayerInfo);
	private static Field ACTION = fields.get("a");
	private static Field PLAYERS = fields.get("b");
	
	private static Map<String, Field> infodata = getFields(MethodAPI.PlayerInfoData);
	private static Field PING = infodata.get("b");
	private static Field GAMEMODE = infodata.get("c");
	private static Field PROFILE = infodata.get("d");
	private static Field LISTNAME = infodata.get("e");
}