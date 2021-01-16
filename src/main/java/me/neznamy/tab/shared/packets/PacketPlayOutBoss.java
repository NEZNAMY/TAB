package me.neznamy.tab.shared.packets;

import java.util.UUID;

import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;

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
	public PacketPlayOutBoss(UUID id, String name, float pct, BarColor color, BarStyle overlay, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {
		this.operation = Action.ADD;
		this.id = id;
		this.name = name;
		this.pct = pct;
		this.color = color;
		this.overlay = overlay;
		this.darkenScreen = darkenScreen;
		this.playMusic = playMusic;
		this.createWorldFog = createWorldFog;
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
	public PacketPlayOutBoss(UUID id, String name, float pct, BarColor color, BarStyle overlay) {
		this.operation = Action.ADD;
		this.id = id;
		this.name = name;
		this.pct = pct;
		this.color = color;
		this.overlay = overlay;
	}

	/**
	 * Constructs new packet based on given parameters
	 * @param id - bossbar uuid
	 * @return the instance with given parameters with REMOVE action
	 */
	public PacketPlayOutBoss(UUID id) {
		this.operation = Action.REMOVE;
		this.id = id;
	}

	/**
	 * Constructs new packet based on given parameters
	 * @param id - bossbar uuid
	 * @param pct - bossbar progress
	 * @return the instance with given parameters with UPDATE_PCT action
	 */
	public PacketPlayOutBoss(UUID id, float pct) {
		this.operation = Action.UPDATE_PCT;
		this.id = id;
		this.pct = pct;
	}

	/**
	 * Constructs new packet based on given parameters
	 * @param id - bossbar uuid
	 * @param name - bossbar title
	 * @return the instance with given parameters with UPDATE_NAME action
	 */
	public PacketPlayOutBoss(UUID id, String name) {
		this.operation = Action.UPDATE_NAME;
		this.id = id;
		this.name = name;
	}

	/**
	 * Constructs new packet based on given parameters
	 * @param id - bossbar uuid
	 * @param color - bossbar color
	 * @param overlay - bossbar style
	 * @return the instance with given parameters with UPDATE_STYLE action
	 */
	public PacketPlayOutBoss(UUID id, BarColor color, BarStyle overlay) {
		this.operation = Action.UPDATE_STYLE;
		this.id = id;
		this.color = color;
		this.overlay = overlay;
	}

	/**
	 * Constructs new packet based on given parameters
	 * @param id - bossbar uuid
	 * @param darkenScreen - darker screen if bossbar is displayed
	 * @param playMusic - play boss music when bossbar is displayed
	 * @param createWorldFog - create fog if bossbar is displayed
	 * @return the instance with given parameters with UPDATE_PROPERTIES action
	 */
	public PacketPlayOutBoss(UUID id, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {
		this.operation = Action.UPDATE_PROPERTIES;
		this.id = id;
		this.darkenScreen = darkenScreen;
		this.playMusic = playMusic;
		this.createWorldFog = createWorldFog;
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
		return TAB.getInstance().getPacketBuilder().build(this, clientVersion);
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
}