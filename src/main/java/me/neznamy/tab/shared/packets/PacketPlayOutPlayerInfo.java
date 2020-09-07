package me.neznamy.tab.shared.packets;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutPlayerInfo extends UniversalPacketPlayOut {

	//packet action
	public EnumPlayerInfoAction action;

	//list of affected players
	public List<PlayerInfoData> entries;

	/**
	 * Constructs a new instance with given parameters
	 * @param action - packet action
	 * @param entries - affected players
	 */
	public PacketPlayOutPlayerInfo(EnumPlayerInfoAction action, PlayerInfoData... entries) {
		this.action = action;
		this.entries = Lists.newArrayList(entries);
	}

	/**
	 * Constructs a new instance with given parameters
	 * @param action - packet action
	 * @param entries - affected players
	 */
	public PacketPlayOutPlayerInfo(EnumPlayerInfoAction action, List<PlayerInfoData> entries) {
		this.action = action;
		this.entries = entries;
	}

	/**
	 * Calls build method of packet builder instance and returns output
	 */
	@Override
	protected Object build(ProtocolVersion clientVersion) throws Exception {
		return builder.build(this, clientVersion);
	}

	/**
	 * An override to toString() method for better output
	 */
	@Override
	public String toString() {
		return "PacketPlayOutPlayerInfo{action=" + action + ",entries=" + entries + "}";
	}

	/**
	 * A subclass representing player list entry
	 */
	public static class PlayerInfoData {

		//ping
		public int latency;

		//gamemode
		public EnumGamemode gameMode;

		//tablist name
		public IChatBaseComponent displayName;

		//username
		public String name;

		//uuid
		public UUID uniqueId;

		//platform-specific skin data
		public Object skin;

		/**
		 * Constructs a new instance with given parameters
		 * @param name - player's name
		 * @param uniqueId - player's uuid
		 * @param skin - player's skin
		 * @param latency - player's ping
		 * @param gameMode - player's gamemode
		 * @param displayName - player's tablist name
		 */
		public PlayerInfoData(String name, UUID uniqueId, Object skin, int latency, EnumGamemode gameMode, IChatBaseComponent displayName) {
			this.name = name;
			this.uniqueId = uniqueId;
			this.skin = skin;
			this.latency = latency;
			this.gameMode = gameMode;
			this.displayName = displayName;
		}

		/**
		 * Creates and returns a clone of this instance
		 */
		public PlayerInfoData clone() {
			return new PlayerInfoData(name, uniqueId, skin, latency, gameMode, displayName);
		}

		/**
		 * An override to toString() method for better output
		 */
		@Override
		public String toString() {
			return "PlayerInfoData{latency=" + latency + ",gameMode=" + gameMode + ",displayName=" + displayName + 
					",name=" + name + ",uniqueId=" + uniqueId + ",skin=" + skin + "}";
		}
	}

	/**
	 * En enum representing packet action
	 */
	public enum EnumPlayerInfoAction {

		ADD_PLAYER,
		UPDATE_GAME_MODE,
		UPDATE_LATENCY,
		UPDATE_DISPLAY_NAME,
		REMOVE_PLAYER;

	}

	/**
	 * An enum representing gamemode
	 */
	public enum EnumGamemode {

		NOT_SET,
		SURVIVAL,
		CREATIVE,
		ADVENTURE,
		SPECTATOR;

	}
}