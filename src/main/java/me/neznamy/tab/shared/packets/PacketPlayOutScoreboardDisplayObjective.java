package me.neznamy.tab.shared.packets;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutScoreboardDisplayObjective extends UniversalPacketPlayOut {

	//display slot (0 = playerlist, 1 = sidebar, 2 = belowname)
	public int slot;
	
	//name of the objective
	public String objectiveName;

	/**
	 * Constructs a new instance with given parameters
	 * @param slot - display slot
	 * @param objectiveName - name of the objective
	 */
	public PacketPlayOutScoreboardDisplayObjective(int slot, String objectiveName) {
		this.slot = slot;
		this.objectiveName = objectiveName;
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
		return String.format("PacketPlayOutScoreboardDisplayObjective{slot=%s,objectiveName=%s}", slot, objectiveName);
	}
}