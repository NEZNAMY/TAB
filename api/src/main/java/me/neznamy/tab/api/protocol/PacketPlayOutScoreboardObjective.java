package me.neznamy.tab.api.protocol;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutScoreboardObjective implements CrossPlatformPacket {

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

	@Override
	public String toString() {
		return String.format("PacketPlayOutScoreboardObjective{objectiveName=%s,displayName=%s,renderType=%s,method=%s}",
				objectiveName, displayName, renderType, method);
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