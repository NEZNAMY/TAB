package me.neznamy.tab.api.protocol;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutScoreboardDisplayObjective implements CrossPlatformPacket {

	//display slot (0 = playerlist, 1 = sidebar, 2 = belowname)
	private int slot;
	
	//name of the objective
	private String objectiveName;

	/**
	 * Constructs a new instance with given parameters
	 * @param slot - display slot
	 * @param objectiveName - name of the objective
	 */
	public PacketPlayOutScoreboardDisplayObjective(int slot, String objectiveName) {
		this.slot = slot;
		this.objectiveName = objectiveName;
	}

	@Override
	public String toString() {
		return String.format("PacketPlayOutScoreboardDisplayObjective{slot=%s,objectiveName=%s}", slot, objectiveName);
	}

	public String getObjectiveName() {
		return objectiveName;
	}

	public int getSlot() {
		return slot;
	}
}