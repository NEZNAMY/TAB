package me.neznamy.tab.api.protocol;

import java.util.Collection;
import java.util.Collections;

import me.neznamy.tab.api.chat.EnumChatFormat;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutScoreboardTeam implements TabPacket {

	/**
	 * Up to 16 characters long team name.
	 * It is used to sort players alphabetically in TabList by team names.
	 * Team-less players appear on top, sorted by usernames.
	 * Players with teams are below them, players of the same team are
	 * sorted alphabetically by usernames.
	 */
	private final String name;

	/** Team prefix, limited to 16 characters, on <1.13 */
	private String playerPrefix;

	/** Team suffix, limited to 16 characters, on <1.13 */
	private String playerSuffix;

	/** 
	 * NameTag visibility rule since 1.8. Possible options are:
	 * {@code always}, {@code hideForOtherTeams}, {@code hideForOwnTeam}, {@code never}
	 */
	private String nameTagVisibility;

	/**
	 * Collision rule added in 1.9. Possible options are:
	 * {@code always}, {@code pushOtherTeams}, {@code pushOwnTeam}, {@code never}
	 */
	private String collisionRule;

	/**
	 * Team color field added in 1.13. It determines name color,
	 * prefix and suffix color start.
	 */
	private EnumChatFormat color;

	/** 
	 * Affected entities. For players, it is their name, for other entities their UUID
	 * Player name length is limited to 16 characters for <1.8, 40 characters since 1.8.
	 * Entity UUID option was added in 1.8.
	 */
	private Collection<String> players = Collections.emptyList();

	/**
	 * Packet action.
	 * 0 = create team,
	 * 1 = remove team,
	 * 2 = update team info,
	 * 3 = add entries,
	 * 4 = remove entries
	 */
	private final int method;

	/**
	 * Bit mask.
	 * 0x01: Allow friendly fire,
	 * 0x02: Can see invisible players on the same team.
	 */
	private int options;

	/**
	 * Constructs new instance with given parameters. Private constructor used
	 * internally to validate team name and set action.
	 * 
	 * @param	method
	 * 			Packet action (0 = create team, 1 = remove team, 2 = update team info,
	 * 			3 = add entries, 4 = remove entries)
	 * @param	name
	 * 			Team name, up to 16 characters long
	 * @throws	IllegalArgumentException
	 * 			if {@code name} is null, empty or longer than 16 characters
	 */
	private PacketPlayOutScoreboardTeam(int method, String name) {
		if (name == null || name.length() == 0) throw new IllegalArgumentException("Team name cannot be null/empty");
		if (name.length() > 16) throw new IllegalArgumentException("name cannot be longer than 16 character (is " + name.length() + ")");
		this.method = method;
		this.name = name;
	}

	/**
	 * Constructs new instance with given parameters and 0 (CREATE) action
	 * 
	 * @param	team
	 * 			Team name, up to 16 characters long
	 * @param	prefix
	 * 			Prefix of players in team
	 * @param	suffix
	 * 			Suffix of players in team
	 * @param	visibility
	 * 			NameTag visibility rule
	 * @param	collision
	 * 			Collision rule
	 * @param	players
	 * 			Affected entities
	 * @param	options
	 * 			bitmask of team options
	 * @throws	IllegalArgumentException
	 * 			if {@code name} is null, empty or longer than 16 characters
	 */
	public PacketPlayOutScoreboardTeam(String team, String prefix, String suffix, String visibility, String collision, Collection<String> players, int options) {
		this(0, team);
		this.playerPrefix = prefix;
		this.playerSuffix = suffix;
		this.nameTagVisibility = visibility;
		this.collisionRule = collision;
		this.players = players;
		this.options = options;
	}

	/**
	 * Constructs new instance with given parameters and 1 (REMOVE) action
	 * 
	 * @param	team
	 * 			Team name, up to 16 characters long
	 * @throws	IllegalArgumentException
	 * 			if {@code name} is null, empty or longer than 16 characters
	 */
	public PacketPlayOutScoreboardTeam(String team) {
		this(1, team);
	}

	/**
	 * Constructs new instance with given parameters and 2 (UPDATE_TEAM_INFO) action
	 * 
	 * @param	team
	 * 			Team name, up to 16 characters long
	 * @param	prefix
	 * 			Prefix of players in team
	 * @param	suffix
	 * 			Suffix of players in team
	 * @param	visibility
	 * 			NameTag visibility rule
	 * @param	collision
	 * 			Collision rule
	 * @param	options
	 * 			bitmask of team options
	 * @throws	IllegalArgumentException
	 * 			if {@code name} is null, empty or longer than 16 characters
	 */
	public PacketPlayOutScoreboardTeam(String team, String prefix, String suffix, String visibility, String collision, int options) {
		this(2, team);
		this.playerPrefix = prefix;
		this.playerSuffix = suffix;
		this.nameTagVisibility = visibility;
		this.collisionRule = collision;
		this.options = options;
	}

	/**
	 * Constructs new instance with given parameters and 3 (ADD_PLAYERS) if {@code add} is {@code true}, 
	 * 4 (REMOVE_PLAYERS) if {@code add} is {@code false} action.
	 * 
	 * @param	team
	 * 			Team name, up to 16 characters long
	 * @param	players
	 * 			Affected entities
	 * @param	add
	 * 			{@code true} if players should be added, {@code false} if removed from team
	 * @throws	IllegalArgumentException
	 * 			if {@code name} is null, empty or longer than 16 characters
	 */
	public PacketPlayOutScoreboardTeam(String team, Collection<String> players, boolean add) {
		this(add ? 3 : 4, team);
		this.players = players;
	}

	@Override
	public String toString() {
		return String.format("PacketPlayOutScoreboardTeam{name=%s,playerPrefix=%s,playerSuffix=%s,nameTagVisibility=%s,"
				+ "collisionRule=%s,color=%s,players=%s,method=%s,options=%s}",
				name, playerPrefix, playerSuffix, nameTagVisibility, collisionRule, color, players, method, options);
	}

	/**
	 * Returns {@link #name}
	 * @return	team name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns {@link #playerPrefix}
	 * @return	playerPrefix
	 */
	public String getPlayerPrefix() {
		return playerPrefix;
	}

	/**
	 * Returns {@link #playerSuffix}
	 * @return	playerSuffix
	 */
	public String getPlayerSuffix() {
		return playerSuffix;
	}

	/**
	 * Returns {@link #nameTagVisibility}
	 * @return	nameTagVisibility
	 */
	public String getNameTagVisibility() {
		return nameTagVisibility;
	}
	
	/**
	 * Returns {@link #collisionRule}
	 * @return	collisionRule
	 */
	public String getCollisionRule() {
		return collisionRule;
	}

	/**
	 * Sets {@link #color} to specified color
	 * 
	 * @param	color
	 * 			color to use
	 */
	public void setColor(EnumChatFormat color) {
		this.color = color;
	}

	/**
	 * Returns {@link #color}
	 * @return	color
	 */
	public EnumChatFormat getColor() {
		return color;
	}

	/**
	 * Returns {@link #players}
	 * @return	players
	 */
	public Collection<String> getPlayers() {
		return players;
	}

	/**
	 * Returns {@link #options}
	 * @return	team options
	 */
	public int getOptions() {
		return options;
	}
	
	/**
	 * Returns {@link #method}
	 * @return	packet action
	 */
	public int getMethod() {
		return method;
	}
}