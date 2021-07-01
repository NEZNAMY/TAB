package me.neznamy.tab.shared.packets;

import java.lang.reflect.InvocationTargetException;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutScoreboardObjective extends UniversalPacketPlayOut {

	//objective name
	private String objectiveName;
	
	//title
	private String displayName;
	
	//display type
	private EnumScoreboardHealthDisplay renderType;
	
	//action
	private int method;

	/**
	 * Constructs new packet based on given parameters
	 * @param method - packet action (0 = add, 1 = remove, 2 = update title)
	 * @param objectiveName - objective name
	 * @param displayName - title
	 * @param renderType - display type
	 * @return the instance with given parameters
	 */
	public PacketPlayOutScoreboardObjective(int method, String objectiveName, String displayName, EnumScoreboardHealthDisplay renderType) {
		this.objectiveName = objectiveName;
		this.displayName = displayName;
		this.renderType = renderType;
		this.method = method;
	}

	/**
	 * Constructs new packet based on given parameter
	 * @param objectiveName - objective name
	 * @return the instance with given parameter with UNREGISTER action
	 */
	public PacketPlayOutScoreboardObjective(String objectiveName) {
		this.objectiveName = objectiveName;
		this.displayName = ""; //avoiding NPE on <1.7
		this.method = 1;
	}

	/**
	 * Calls build method of packet builder instance and returns output
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	@Override
	protected Object build(ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return TAB.getInstance().getPacketBuilder().build(this, clientVersion);
	}
	
	/**
	 * An override to toString() method for better output
	 */
	@Override
	public String toString() {
		return String.format("PacketPlayOutScoreboardObjective{objectiveName=%s,displayName=%s,renderType=%s,method=%s}",
				getObjectiveName(), getDisplayName(), getRenderType(), getMethod());
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public String getObjectiveName() {
		return objectiveName;
	}

	public EnumScoreboardHealthDisplay getRenderType() {
		return renderType;
	}

	public int getMethod() {
		return method;
	}

	/**
	 * An enum representing display types
	 */
	public enum EnumScoreboardHealthDisplay {

		INTEGER, HEARTS;
	}
}