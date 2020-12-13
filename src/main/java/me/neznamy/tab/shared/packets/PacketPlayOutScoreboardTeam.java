package me.neznamy.tab.shared.packets;

import java.util.Collection;
import java.util.Collections;

import me.neznamy.tab.shared.ProtocolVersion;

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
	
	//good question
	//either name or glow color
	//will investigate some day
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
	private PacketPlayOutScoreboardTeam() {
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
	public static PacketPlayOutScoreboardTeam CREATE(String team, String prefix, String suffix, String visibility, String collision, Collection<String> players, int options) {
		PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
		packet.method = 0;
		packet.name = team;
		packet.playerPrefix = prefix;
		packet.playerSuffix = suffix;
		packet.nametagVisibility = visibility;
		packet.collisionRule = collision;
		packet.players = players;
		packet.options = options;
		return packet;
	}

	/**
	 * Constructs new packet based on given parameter
	 * @param team - team name
	 * @return the instance with given parameter with REMOVE action
	 */
	public static PacketPlayOutScoreboardTeam REMOVE(String team) {
		PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
		packet.method = 1;
		packet.name = team;
		return packet;
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
	public static PacketPlayOutScoreboardTeam UPDATE_TEAM_INFO(String team, String prefix, String suffix, String visibility, String collision, int options) {
		PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
		packet.method = 2;
		packet.name = team;
		packet.playerPrefix = prefix;
		packet.playerSuffix = suffix;
		packet.nametagVisibility = visibility;
		packet.collisionRule = collision;
		packet.options = options;
		return packet;
	}

	/**
	 * Constructs new packet based on given parameters
	 * @param team - team name
	 * @param players - affected entities
	 * @return the instance with given parameters with ADD_PLAYERS action
	 */
	public static PacketPlayOutScoreboardTeam ADD_PLAYERS(String team, Collection<String> players) {
		PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
		packet.method = 3;
		packet.name = team;
		packet.players = players;
		return packet;
	}

	/**
	 * Constructs new packet based on given parameters
	 * @param team - team name
	 * @param players - affected entities
	 * @return the instance with given parameters with REMOVE_PLAYERS action
	 */
	public static PacketPlayOutScoreboardTeam REMOVE_PLAYERS(String team, Collection<String> players) {
		PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
		packet.method = 4;
		packet.name = team;
		packet.players = players;
		return packet;
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
		return builder.build(this, clientVersion);
	}
	
	/**
	 * An override to toString() method for better output
	 */
	@Override
	public String toString() {
		return "PacketPlayOutScoreboardTeam{name=" + name + ",playerPrefix=" + playerPrefix + ",playerSuffix=" + playerSuffix + 
				",nametagVisibility=" + nametagVisibility +",collisionRule=" + collisionRule + ",color=" + color + 
				",players=" + players + ",method=" + method + ",options=" + options + "}";
	}
}