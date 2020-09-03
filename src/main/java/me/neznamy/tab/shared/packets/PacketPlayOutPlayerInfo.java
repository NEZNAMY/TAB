package me.neznamy.tab.shared.packets;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutPlayerInfo extends UniversalPacketPlayOut {

	public EnumPlayerInfoAction action;
	public List<PlayerInfoData> entries;

	public PacketPlayOutPlayerInfo(EnumPlayerInfoAction action, PlayerInfoData... entries) {
		this.action = action;
		this.entries = Lists.newArrayList(entries);
	}

	public PacketPlayOutPlayerInfo(EnumPlayerInfoAction action, List<PlayerInfoData> entries) {
		this.action = action;
		this.entries = entries;
	}

	@Override
	public String toString() {
		return "PacketPlayOutPlayerInfo{action=" + action + ",entries=" + entries + "}";
	}

	@Override
	protected Object build(ProtocolVersion clientVersion) throws Exception {
		return builder.build(this, clientVersion);
	}

	public static class PlayerInfoData {

		public int latency;
		public EnumGamemode gameMode;
		public IChatBaseComponent displayName;
		public String name;
		public UUID uniqueId;
		public Object skin; //platform-specific skin data

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

		@Override
		public String toString() {
			return "PlayerInfoData{latency=" + latency + ",gameMode=" + gameMode + ",displayName=" + displayName + ",name=" + name + ",uniqueId=" + uniqueId + ",skin=" + skin + "}";
		}
	}
	
	public enum EnumPlayerInfoAction {

		ADD_PLAYER,
		UPDATE_GAME_MODE,
		UPDATE_LATENCY,
		UPDATE_DISPLAY_NAME,
		REMOVE_PLAYER;

	}
	
	public enum EnumGamemode {

		NOT_SET,
		SURVIVAL,
		CREATIVE,
		ADVENTURE,
		SPECTATOR;

	}
}