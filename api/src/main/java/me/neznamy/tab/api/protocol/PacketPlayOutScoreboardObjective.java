package me.neznamy.tab.api.protocol;

import me.neznamy.tab.api.util.Preconditions;

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
	private final int action;

	/**
	 * Constructs new instance with given parameters.
	 * 
	 * @param	action
	 * 			Packet action (0 = add, 1 = remove, 2 = update title)
	 * @param	objectiveName
	 * 			objective name, up to 16 characters long
	 * @param displayName - title
	 * @param renderType - display type
	 * @throws	IllegalArgumentException
	 * 			if {@code objectiveName} is null or longer than 16 characters
	 */
	public PacketPlayOutScoreboardObjective(int action, String objectiveName, String displayName, EnumScoreboardHealthDisplay renderType) {
		Preconditions.checkRange(action, 0, 2, "action");
		Preconditions.checkNotNull(objectiveName, "objective name");
		Preconditions.checkMaxLength(objectiveName, 16, "objective name");
		Preconditions.checkNotNull(renderType, "render type");
		this.objectiveName = objectiveName;
		this.displayName = displayName;
		this.renderType = renderType;
		this.action = action;
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
		Preconditions.checkNotNull(objectiveName, "objective name");
		Preconditions.checkMaxLength(objectiveName, 16, "objective name");
		this.objectiveName = objectiveName;
		this.displayName = ""; //avoiding NPE on <1.7
		this.action = 1;
		this.renderType = null;
	}

	@Override
	public String toString() {
		return String.format("PacketPlayOutScoreboardObjective{objectiveName=%s,displayName=%s,renderType=%s,method=%s}",
				objectiveName, displayName, renderType, action);
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
	 * Returns {@link #action}
	 * @return	packet action
	 */
	public int getAction() {
		return action;
	}

	/**
	 * An enum representing available display types.
	 * They only take effect in PlayerList position.
	 */
	public enum EnumScoreboardHealthDisplay {

		INTEGER, HEARTS
	}
}