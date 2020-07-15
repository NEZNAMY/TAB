package me.neznamy.tab.shared.packets;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;
import net.md_5.bungee.protocol.packet.Team;

public class PacketPlayOutScoreboardTeam extends UniversalPacketPlayOut{

	private static Map<String, Field> fields = getFields(MethodAPI.PacketPlayOutScoreboardTeam);
	private static final Field NAME = getField(fields, "a");
	private static final Field DISPLAYNAME = getField(fields, "b");
	private static final Field PREFIX = getField(fields, "c");
	private static final Field SUFFIX = getField(fields, "d");
	private static Field VISIBILITY; //1.8+
	private static Field CHATFORMAT; //1.13+
	private static Field COLLISION; //1.9+
	public static final Field PLAYERS;
	private static final Field ACTION;
	public static final Field SIGNATURE;
	
	private String name;
//	private String displayName;
	private String playerPrefix;
	private String playerSuffix;
	private String nametagVisibility;
	private String collisionRule;
	private EnumChatFormat color;
	private Collection<String> players = Collections.emptyList();
	private int method;
	private int options;

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
	
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		if (name == null || name.length() == 0) throw new IllegalArgumentException("Team name cannot be null/empty");
		String prefix = this.playerPrefix;
		String suffix = this.playerSuffix;
		if (clientVersion.getMinorVersion() < 13) {
			prefix = cutTo(prefix, 16);
			suffix = cutTo(suffix, 16);
		}
		Object packet = MethodAPI.getInstance().newPacketPlayOutScoreboardTeam();
		NAME.set(packet, name);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
			DISPLAYNAME.set(packet, MethodAPI.getInstance().stringToComponent(IChatBaseComponent.optimizedComponent(name).toString(clientVersion)));
			if (prefix != null && prefix.length() > 0) PREFIX.set(packet, MethodAPI.getInstance().stringToComponent(IChatBaseComponent.optimizedComponent(prefix).toString(clientVersion)));
			if (suffix != null && suffix.length() > 0) SUFFIX.set(packet, MethodAPI.getInstance().stringToComponent(IChatBaseComponent.optimizedComponent(suffix).toString(clientVersion)));
			CHATFORMAT.set(packet, color != null ? color.toNMS() : EnumChatFormat.lastColorsOf(prefix).toNMS());
		} else {
			DISPLAYNAME.set(packet, name);
			PREFIX.set(packet, prefix);
			SUFFIX.set(packet, suffix);
		}
		if (COLLISION != null) COLLISION.set(packet, collisionRule);
		PLAYERS.set(packet, players);
		ACTION.set(packet, method);
		SIGNATURE.set(packet, options);
		if (VISIBILITY != null) VISIBILITY.set(packet, nametagVisibility);
		return packet;
	}
	public Object toBungee(ProtocolVersion clientVersion) {
		String teamDisplay = name;
		int color = 0;
		String prefix;
		String suffix;
		if (clientVersion.getMinorVersion() >= 13) {
			prefix = IChatBaseComponent.optimizedComponent(playerPrefix).toString(clientVersion);
			suffix = IChatBaseComponent.optimizedComponent(playerSuffix).toString(clientVersion);
			teamDisplay = IChatBaseComponent.optimizedComponent(name).toString(clientVersion);
			color = EnumChatFormat.lastColorsOf(playerPrefix).getNetworkId();
		} else {
			prefix = cutTo(this.playerPrefix, 16);
			suffix = cutTo(this.playerSuffix, 16);
		}
		return new Team(name, (byte)method, teamDisplay, prefix, suffix, nametagVisibility, collisionRule, color, (byte)options, players.toArray(new String[0]));
	}
	public Object toVelocity(ProtocolVersion clientVersion) {
		String teamDisplay = name;
		int color = 0;
		String prefix;
		String suffix;
		if (clientVersion.getMinorVersion() >= 13) {
			prefix = IChatBaseComponent.optimizedComponent(playerPrefix).toString(clientVersion);
			suffix = IChatBaseComponent.optimizedComponent(playerSuffix).toString(clientVersion);
			teamDisplay = IChatBaseComponent.optimizedComponent(name).toString(clientVersion);
			color = EnumChatFormat.lastColorsOf(playerPrefix).getNetworkId();
		} else {
			prefix = cutTo(this.playerPrefix, 16);
			suffix = cutTo(this.playerSuffix, 16);
		}
		return new me.neznamy.tab.platforms.velocity.protocol.Team(name, (byte)method, teamDisplay, prefix, suffix, nametagVisibility, collisionRule, color, (byte)options, players.toArray(new String[0]));
	}

	static {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			//1.9+
			VISIBILITY = getField(fields, "e");
			COLLISION = getField(fields, "f");
			PLAYERS = getField(fields, "h");
			ACTION = getField(fields, "i");
			SIGNATURE = getField(fields, "j");
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) CHATFORMAT = getField(fields, "g");
		} else {
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
				//1.8.x
				VISIBILITY = getField(fields, "e");
				PLAYERS = getField(fields, "g");
				ACTION = getField(fields, "h");
				SIGNATURE = getField(fields, "i");
			} else {
				//1.5.x - 1.7.x
				PLAYERS = getField(fields, "e");
				ACTION = getField(fields, "f");
				SIGNATURE = getField(fields, "g");
			}
		}
	}
}