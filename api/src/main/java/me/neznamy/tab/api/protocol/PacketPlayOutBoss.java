package me.neznamy.tab.api.protocol;

import java.util.UUID;

import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutBoss implements TabPacket {

	/** UUID of the BossBar */
	private final UUID id;

	/** Action of this packet */
	private final Action operation;

	/** BossBar title */
	private String name;

	/** BossBar progress (0-1)*/
	private float pct;

	/** BossBar color */
	private BarColor color;

	/** BossBar style */
	private BarStyle overlay;

	/** Darken screen flag */
	private boolean darkenScreen;

	/** Play music flag */
	private boolean playMusic;

	/** Create fog flag */
	private boolean createWorldFog;

	/**
	 * Constructs new instance with given parameters and  
	 * {@link me.neznamy.tab.api.protocol.PacketPlayOutBoss.Action}.ADD action
	 * 
	 * @param	id
	 * 			BossBar uuid
	 * @param	name
	 * 			BossBar title
	 * @param	pct
	 * 			BossBar progress
	 * @param	color
	 * 			BossBar color
	 * @param	overlay
	 * 			BossBar style
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
	 * Constructs new instance with given parameters and  
	 * {@link me.neznamy.tab.api.protocol.PacketPlayOutBoss.Action}.REMOVE action
	 * 
	 * @param	id
	 * 			BossBar uuid
	 */
	public PacketPlayOutBoss(UUID id) {
		this.operation = Action.REMOVE;
		this.id = id;
	}

	/**
	 * Constructs new instance with given parameters and  
	 * {@link me.neznamy.tab.api.protocol.PacketPlayOutBoss.Action}.UPDATE_PCT action
	 * 
	 * @param	id
	 * 			BossBar uuid
	 * @param	pct
	 * 			BossBar progress
	 */
	public PacketPlayOutBoss(UUID id, float pct) {
		this.operation = Action.UPDATE_PCT;
		this.id = id;
		this.pct = pct;
	}

	/**
	 * Constructs new instance with given parameters and  
	 * {@link me.neznamy.tab.api.protocol.PacketPlayOutBoss.Action}.UPDATE_NAME action
	 * 
	 * @param	id
	 * 			BossBar uuid
	 * @param	name
	 * 			BossBar title
	 */
	public PacketPlayOutBoss(UUID id, String name) {
		this.operation = Action.UPDATE_NAME;
		this.id = id;
		this.name = name;
	}

	/**
	 * Constructs new instance with given parameters and  
	 * {@link me.neznamy.tab.api.protocol.PacketPlayOutBoss.Action}.UPDATE_STYLE action
	 * 
	 * @param	id
	 * 			BossBar uuid
	 * @param	color
	 * 			BossBar color
	 * @param	overlay
	 * 			BossBar style
	 */
	public PacketPlayOutBoss(UUID id, BarColor color, BarStyle overlay) {
		this.operation = Action.UPDATE_STYLE;
		this.id = id;
		this.color = color;
		this.overlay = overlay;
	}

	/**
	 * Constructs new instance with given parameters and  
	 * {@link me.neznamy.tab.api.protocol.PacketPlayOutBoss.Action}.UPDATE_PROPERTIES action
	 * 
	 * @param	darkenScreen
	 * 			Darken screen flag
	 * @param	playMusic
	 * 			Play music flag
	 * @param	createWorldFog
	 * 			Create fog flag
	 */
	public PacketPlayOutBoss(UUID id, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {
		this.operation = Action.UPDATE_PROPERTIES;
		this.id = id;
		this.darkenScreen = darkenScreen;
		this.playMusic = playMusic;
		this.createWorldFog = createWorldFog;
	}

	/**
	 * Returns bitmask based on {@link #darkenScreen}, {@link #playMusic} and {@link #darkenScreen} values.
	 * <p>
	 * {@link #darkenScreen} adds {@code 1}, {@link #playMusic} {@code 2} and {@link #darkenScreen} {@code 4}
	 * to the final value.
	 * 
	 * @return the bitmask
	 */
	public byte getFlags(){
		byte value = 0;
		if (darkenScreen) value += 1;
		if (playMusic) value += 2;
		if (createWorldFog) value += 4;
		return value;
	}

	@Override
	public String toString() {
		return String.format("PacketPlayOutBoss{id=%s,operation=%s,name=%s,pct=%s,color=%s,overlay=%s,darkenScreen=%s,playMusic=%s,createWorldFog=%s}", 
				id, operation, name, pct, color, overlay, darkenScreen, playMusic, createWorldFog);
	}

	/**
	 * Returns {@link #color}
	 * @return	color
	 */
	public BarColor getColor() {
		return color;
	}

	/**
	 * Returns {@link #overlay}
	 * @return	style
	 */
	public BarStyle getOverlay() {
		return overlay;
	}

	/**
	 * Returns {@link #name}
	 * @return	name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns {@link #id}
	 * @return	id
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * Returns {@link #pct}
	 * @return	progress
	 */
	public float getPct() {
		return pct;
	}

	/**
	 * Returns {@link #operation}
	 * @return	packet action
	 */
	public Action getOperation() {
		return operation;
	}

	/**
	 * Returns {@link #darkenScreen}
	 * @return	darkenScreen
	 */
	public boolean isDarkenScreen() {
		return darkenScreen;
	}

	/**
	 * Returns {@link #createWorldFog}
	 * @return	createWorldFog
	 */
	public boolean isCreateWorldFog() {
		return createWorldFog;
	}

	/**
	 * Returns {@link #playMusic}
	 * @return	playMusic
	 */
	public boolean isPlayMusic() {
		return playMusic;
	}

	/**
	 * Sets {@link #darkenScreen} to specified value
	 * 
	 * @param	darkenScreen
	 * 			Darken screen flag
	 */
	public void setDarkenScreen(boolean darkenScreen) {
		this.darkenScreen = darkenScreen;
	}

	/**
	 * Sets {@link #createWorldFog} to specified value
	 * 
	 * @param	createWorldFog
	 * 			Create fog flag
	 */
	public void setCreateWorldFog(boolean createWorldFog) {
		this.createWorldFog = createWorldFog;
	}

	/**
	 * Sets {@link #playMusic} to specified value
	 * 
	 * @param	playMusic
	 * 			Play music flag
	 */
	public void setPlayMusic(boolean playMusic) {
		this.playMusic = playMusic;
	}

	/**
	 * An enum representing all valid boss packet actions.
	 * Calling ordinal() will return action's network ID.
	 */
	public enum Action {

		ADD,
		REMOVE,
		UPDATE_PCT,
		UPDATE_NAME,
		UPDATE_STYLE,
		UPDATE_PROPERTIES
	}
}