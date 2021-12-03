package me.neznamy.tab.api.protocol;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutScoreboardObjective implements TabPacket {

	/** Up to 16 characters long objective name */
	private final String objectiveName;

	/** Display name */
	private final String displayName;

	/** Display type, only takes effect in PlayerList */
	private final EnumScoreboardHealthDisplay renderType;

	/**
	 * Packet action.
	 * 0 = register,
	 * 1 = unregister,
	 * 2 = update title
	 */
	private final int method;

	/**
	 * Constructs new instance with given parameters.
	 * 
	 * @param	method
	 * 			Packet action (0 = add, 1 = remove, 2 = update title)
	 * @param	objectiveName
	 * 			objective name, up to 16 characters long
	 * @param displayName - title
	 * @param renderType - display type
	 * @throws	IllegalArgumentException
	 * 			if {@code objectiveName} is null or longer than 16 characters
	 */
	public PacketPlayOutScoreboardObjective(int method, String objectiveName, String displayName, EnumScoreboardHealthDisplay renderType) {
		if (objectiveName == null) throw new IllegalArgumentException("objectiveName cannot be null");
		if (objectiveName.length() > 16) throw new IllegalArgumentException("objectiveName cannot be longer than 16 character (is " + objectiveName.length() + ")");
		this.objectiveName = objectiveName;
		this.displayName = displayName;
		this.renderType = renderType;
		this.method = method;
	}

	/**
	 * Constructs new packet with given objective name and 1 (unregister) action.
	 * 
	 * @param	objectiveName
	 * 			objective name, up to 16 characters long
	 * @throws	IllegalArgumentException
	 * 			if {@code objectiveName} is null or longer than 16 characters
	 */
	public PacketPlayOutScoreboardObjective(String objectiveName) {
		if (objectiveName == null) throw new IllegalArgumentException("objectiveName cannot be null");
		if (objectiveName.length() > 16) throw new IllegalArgumentException("objectiveName cannot be longer than 16 character (is " + objectiveName.length() + ")");
		this.objectiveName = objectiveName;
		this.displayName = ""; //avoiding NPE on <1.7
		this.method = 1;
		this.renderType = null;
	}

	@Override
	public String toString() {
		return String.format("PacketPlayOutScoreboardObjective{objectiveName=%s,displayName=%s,renderType=%s,method=%s}",
				objectiveName, displayName, renderType, method);
	}

	/**
	 * Returns {@link #displayName}
	 * @return	displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Returns {@link #objectiveName}
	 * @return	objectiveName
	 */
	public String getObjectiveName() {
		return objectiveName;
	}

	/**
	 * Returns {@link #renderType}
	 * @return	renderType
	 */
	public EnumScoreboardHealthDisplay getRenderType() {
		return renderType;
	}

	/**
	 * Returns {@link #method}
	 * @return	packet action
	 */
	public int getMethod() {
		return method;
	}

	/**
	 * An enum representing available display types.
	 * They only take effect in PlayerList position.
	 */
	public enum EnumScoreboardHealthDisplay {

		INTEGER, HEARTS
	}
}