package me.neznamy.tab.shared.packets;

import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutScoreboardScore extends UniversalPacketPlayOut {

	//packet action
	public Action action;

	//objective name
	public String objectiveName;

	//affected player
	public String player;

	//player's score
	public int score;

	/**
	 * Constructs a new instance with given parameters
	 * @param action - packet action
	 * @param objectiveName - objective name
	 * @param player - affected player
	 * @param score - player's score
	 */
	public PacketPlayOutScoreboardScore(Action action, String objectiveName, String player, int score) {
		this.action = action;
		this.objectiveName = objectiveName;
		this.player = player;
		this.score = score;
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
		return "PacketPlayOutScoreboardScore{action=" + action + ",objectiveName=" + objectiveName + 
				",player=" + player + ",score=" + score + "}";
	}

	/**
	 * An enum representing action types
	 */
	public enum Action {

		CHANGE,
		REMOVE;
	}
}