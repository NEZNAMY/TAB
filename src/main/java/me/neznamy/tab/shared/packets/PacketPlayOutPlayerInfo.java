package me.neznamy.tab.shared.packets;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;

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
		return TAB.getInstance().getPacketBuilder().build(this, clientVersion);
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
		public EnumGamemode gameMode = EnumGamemode.SURVIVAL; //protocollib causes NPE even when action does not use gamemode

		//tablist name
		public IChatBaseComponent displayName;

		//username
		public String name;

		//uuid
		public UUID uniqueId;

		//platform-specific skin data
		public Object skin;

		/**
		 * Constructor perfect for ADD_PLAYER action
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
		 * Constructor perfect for UPDATE_GAME_MODE action
		 * @param uniqueId - player's uuid
		 * @param gameMode - player's gamemode
		 */
		public PlayerInfoData(UUID uniqueId, EnumGamemode gameMode) {
			this.uniqueId = uniqueId;
			this.gameMode = gameMode;
		}
		
		/**
		 * Constructor perfect for UPDATE_LATENCY action
		 * @param uniqueId - player's uuid
		 * @param latency - player's ping
		 */
		public PlayerInfoData(UUID uniqueId, int latency) {
			this.uniqueId = uniqueId;
			this.latency = latency;
		}
		
		/**
		 * Constructor perfect for UPDATE_DISPLAY_NAME action
		 * @param uniqueId - player's uuid
		 * @param displayName - player's tablist name
		 */
		public PlayerInfoData(UUID uniqueId, IChatBaseComponent displayName) {
			this.uniqueId = uniqueId;
			this.displayName = displayName;
		}
		
		/**
		 * Constructor perfect for REMOVE_PLAYER action
		 * @param uniqueId - player's uuid
		 */
		public PlayerInfoData(UUID uniqueId) {
			this.uniqueId = uniqueId;
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