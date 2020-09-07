package me.neznamy.tab.shared.packets;

import java.util.UUID;

import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutBoss extends UniversalPacketPlayOut {

	//bossbar's uuid
	public UUID id;
	
	//packet action
	public Action operation;
	
	//bossbar title
	public String name;
	
	//bossbar progress
	public float pct;
	
	//bossbar color
	public BarColor color;
	
	//bossbar style
	public BarStyle overlay;
	
	//darker screen if bossbar is displayed
	public boolean darkenScreen;
	
	//play boss music when bossbar is displayed
	public boolean playMusic;
	
	//create fog if bossbar is displayed
	public boolean createWorldFog;

	/*
	 * Creates a new instance of the class
	 * Constructor is private, use one of the static methods to create an instance
	 */
	private PacketPlayOutBoss() {
	}

	/**
	 * Constructs new packet based on given parameters
	 * @param id - bossbar uuid
	 * @param name - bossbar title
	 * @param pct - bossbar progress
	 * @param color - bossbar color
	 * @param overlay - bossbar style
	 * @param darkenScreen - darker screen if bossbar is displayed
	 * @param playMusic - play boss music when bossbar is displayed
	 * @param createWorldFog - create fog if bossbar is displayed
	 * @return the instance with given parameters with ADD action
	 */
	public static PacketPlayOutBoss ADD(UUID id, String name, float pct, BarColor color, BarStyle overlay, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {
		PacketPlayOutBoss packet = new PacketPlayOutBoss();
		packet.operation = Action.ADD;
		packet.id = id;
		packet.name = name;
		packet.pct = pct;
		packet.color = color;
		packet.overlay = overlay;
		packet.darkenScreen = darkenScreen;
		packet.playMusic = playMusic;
		packet.createWorldFog = createWorldFog;
		return packet;
	}

	/**
	 * Constructs new packet based on given parameters
	 * @param id - bossbar uuid
	 * @param name - bossbar title
	 * @param pct - bossbar progress
	 * @param color - bossbar color
	 * @param overlay - bossbar style
	 * @return the instance with given parameters with ADD action
	 */
	public static PacketPlayOutBoss ADD(UUID id, String name, float pct, BarColor color, BarStyle overlay) {
		PacketPlayOutBoss packet = new PacketPlayOutBoss();
		packet.operation = Action.ADD;
		packet.id = id;
		packet.name = name;
		packet.pct = pct;
		packet.color = color;
		packet.overlay = overlay;
		return packet;
	}

	/**
	 * Constructs new packet based on given parameters
	 * @param id - bossbar uuid
	 * @return the instance with given parameters with REMOVE action
	 */
	public static PacketPlayOutBoss REMOVE(UUID id) {
		PacketPlayOutBoss packet = new PacketPlayOutBoss();
		packet.operation = Action.REMOVE;
		packet.id = id;
		return packet;
	}

	/**
	 * Constructs new packet based on given parameters
	 * @param id - bossbar uuid
	 * @param pct - bossbar progress
	 * @return the instance with given parameters with UPDATE_PCT action
	 */
	public static PacketPlayOutBoss UPDATE_PCT(UUID id, float pct) {
		PacketPlayOutBoss packet = new PacketPlayOutBoss();
		packet.operation = Action.UPDATE_PCT;
		packet.id = id;
		packet.pct = pct;
		return packet;
	}

	/**
	 * Constructs new packet based on given parameters
	 * @param id - bossbar uuid
	 * @param name - bossbar title
	 * @return the instance with given parameters with UPDATE_NAME action
	 */
	public static PacketPlayOutBoss UPDATE_NAME(UUID id, String name) {
		PacketPlayOutBoss packet = new PacketPlayOutBoss();
		packet.operation = Action.UPDATE_NAME;
		packet.id = id;
		packet.name = name;
		return packet;
	}

	/**
	 * Constructs new packet based on given parameters
	 * @param id - bossbar uuid
	 * @param color - bossbar color
	 * @param overlay - bossbar style
	 * @return the instance with given parameters with UPDATE_STYLE action
	 */
	public static PacketPlayOutBoss UPDATE_STYLE(UUID id, BarColor color, BarStyle overlay) {
		PacketPlayOutBoss packet = new PacketPlayOutBoss();
		packet.operation = Action.UPDATE_STYLE;
		packet.id = id;
		packet.color = color;
		packet.overlay = overlay;
		return packet;
	}

	/**
	 * Constructs new packet based on given parameters
	 * @param id - bossbar uuid
	 * @param darkenScreen - darker screen if bossbar is displayed
	 * @param playMusic - play boss music when bossbar is displayed
	 * @param createWorldFog - create fog if bossbar is displayed
	 * @return the instance with given parameters with UPDATE_PROPERTIES action
	 */
	public static PacketPlayOutBoss UPDATE_PROPERTIES(UUID id, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {
		PacketPlayOutBoss packet = new PacketPlayOutBoss();
		packet.operation = Action.UPDATE_PROPERTIES;
		packet.id = id;
		packet.darkenScreen = darkenScreen;
		packet.playMusic = playMusic;
		packet.createWorldFog = createWorldFog;
		return packet;
	}

	/**
	 * Returns bitmask based on darkenScreen, playMusic and createWorldFog values
	 * @return the bitmask
	 */
	public byte getFlags(){
		byte value = 0;
		if (darkenScreen) value += 1;
		if (playMusic) value += 2;
		if (createWorldFog) value += 4;
		return value;
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
		return "PacketPlayOutBoss{id=" + id + ",operation=" + operation + ",name=" + name + ",pct=" + pct +",color=" + color + ",overlay=" + overlay
				+ ",darkenScreen=" + darkenScreen + ",playMusic=" + playMusic + ",createWorldFog=" + createWorldFog + "}";
	}

	/**
	 * An enum representing boss packet action
	 */
	public enum Action {

		ADD,
		REMOVE,
		UPDATE_PCT,
		UPDATE_NAME,
		UPDATE_STYLE,
		UPDATE_PROPERTIES;
	}

	/**
	 * An enum representing bossbar colors
	 */
	public enum BarColor {

		PINK,
		BLUE,
		RED,
		GREEN,
		YELLOW,
		PURPLE,
		WHITE;
	}

	/**
	 * An enum representing bossbar styles using same names as NMS
	 */
	public enum BarStyle {

		PROGRESS,
		NOTCHED_6,
		NOTCHED_10,
		NOTCHED_12,
		NOTCHED_20;
	}
}