package me.neznamy.tab.shared.packets;

import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutScoreboardDisplayObjective extends UniversalPacketPlayOut {

	public int slot;
	public String objectiveName;

	public PacketPlayOutScoreboardDisplayObjective(int slot, String objectiveName) {
		this.slot = slot;
		this.objectiveName = objectiveName;
	}

	@Override
	protected Object build(ProtocolVersion clientVersion) throws Exception {
		return builder.build(this, clientVersion);
	}
}