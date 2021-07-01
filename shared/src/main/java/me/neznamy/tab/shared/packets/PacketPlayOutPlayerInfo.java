package me.neznamy.tab.shared.packets;

import java.lang.reflect.InvocationTargetException;
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
	 * @throws NegativeArraySizeException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	@Override
	protected Object build(ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException, NegativeArraySizeException {
		return TAB.getInstance().getPacketBuilder().build(this, clientVersion);
	}

	/**
	 * An override to toString() method for better output
	 */
	@Override
	public String toString() {
		return String.format("PacketPlayOutPlayerInfo{action=%s,entries=%s}", getAction(), getEntries());
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
			this.setName(name);
			this.setUniqueId(uniqueId);
			this.setSkin(skin);
			this.setLatency(latency);
			this.setGameMode(gameMode);
			this.setDisplayName(displayName);
		}
		
		/**
		 * Constructor perfect for UPDATE_GAME_MODE action
		 * @param uniqueId - player's uuid
		 * @param gameMode - player's gamemode
		 */
		public PlayerInfoData(UUID uniqueId, EnumGamemode gameMode) {
			this.setUniqueId(uniqueId);
			this.setGameMode(gameMode);
		}
		
		/**
		 * Constructor perfect for UPDATE_LATENCY action
		 * @param uniqueId - player's uuid
		 * @param latency - player's ping
		 */
		public PlayerInfoData(UUID uniqueId, int latency) {
			this.setUniqueId(uniqueId);
			this.setLatency(latency);
		}
		
		/**
		 * Constructor perfect for UPDATE_DISPLAY_NAME action
		 * @param uniqueId - player's uuid
		 * @param displayName - player's tablist name
		 */
		public PlayerInfoData(UUID uniqueId, IChatBaseComponent displayName) {
			this.setUniqueId(uniqueId);
			this.setDisplayName(displayName);
		}
		
		/**
		 * Constructor perfect for REMOVE_PLAYER action
		 * @param uniqueId - player's uuid
		 */
		public PlayerInfoData(UUID uniqueId) {
			this.setUniqueId(uniqueId);
		}

		/**
		 * Creates and returns a clone of this instance
		 */
		public PlayerInfoData clone() {
			return new PlayerInfoData(getName(), getUniqueId(), getSkin(), getLatency(), getGameMode(), getDisplayName());
		}

		/**
		 * An override to toString() method for better output
		 */
		@Override
		public String toString() {
			return String.format("PlayerInfoData{latency=%s,gameMode=%s,displayName=%s,name=%s,uniqueId=%s,skin=%s}",
					getLatency(), getGameMode(), getDisplayName(), getName(), getUniqueId(), getSkin());
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