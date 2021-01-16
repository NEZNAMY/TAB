package me.neznamy.tab.shared.packets;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutScoreboardObjective extends UniversalPacketPlayOut {

	//objective name
	public String objectiveName;
	
	//title
	public String displayName;
	
	//display type
	public EnumScoreboardHealthDisplay renderType;
	
	//action
	public int method;

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
		return "PacketPlayOutScoreboardObjective{objectiveName=" + objectiveName + ",displayName=" + displayName + 
				",renderType=" + renderType + ",method=" + method + "}";
	}
	
	/**
	 * An enum representing display types
	 */
	public enum EnumScoreboardHealthDisplay {

		INTEGER, HEARTS;
	}
}