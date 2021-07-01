package me.neznamy.tab.shared.packets;

import java.lang.reflect.InvocationTargetException;
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
	private UUID id;
	
	//packet action
	private Action operation;
	
	//bossbar title
	private String name;
	
	//bossbar progress
	private float pct;
	
	//bossbar color
	private BarColor color;
	
	//bossbar style
	private BarStyle overlay;
	
	//darker screen if bossbar is displayed
	private boolean darkenScreen;
	
	//play boss music when bossbar is displayed
	private boolean playMusic;
	
	//create fog if bossbar is displayed
	private boolean createWorldFog;

	/**
	 * Constructs new packet based on given parameters with ADD action
	 * @param id - bossbar uuid
	 * @param name - bossbar title
	 * @param pct - bossbar progress
	 * @param color - bossbar color
	 * @param overlay - bossbar style
	 * @param darkenScreen - darker screen if bossbar is displayed
	 * @param playMusic - play boss music when bossbar is displayed
	 * @param createWorldFog - create fog if bossbar is displayed
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
	 * Constructs new packet based on given parameters with ADD action
	 * @param id - bossbar uuid
	 * @param name - bossbar title
	 * @param pct - bossbar progress
	 * @param color - bossbar color
	 * @param overlay - bossbar style
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
	 * Constructs new packet based on given parameters with REMOVE action
	 * @param id - bossbar uuid
	 */
	public PacketPlayOutBoss(UUID id) {
		this.operation = Action.REMOVE;
		this.id = id;
	}

	/**
	 * Constructs new packet based on given parameters UPDATE_PCT action
	 * @param id - bossbar uuid
	 * @param pct - bossbar progress
	 */
	public PacketPlayOutBoss(UUID id, float pct) {
		this.operation = Action.UPDATE_PCT;
		this.id = id;
		this.pct = pct;
	}

	/**
	 * Constructs new packet based on given parameters UPDATE_NAME action
	 * @param id - bossbar uuid
	 * @param name - bossbar title
	 */
	public PacketPlayOutBoss(UUID id, String name) {
		this.operation = Action.UPDATE_NAME;
		this.id = id;
		this.name = name;
	}

	/**
	 * Constructs new packet based on given parameters UPDATE_STYLE action
	 * @param id - bossbar uuid
	 * @param color - bossbar color
	 * @param overlay - bossbar style
	 */
	public PacketPlayOutBoss(UUID id, BarColor color, BarStyle overlay) {
		this.operation = Action.UPDATE_STYLE;
		this.id = id;
		this.color = color;
		this.overlay = overlay;
	}

	/**
	 * Constructs new packet based on given parameters with UPDATE_PROPERTIES action
	 * @param id - bossbar uuid
	 * @param darkenScreen - darker screen if bossbar is displayed
	 * @param playMusic - play boss music when bossbar is displayed
	 * @param createWorldFog - create fog if bossbar is displayed
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
		if (isDarkenScreen()) value += 1;
		if (isPlayMusic()) value += 2;
		if (isCreateWorldFog()) value += 4;
		return value;
	}

	/**
	 * Calls build method of packet builder instance and returns output
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	@Override
	protected Object build(ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		return TAB.getInstance().getPacketBuilder().build(this, clientVersion);
	}
	
	/**
	 * An override to toString() method for better output
	 */
	@Override
	public String toString() {
		return String.format("PacketPlayOutBoss{id=%s,operation=%s,name=%s,pct=%s,color=%s,overlay=%s,darkenScreen=%s,playMusic=%s,createWorldFog=%s}", 
				getId(), getOperation(), getName(), getPct(), getColor(), getOverlay(), isDarkenScreen(), isPlayMusic(), isCreateWorldFog());
	}

	public BarColor getColor() {
		return color;
	}

	public BarStyle getOverlay() {
		return overlay;
	}

	public String getName() {
		return name;
	}

	public UUID getId() {
		return id;
	}

	public float getPct() {
		return pct;
	}

	public Action getOperation() {
		return operation;
	}

	public boolean isDarkenScreen() {
		return darkenScreen;
	}

	public boolean isCreateWorldFog() {
		return createWorldFog;
	}

	public boolean isPlayMusic() {
		return playMusic;
	}

	/**
	 * An enum representing all valid boss packet actions
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