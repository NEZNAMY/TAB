package me.neznamy.tab.shared.packets;

import me.neznamy.tab.shared.ProtocolVersion;

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

	/*
	 * Creates a new instance of the class
	 * Constructor is private, use one of the static methods to create an instance
	 */
	private PacketPlayOutScoreboardObjective() {
	}

	/**
	 * Constructs new packet based on given parameters
	 * @param objectiveName - objective name
	 * @param displayName - title
	 * @param renderType - display type
	 * @return the instance with given parameters with REGISTER action
	 */
	public static PacketPlayOutScoreboardObjective REGISTER(String objectiveName, String displayName, EnumScoreboardHealthDisplay renderType) {
		PacketPlayOutScoreboardObjective packet =  new PacketPlayOutScoreboardObjective();
		packet.objectiveName = objectiveName;
		packet.displayName = displayName;
		packet.renderType = renderType;
		packet.method = 0;
		return packet;
	}

	/**
	 * Constructs new packet based on given parameter
	 * @param objectiveName - objective name
	 * @return the instance with given parameter with UNREGISTER action
	 */
	public static PacketPlayOutScoreboardObjective UNREGISTER(String objectiveName) {
		PacketPlayOutScoreboardObjective packet =  new PacketPlayOutScoreboardObjective();
		packet.objectiveName = objectiveName;
		packet.displayName = ""; //avoiding NPE on <1.7
		packet.method = 1;
		return packet;
	}

	/**
	 * Constructs new packet based on given parameters
	 * @param objectiveName - objective name
	 * @param displayName - title
	 * @param renderType - display type
	 * @return the instance with given parameters with UPDATE_TITLE action
	 */
	public static PacketPlayOutScoreboardObjective UPDATE_TITLE(String objectiveName, String displayName, EnumScoreboardHealthDisplay renderType) {
		PacketPlayOutScoreboardObjective packet =  new PacketPlayOutScoreboardObjective();
		packet.objectiveName = objectiveName;
		packet.displayName = displayName;
		packet.renderType = renderType;
		packet.method = 2;
		return packet;
	}

	/**
	 * Calls build method of packet builder instance and returns output
	 */
	@Override
	protected Object build(ProtocolVersion clientVersion) throws Exception {
		return builder.build(this, clientVersion);
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

		INTEGER,
		HEARTS;
	}
}