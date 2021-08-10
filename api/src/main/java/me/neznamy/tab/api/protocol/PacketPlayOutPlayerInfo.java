package me.neznamy.tab.api.protocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import me.neznamy.tab.api.chat.IChatBaseComponent;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutPlayerInfo implements CrossPlatformPacket {

	//packet action
	private EnumPlayerInfoAction action;

	//list of affected players
	private List<PlayerInfoData> entries;

	/**
	 * Constructs a new instance with given parameters
	 * @param action - packet action
	 * @param entries - affected players
	 */
	public PacketPlayOutPlayerInfo(EnumPlayerInfoAction action, PlayerInfoData... entries) {
		this.action = action;
		this.entries = new ArrayList<>(Arrays.asList(entries));
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

	@Override
	public String toString() {
		return String.format("PacketPlayOutPlayerInfo{action=%s,entries=%s}", action, entries);
	}

	public List<PlayerInfoData> getEntries() {
		return entries;
	}

	public EnumPlayerInfoAction getAction() {
		return action;
	}

	/**
	 * A subclass representing player list entry
	 */
	public static class PlayerInfoData {

		//ping
		private int latency;

		//gamemode
		private EnumGamemode gameMode = EnumGamemode.SURVIVAL; //protocollib causes NPE even when action does not use gamemode

		//tablist name
		private IChatBaseComponent displayName;

		//username
		private String name;

		//uuid
		private UUID uniqueId;

		//platform-specific skin data
		private Object skin;

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
		 * An override to toString() method for better output
		 */
		@Override
		public String toString() {
			return String.format("PlayerInfoData{latency=%s,gameMode=%s,displayName=%s,name=%s,uniqueId=%s,skin=%s}",
					latency, gameMode, displayName, name, uniqueId, skin);
		}

		public int getLatency() {
			return latency;
		}

		public void setLatency(int latency) {
			this.latency = latency;
		}

		public IChatBaseComponent getDisplayName() {
			return displayName;
		}

		public void setDisplayName(IChatBaseComponent displayName) {
			this.displayName = displayName;
		}

		public UUID getUniqueId() {
			return uniqueId;
		}

		public void setUniqueId(UUID uniqueId) {
			this.uniqueId = uniqueId;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public EnumGamemode getGameMode() {
			return gameMode;
		}

		public void setGameMode(EnumGamemode gameMode) {
			this.gameMode = gameMode;
		}

		public Object getSkin() {
			return skin;
		}

		public void setSkin(Object skin) {
			this.skin = skin;
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