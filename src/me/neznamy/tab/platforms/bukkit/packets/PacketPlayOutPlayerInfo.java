package me.neznamy.tab.platforms.bukkit.packets;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class PacketPlayOutPlayerInfo extends PacketPlayOut{

	private EnumPlayerInfoAction action;
	private List<PlayerInfoData> list = Lists.newArrayList();

	public PacketPlayOutPlayerInfo(EnumPlayerInfoAction action) {
		this.action = action;
	}
	public PacketPlayOutPlayerInfo(EnumPlayerInfoAction action, Player... players) {
		this.action = action;
		for (Player player : players) {
			list.add(new PlayerInfoData(MethodAPI.getInstance().getProfile(player), MethodAPI.getInstance().getPing(player), EnumGamemode.valueOf(player.getGameMode().toString()), player.getPlayerListName()));
		}
	}
	public PacketPlayOutPlayerInfo(EnumPlayerInfoAction action, List<PlayerInfoData> players) {
		this.action = action;
		this.list = players;
	}
	public PacketPlayOutPlayerInfo setAction(EnumPlayerInfoAction action) {
		this.action = action;
		return this;
	}
	public PacketPlayOutPlayerInfo setPlayers(List<PlayerInfoData> players){
		list = players;
		return this;
	}
	public EnumPlayerInfoAction getAction() {
		return action;
	}
	public List<PlayerInfoData> getPlayers(){
		return list;
	}

	public enum EnumPlayerInfoAction{

		ADD_PLAYER,
		REMOVE_PLAYER,
		UPDATE_DISPLAY_NAME,
		UPDATE_GAME_MODE,
		UPDATE_LATENCY;

		private Object nmsEquivalent;

		private EnumPlayerInfoAction() {
			nmsEquivalent = Enum.valueOf((Class<Enum>)MethodAPI.EnumPlayerInfoAction, toString());
		}
		public static EnumPlayerInfoAction fromNMS(Object nms) {
			return EnumPlayerInfoAction.valueOf(nms.toString());
		}
		public Object toNMS() {
			return nmsEquivalent;
		}
	}
	public enum EnumGamemode{

		NOT_SET, 
		SURVIVAL, 
		CREATIVE, 
		ADVENTURE, 
		SPECTATOR;

		private Object nmsEquivalent;

		private EnumGamemode() {
			nmsEquivalent = Enum.valueOf((Class<Enum>)MethodAPI.EnumGamemode, toString());
		}
		public static EnumGamemode fromNMS(Object nms) {
			return EnumGamemode.valueOf(nms.toString());
		}
		public Object toNMS() {
			return nmsEquivalent;
		}
	}

	public static class PlayerInfoData{

		public int ping;
		public EnumGamemode gamemode;
		public Object profile;
		public String playerListName;

		public PlayerInfoData(Object profile, int ping, EnumGamemode gamemode, String playerListName) {
			this.profile = profile;
			this.ping = ping;
			this.gamemode = gamemode;
			this.playerListName = playerListName;
		}
		public Object toNMS(Object packet){
			return MethodAPI.getInstance().newPlayerInfoData(packet, profile, ping, gamemode.toNMS(), playerListName==null?null:Shared.mainClass.createComponent(playerListName));
		}
		public static PlayerInfoData fromNMS(Object nmsData) throws Exception{
			int ping = PING.getInt(nmsData);
			EnumGamemode gamemode = EnumGamemode.fromNMS(GAMEMODE.get(nmsData));
			Object profile = PROFILE.get(nmsData);
			Object nmsComponent = LISTNAME.get(nmsData);
			String listName;
			if (nmsComponent == null) {
				listName = null;
			} else {
				listName = MethodAPI.getInstance().CCM_fromComponent(nmsComponent);
			}
			return new PlayerInfoData(profile, ping, gamemode, listName);
		}
	}

	public Object toNMS(ProtocolVersion clientVersion) throws Exception{
		Object packet = MethodAPI.getInstance().newPacketPlayOutPlayerInfo(action.toNMS());
		List<Object> list2 = Lists.newArrayList();
		for (PlayerInfoData data : list) {
			list2.add(data.toNMS(packet));
		}
		PLAYERS.set(packet, list2);
		return packet;
	}
	public static PacketPlayOutPlayerInfo fromNMS(Object nmsPacket) throws Exception{
		if (!MethodAPI.PacketPlayOutPlayerInfo.isInstance(nmsPacket)) return null;
		EnumPlayerInfoAction action = EnumPlayerInfoAction.fromNMS(ACTION.get(nmsPacket));
		List<Object> players = (List<Object>) PLAYERS.get(nmsPacket);
		List<PlayerInfoData> listData = Lists.newArrayList();
		for (Object p : players) {
			listData.add(PlayerInfoData.fromNMS(p));
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