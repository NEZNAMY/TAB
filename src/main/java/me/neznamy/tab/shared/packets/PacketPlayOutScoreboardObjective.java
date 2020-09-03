package me.neznamy.tab.shared.packets;

import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutScoreboardObjective extends UniversalPacketPlayOut {

	public String objectiveName;
	public String displayName;
	public EnumScoreboardHealthDisplay renderType;
	public int method;

	private PacketPlayOutScoreboardObjective() {
	}

	public static PacketPlayOutScoreboardObjective REGISTER(String objectiveName, String displayName, EnumScoreboardHealthDisplay renderType) {
		PacketPlayOutScoreboardObjective packet =  new PacketPlayOutScoreboardObjective();
		packet.objectiveName = objectiveName;
		packet.displayName = displayName;
		packet.renderType = renderType;
		packet.method = 0;
		return packet;
	}

	public static PacketPlayOutScoreboardObjective UNREGISTER(String objectiveName) {
		PacketPlayOutScoreboardObjective packet =  new PacketPlayOutScoreboardObjective();
		packet.objectiveName = objectiveName;
		packet.displayName = ""; //error on <1.7
		packet.method = 1;
		return packet;
	}

	public static PacketPlayOutScoreboardObjective UPDATE_TITLE(String objectiveName, String displayName, EnumScoreboardHealthDisplay renderType) {
		PacketPlayOutScoreboardObjective packet =  new PacketPlayOutScoreboardObjective();
		packet.objectiveName = objectiveName;
		packet.displayName = displayName;
		packet.renderType = renderType;
		packet.method = 2;
		return packet;
	}

	@Override
	protected Object build(ProtocolVersion clientVersion) throws Exception {
		return builder.build(this, clientVersion);
	}

	public enum EnumScoreboardHealthDisplay{

		INTEGER,
		HEARTS;
	}
}