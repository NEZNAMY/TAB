package me.neznamy.tab.shared.packets;

import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutScoreboardScore extends UniversalPacketPlayOut {

	public Action action;
	public String objectiveName;
	public String player;
	public int score;

	public PacketPlayOutScoreboardScore(Action action, String objectiveName, String player, int score) {
		this.action = action;
		this.objectiveName = objectiveName;
		this.player = player;
		this.score = score;
	}

	@Override
	protected Object build(ProtocolVersion clientVersion) throws Exception {
		return builder.build(this, clientVersion);
	}

	public enum Action {

		CHANGE,
		REMOVE;
	}
}