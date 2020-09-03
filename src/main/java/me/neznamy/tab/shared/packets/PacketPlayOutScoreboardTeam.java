package me.neznamy.tab.shared.packets;

import java.util.Collection;
import java.util.Collections;

import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutScoreboardTeam extends UniversalPacketPlayOut {

	public String name;
//	public String displayName;
	public String playerPrefix;
	public String playerSuffix;
	public String nametagVisibility;
	public String collisionRule;
	public EnumChatFormat color;
	public Collection<String> players = Collections.emptyList();
	public int method;
	public int options;

	private PacketPlayOutScoreboardTeam() {
	}

	public static PacketPlayOutScoreboardTeam CREATE_TEAM(String team, String prefix, String suffix, String visibility, String collision, Collection<String> players, int options) {
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

	public static PacketPlayOutScoreboardTeam REMOVE_TEAM(String team) {
		PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
		packet.method = 1;
		packet.name = team;
		return packet;
	}

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

	public static PacketPlayOutScoreboardTeam ADD_PLAYERS(String team, Collection<String> players) {
		PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
		packet.method = 3;
		packet.name = team;
		packet.players = players;
		return packet;
	}

	public static PacketPlayOutScoreboardTeam REMOVE_PLAYERS(String team, Collection<String> players) {
		PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
		packet.method = 4;
		packet.name = team;
		packet.players = players;
		return packet;
	}

	public PacketPlayOutScoreboardTeam setTeamOptions(int options) {
		this.options = options;
		return this;
	}

	public PacketPlayOutScoreboardTeam setColor(EnumChatFormat color) {
		this.color = color;
		return this;
	}

	@Override
	protected Object build(ProtocolVersion clientVersion) throws Exception {
		return builder.build(this, clientVersion);
	}
}