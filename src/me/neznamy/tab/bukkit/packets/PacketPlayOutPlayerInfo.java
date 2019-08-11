package me.neznamy.tab.bukkit.packets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;

import me.neznamy.tab.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.Shared;

public class PacketPlayOutPlayerInfo extends PacketPlayOut{

	private EnumPlayerInfoAction action;
	private List<PlayerInfoData> list = Lists.newArrayList();

	public PacketPlayOutPlayerInfo() {
	}
	public PacketPlayOutPlayerInfo(EnumPlayerInfoAction action) {
		this.action = action;
	}
	public PacketPlayOutPlayerInfo(EnumPlayerInfoAction action, Player... players) {
		this.action = action;
		for (Player player : players) {
			list.add(new PlayerInfoData(MethodAPI.getInstance().getProfile(player), MethodAPI.getInstance().getPing(player), player.getGameMode(), player.getPlayerListName()));
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
		
		ADD_PLAYER(EnumConstant.EnumPlayerInfoAction_ADD_PLAYER),
		REMOVE_PLAYER(EnumConstant.EnumPlayerInfoAction_REMOVE_PLAYER),
		UPDATE_DISPLAY_NAME(EnumConstant.EnumPlayerInfoAction_UPDATE_DISPLAY_NAME),
		UPDATE_GAME_MODE(EnumConstant.EnumPlayerInfoAction_UPDATE_GAME_MODE),
		UPDATE_LATENCY(EnumConstant.EnumPlayerInfoAction_UPDATE_LATENCY);
		
		private Object nmsEquivalent;
		
		private EnumPlayerInfoAction(Object nmsEquivalent) {
			this.nmsEquivalent = nmsEquivalent;
		}
		public static EnumPlayerInfoAction fromNMS(Object nmsCommand) {
			return EnumPlayerInfoAction.valueOf(nmsCommand.toString());
	    }
	    public Object toNMS() {
	    	return nmsEquivalent;
	    }
	}

	public static class PlayerInfoData{
		
		private int ping;
		private GameMode gamemode;
		private GameProfile profile;
		private String playerListName;

		public PlayerInfoData(GameProfile profile, int ping, GameMode gamemode, String playerListName) {
			this.profile = profile;
			this.ping = ping;
			this.gamemode = gamemode;
			this.playerListName = playerListName;
		}
		public PlayerInfoData setGameProfile(GameProfile profile) {
			this.profile = profile;
			return this;
		}
		public PlayerInfoData setPing(int ping) {
			this.ping = ping;
			return this;
		}
		public PlayerInfoData setGameMode(GameMode gamemode) {
			this.gamemode = gamemode;
			return this;
		}
		public PlayerInfoData setPlayerListName(String playerListName) {
			this.playerListName = playerListName;
			return this;
		}
		public GameProfile getGameProfile() {
			return profile;
		}
		public int getPing() {
			return ping;
		}
		public GameMode getGameMode() {
			return gamemode;
		}
		public String getPlayerListName() {
			return playerListName;
		}
		public Object toNMS(Object packet) throws Exception{
			Object data;
			if (newPlayerInfoData.getParameterCount() == 5) {
				data = newPlayerInfoData.newInstance(packet, profile, ping, EnumGamemode.fromBukkit(gamemode).toNMS(), playerListName==null?null:Shared.mainClass.createComponent(playerListName));
			} else {
				data = newPlayerInfoData.newInstance(profile, ping, EnumGamemode.fromBukkit(gamemode).toNMS(), playerListName==null?null:Shared.mainClass.createComponent(playerListName));
			}
			return data;
		}
		public static PlayerInfoData fromNMS(Object nmsData) throws Exception{
			int ping = PlayerInfoData_PING.getInt(nmsData);
			GameMode gamemode = EnumGamemode.fromNMS(PlayerInfoData_GAMEMODE.get(nmsData)).toBukkit();
			GameProfile profile = (GameProfile) PlayerInfoData_PROFILE.get(nmsData);
			Object nmsComponent = PlayerInfoData_LISTNAME.get(nmsData);
			String listName;
			if (nmsComponent == null) {
				listName = null;
			} else {
				listName = MethodAPI.getInstance().CCM_fromComponent(nmsComponent);
			}
			return new PlayerInfoData(profile, ping, gamemode, listName);
		}
	}

	public Object toNMS() throws Exception{
		Object packet = newPacketPlayOutPlayerInfo.newInstance();
		PacketPlayOutPlayerInfo_ACTION.set(packet, action.toNMS());
		List<Object> list2 = Lists.newArrayList();
		for (PlayerInfoData data : list) {
			list2.add(data.toNMS(packet));
		}
		PacketPlayOutPlayerInfo_PLAYERS.set(packet, list2);
		return packet;
	}
	@SuppressWarnings("unchecked")
	public static PacketPlayOutPlayerInfo fromNMS(Object nmsPacket) throws Exception{
		if (!PacketPlayOutPlayerInfo.isInstance(nmsPacket)) return null;
		EnumPlayerInfoAction action = EnumPlayerInfoAction.fromNMS(PacketPlayOutPlayerInfo_ACTION.get(nmsPacket));
		List<Object> players = (List<Object>) PacketPlayOutPlayerInfo_PLAYERS.get(nmsPacket);
		List<PlayerInfoData> listData = Lists.newArrayList();
		for (Object p : players) {
			listData.add(PlayerInfoData.fromNMS(p));
		}
		return new PacketPlayOutPlayerInfo(action, listData);
	}
	
	
	private static Class<?> PacketPlayOutPlayerInfo;
	private static Constructor<?> newPacketPlayOutPlayerInfo;
	private static Class<?> _PlayerInfoData;
	private static Constructor<?> newPlayerInfoData;
	private static Field PacketPlayOutPlayerInfo_ACTION;
	private static Field PacketPlayOutPlayerInfo_PLAYERS;
	private static Field PlayerInfoData_PING;
	private static Field PlayerInfoData_GAMEMODE;
	private static Field PlayerInfoData_PROFILE;
	private static Field PlayerInfoData_LISTNAME;
	
	static {
		try {
			PacketPlayOutPlayerInfo = getNMSClass("PacketPlayOutPlayerInfo");
			if (version.equals("v1_8_R1")) {
				_PlayerInfoData = getNMSClass("PlayerInfoData");
			} else {
				_PlayerInfoData = getNMSClass("PacketPlayOutPlayerInfo$PlayerInfoData");
			}
			(PacketPlayOutPlayerInfo_ACTION = PacketPlayOutPlayerInfo.getDeclaredField("a")).setAccessible(true);
			(PacketPlayOutPlayerInfo_PLAYERS = PacketPlayOutPlayerInfo.getDeclaredField("b")).setAccessible(true);
			newPacketPlayOutPlayerInfo = PacketPlayOutPlayerInfo.getConstructor();
			newPlayerInfoData = _PlayerInfoData.getDeclaredConstructors()[0];
			(PlayerInfoData_PING = _PlayerInfoData.getDeclaredField("b")).setAccessible(true);
			(PlayerInfoData_GAMEMODE = _PlayerInfoData.getDeclaredField("c")).setAccessible(true);
			(PlayerInfoData_PROFILE = _PlayerInfoData.getDeclaredField("d")).setAccessible(true);
			(PlayerInfoData_LISTNAME = _PlayerInfoData.getDeclaredField("e")).setAccessible(true);
		} catch (Exception e) {
			Shared.error("Failed to initialize PacketPlayOutPlayerInfo class", e);
		}
	}
}