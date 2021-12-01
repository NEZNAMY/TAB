package me.neznamy.tab.api.protocol;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import me.neznamy.tab.api.chat.IChatBaseComponent;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutPlayerInfo implements TabPacket {

	/** Packet action */
	private final EnumPlayerInfoAction action;

	/** List of affected entries */
	private final List<PlayerInfoData> entries;

	/**
	 * Constructs new instance with given parameters.
	 * 
	 * @param	action
	 * 			Packet action
	 * @param	entries
	 * 			Affected entries
	 */
	public PacketPlayOutPlayerInfo(EnumPlayerInfoAction action, PlayerInfoData... entries) {
		this.action = action;
		this.entries = Arrays.asList(entries);
	}

	/**
	 * Constructs new instance with given parameters.
	 * 
	 * @param	action
	 * 			Packet action
	 * @param	entries
	 * 			Affected entries
	 */
	public PacketPlayOutPlayerInfo(EnumPlayerInfoAction action, List<PlayerInfoData> entries) {
		this.action = action;
		this.entries = entries;
	}

	@Override
	public String toString() {
		return String.format("PacketPlayOutPlayerInfo{action=%s,entries=%s}", action, entries);
	}

	/**
	 * Returns {@link #action}
	 * @return	packet action
	 */
	public EnumPlayerInfoAction getAction() {
		return action;
	}

	/**
	 * Returns {@link #entries}
	 * @return	affected entries
	 */
	public List<PlayerInfoData> getEntries() {
		return entries;
	}

	/**
	 * A subclass representing player list entry
	 */
	public static class PlayerInfoData {

		/** Latency */
		private int latency;

		/** GameMode */
		private EnumGamemode gameMode = EnumGamemode.SURVIVAL; //ProtocolLib causes NPE even when action does not use GameMode

		/** 
		 * Display name displayed in TabList. Using {@code null} results in no display name
		 * and scoreboard team prefix/suffix being visible in TabList instead.
		 */
		private IChatBaseComponent displayName;

		/** Real name of affected player */
		private String name;

		/** Player UUID */
		private UUID uniqueId;

		/** platform-specific skin object */
		private Object skin;

		/**
		 * Constructs new instance with given parameters. Suitable for 
		 * {@link EnumPlayerInfoAction}.ADD_PLAYER action
		 * 
		 * @param	name
		 * 			Player's name
		 * @param	uniqueId
		 * 			Player's uuid
		 * @param	skin
		 * 			Player's platform-specific skin object
		 * @param	latency
		 * 			Player's ping
		 * @param	gameMode
		 * 			Player's GameMode
		 * @param	displayName
		 * 			Player's display name
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
		 * Constructs new instance with given parameters. Suitable for 
		 * {@link EnumPlayerInfoAction}.UPDATE_GAME_MODE action
		 * 
		 * @param	uniqueId
		 * 			Player's uuid
		 * @param	gameMode
		 * 			Player's GameMode
		 */
		public PlayerInfoData(UUID uniqueId, EnumGamemode gameMode) {
			this.uniqueId = uniqueId;
			this.gameMode = gameMode;
		}

		/**
		 * Constructs new instance with given parameters. Suitable for 
		 * {@link EnumPlayerInfoAction}.UPDATE_LATENCY action
		 * 
		 * @param	uniqueId
		 * 			Player's uuid
		 * @param	latency
		 * 			Player's ping
		 */
		public PlayerInfoData(UUID uniqueId, int latency) {
			this.uniqueId = uniqueId;
			this.latency = latency;
		}

		/**
		 * Constructs new instance with given parameters. Suitable for 
		 * {@link EnumPlayerInfoAction}.UPDATE_DISPLAY_NAME action
		 * 
		 * @param	uniqueId
		 * 			Player's uuid
		 * @param	displayName
		 * 			Player's display name
		 */
		public PlayerInfoData(UUID uniqueId, IChatBaseComponent displayName) {
			this.uniqueId = uniqueId;
			this.displayName = displayName;
		}

		/**
		 * Constructs new instance with given parameter. Suitable for 
		 * {@link EnumPlayerInfoAction}.REMOVE_PLAYER action
		 * 
		 * @param	uniqueId
		 * 			Player's uuid
		 */
		public PlayerInfoData(UUID uniqueId) {
			this.uniqueId = uniqueId;
		}

		@Override
		public String toString() {
			return String.format("PlayerInfoData{latency=%s,gameMode=%s,displayName=%s,name=%s,uniqueId=%s,skin=%s}",
					latency, gameMode, displayName, name, uniqueId, skin);
		}

		/**
		 * Returns {@link #latency}
		 * @return	latency
		 */
		public int getLatency() {
			return latency;
		}

		/**
		 * Sets {@link #latency} to specified value
		 * @param	latency
		 * 			Latency to use
		 */
		public void setLatency(int latency) {
			this.latency = latency;
		}

		/**
		 * Returns {@link #displayName}
		 * @return	displayName
		 */
		public IChatBaseComponent getDisplayName() {
			return displayName;
		}

		/**
		 * Sets {@link #displayName} to specified value
		 * @param	displayName
		 * 			Display name to use
		 */
		public void setDisplayName(IChatBaseComponent displayName) {
			this.displayName = displayName;
		}

		/**
		 * Returns {@link #uniqueId}
		 * @return	uniqueId
		 */
		public UUID getUniqueId() {
			return uniqueId;
		}

		/**
		 * Sets {@link #uniqueId} to specified value
		 * @param	uniqueId
		 * 			UUID to use
		 */
		public void setUniqueId(UUID uniqueId) {
			this.uniqueId = uniqueId;
		}

		/**
		 * Returns {@link #name}
		 * @return	name
		 */
		public String getName() {
			return name;
		}

		/**
		 * Sets {@link #name} to specified value
		 * @param	name
		 * 			name to use
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Returns {@link #gameMode}
		 * @return	gameMode
		 */
		public EnumGamemode getGameMode() {
			return gameMode;
		}

		/**
		 * Sets {@link #gameMode} to specified value
		 * @param	gameMode
		 * 			GameMode to use
		 */
		public void setGameMode(EnumGamemode gameMode) {
			this.gameMode = gameMode;
		}

		/**
		 * Returns {@link #skin}
		 * @return	skin
		 */
		public Object getSkin() {
			return skin;
		}

		/**
		 * Sets {@link #skin} to specified value
		 * @param	skin
		 * 			Skin to use
		 */
		public void setSkin(Object skin) {
			this.skin = skin;
		}
	}

	/**
	 * En enum representing packet action
	 * Calling ordinal() will return action's network ID.
	 */
	public enum EnumPlayerInfoAction {

		ADD_PLAYER,
		UPDATE_GAME_MODE,
		UPDATE_LATENCY,
		UPDATE_DISPLAY_NAME,
		REMOVE_PLAYER
	}

	/**
	 * An enum representing GameMode
	 */
	public enum EnumGamemode {

		NOT_SET,
		SURVIVAL,
		CREATIVE,
		ADVENTURE,
		SPECTATOR
	}
}