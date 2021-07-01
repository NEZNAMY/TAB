package me.neznamy.tab.shared.packets;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutScoreboardTeam extends UniversalPacketPlayOut {

	//team name, limited to 16 characters, used for sorting in tablist alphabetically
	private String name;
	
	//prefix of players in team
	private String playerPrefix;
	
	//suffix of players in team
	private String playerSuffix;
	
	//nametag visibility rule, possible options are: always, hideForOtherTeams, hideForOwnTeam, never
	private String nametagVisibility;
	
	//collision rule, possible options are: always, pushOtherTeams, pushOwnTeam, never
	private String collisionRule;
	
	//name and glow color, start color of prefix if not set
	private EnumChatFormat color;
	
	//affected entities
	//for players their name, for other entities their UUID
	private Collection<String> players = Collections.emptyList();
	
	//packet action, 0 = create, 1 = remove, 2 = update team info, 3 = add entries, 4 = remove entries
	private int method;
	
	//Bit mask. 0x01: Allow friendly fire, 0x02: can see invisible players on same team.
	private int options;

	/*
	 * Constructs new instance with given parameters
	 */
	private PacketPlayOutScoreboardTeam(int method, String name) {
		if (name == null || name.length() == 0) throw new IllegalArgumentException("Team name cannot be null/empty");
		this.method = method;
		this.name = name;
	}

	/**
	 * Constructs new instance with given parameters and 0 (CREATE) action
	 * @param team - team name
	 * @param prefix - prefix of players in team
	 * @param suffix - suffix of players in team
	 * @param visibility - nametag visibility rule
	 * @param collision - collision rule
	 * @param players - affected entities
	 * @param options - bitmask of team options
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
	 * Constructs new instance with given parameters and 1 (REMOVE) action
	 * @param team - team name
	 */
	public PacketPlayOutScoreboardTeam(String team) {
		this(1, team);
	}

	/**
	 * Constructs new instance with given parameters and 2 (UPDATE_TEAM_INFO) action
	 * @param team - team name
	 * @param prefix - prefix of players in team
	 * @param suffix - suffix of players in team
	 * @param visibility - nametag visibility rule
	 * @param collision - collision rule
	 * @param options - bitmask of team options
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
	 * Constructs new instance with given parameters and 3 (ADD_PLAYERS) if add is true, or 4 (REMOVE_PLAYERS) action
	 * if add is false
	 * @param team - team name
	 * @param players - affected entities
	 * @param add - true if players should be added into team, false if removed
	 */
	public PacketPlayOutScoreboardTeam(String team, Collection<String> players, boolean add) {
		this(add ? 3 : 4, team);
		this.players = players;
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
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InstantiationException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	@Override
	protected Object build(ProtocolVersion clientVersion) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, SecurityException {
		return TAB.getInstance().getPacketBuilder().build(this, clientVersion);
	}
	
	/**
	 * An override to toString() method for better output
	 */
	@Override
	public String toString() {
		return String.format("PacketPlayOutScoreboardTeam{name=%s,playerPrefix=%s,playerSuffix=%s,nametagVisibility=%s,"
				+ "collisionRule=%s,color=%s,players=%s,method=%s,options=%s}",
				getName(), getPlayerPrefix(), getPlayerSuffix(), getNametagVisibility(), getCollisionRule(), getColor(), getPlayers(), getMethod(), getOptions());
	}

	public EnumChatFormat getColor() {
		return color;
	}

	public String getPlayerPrefix() {
		return playerPrefix;
	}

	public String getName() {
		return name;
	}

	public int getMethod() {
		return method;
	}

	public String getNametagVisibility() {
		return nametagVisibility;
	}

	public int getOptions() {
		return options;
	}

	public Collection<String> getPlayers() {
		return players;
	}

	public String getCollisionRule() {
		return collisionRule;
	}

	public String getPlayerSuffix() {
		return playerSuffix;
	}
}