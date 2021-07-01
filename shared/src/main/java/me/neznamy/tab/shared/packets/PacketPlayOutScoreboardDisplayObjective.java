package me.neznamy.tab.shared.packets;

import java.lang.reflect.InvocationTargetException;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutScoreboardDisplayObjective extends UniversalPacketPlayOut {

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
		return String.format("PacketPlayOutScoreboardDisplayObjective{slot=%s,objectiveName=%s}", getSlot(), getObjectiveName());
	}

	public String getObjectiveName() {
		return objectiveName;
	}

	public int getSlot() {
		return slot;
	}
}