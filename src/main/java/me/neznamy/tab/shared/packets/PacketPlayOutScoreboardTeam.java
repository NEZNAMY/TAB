package me.neznamy.tab.shared.packets;

import java.util.Collection;
import java.util.Collections;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutScoreboardTeam extends UniversalPacketPlayOut {

	//team name
	public String name;
	
	//team's display name - appears to be unused
//	public String displayName;
	
	//prefix of players in team
	public String playerPrefix;
	
	//suffix of players in team
	public String playerSuffix;
	
	//nametag visibility rule
	public String nametagVisibility;
	
	//collision rule
	public String collisionRule;
	
	//name and glow color, start color of prefix if not set
	public EnumChatFormat color;
	
	//affected entities
	//for players their name, for other entities their UUID
	public Collection<String> players = Collections.emptyList();
	
	//packet action
	public int method;
	
	//bitmask of team options
	public int options;

	/*
	 * Creates a new instance of the class
	 * Constructor is private, use one of the static methods to create an instance
	 */
	private PacketPlayOutScoreboardTeam(int method, String name) {
		if (name == null || name.length() == 0) throw new IllegalArgumentException("Team name cannot be null/empty");
		this.method = method;
		this.name = name;
	}

	/**
	 * Constructs new packet based on given parameters
	 * @param team - team name
	 * @param prefix - prefix of players in team
	 * @param suffix - suffix of players in team
	 * @param visibility - nametag visibility rule
	 * @param collision - collision rule
	 * @param players - affected entities
	 * @param options - bitmask of team options
	 * @return the instance with given parameters with CREATE action
	 */
	public PacketPlayOutScoreboardTeam(String team, String prefix, String suffix, String visibility, String collision, Collection<String> players, int options) {
		this(0, team);
		this.playerPrefix = prefix;
		this.playerSuffix = suffix;
		this.nametagVisibility = visibility;
		this.collisionRule = collision;
		this.players = players;
		this.options = options;
	}

	/**
	 * Constructs new packet based on given parameter
	 * @param team - team name
	 * @return the instance with given parameter with REMOVE action
	 */
	public PacketPlayOutScoreboardTeam(String team) {
		this(1, team);
	}

	/**
	 * Constructs new packet based on given parameters
	 * @param team - team name
	 * @param prefix - prefix of players in team
	 * @param suffix - suffix of players in team
	 * @param visibility - nametag visibility rule
	 * @param collision - collision rule
	 * @param options - bitmask of team options
	 * @return the instance with given parameters with UPDATE_TEAM_INFO action
	 */
	public PacketPlayOutScoreboardTeam(String team, String prefix, String suffix, String visibility, String collision, int options) {
		this(2, team);
		this.playerPrefix = prefix;
		this.playerSuffix = suffix;
		this.nametagVisibility = visibility;
		this.collisionRule = collision;
		this.options = options;
	}

	/**
	 * Constructs new packet based on given parameters
	 * @param team - team name
	 * @param players - affected entities
	 * @param add - true if players should be added into team, false if removed
	 * @return the instance with given parameters with ADD_PLAYERS action if add is true, REMOVE_PLAYERS if false
	 */
	public PacketPlayOutScoreboardTeam(String team, Collection<String> players, boolean add) {
		this(add ? 3 : 4, team);
		this.players = players;
	}

	/**
	 * Sets team options to given value
	 * @param options - options to set to
	 * @return self
	 */
	public PacketPlayOutScoreboardTeam setTeamOptions(int options) {
		this.options = options;
		return this;
	}

	/**
	 * Sets team color to given value
	 * @param color - color to use
	 * @return self
	 */
	public PacketPlayOutScoreboardTeam setColor(EnumChatFormat color) {
		this.color = color;
		return this;
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
		return String.format("PacketPlayOutScoreboardTeam{name=%s,playerPrefix=%s,playerSuffix=%s,nametagVisibility=%s,"
				+ "collisionRule=%s,color=%s,players=%s,method=%s,options=%s}",
				name, playerPrefix, playerSuffix, nametagVisibility, collisionRule, color, players, method, options);
	}
}