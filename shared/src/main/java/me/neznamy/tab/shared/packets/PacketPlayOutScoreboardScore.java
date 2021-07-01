package me.neznamy.tab.shared.packets;

import java.lang.reflect.InvocationTargetException;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutScoreboardScore extends UniversalPacketPlayOut {

	//packet action
	private Action action;

	//objective name
	private String objectiveName;

	//affected player
	private String player;

	//player's score
	private int score;

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
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	@Override
	protected Object build(ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return TAB.getInstance().getPacketBuilder().build(this, clientVersion);
	}

	/**
	 * An override to toString() method for better output
	 */
	@Override
	public String toString() {
		return String.format("PacketPlayOutScoreboardScore{action=%s,objectiveName=%s,player=%s,score=%s}", 
				getAction(), getObjectiveName(), getPlayer(), getScore());
	}

	public String getPlayer() {
		return player;
	}

	public Action getAction() {
		return action;
	}

	public String getObjectiveName() {
		return objectiveName;
	}

	public int getScore() {
		return score;
	}

	/**
	 * An enum representing action types
	 */
	public enum Action {

		CHANGE,
		REMOVE;
	}
}