package me.neznamy.tab.shared.packets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;

import me.neznamy.tab.platforms.bukkit.packets.NMSHook;
import me.neznamy.tab.shared.ProtocolVersion;
import net.md_5.bungee.protocol.packet.Team;

public class PacketPlayOutScoreboardTeam extends UniversalPacketPlayOut {

	public static Class<?> PacketPlayOutScoreboardTeam;
	private static Constructor<?> newPacketPlayOutScoreboardTeam;
	private static Field NAME;
	private static Field DISPLAYNAME;
	private static Field PREFIX;
	private static Field SUFFIX;
	private static Field VISIBILITY; //1.8+
	private static Field CHATFORMAT; //1.13+
	private static Field COLLISION; //1.9+
	public static Field PLAYERS;
	private static Field ACTION;
	public static Field SIGNATURE;
	
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

	public static void initializeClass() throws Exception {
		try {
			//1.7+
			PacketPlayOutScoreboardTeam = getNMSClass("PacketPlayOutScoreboardTeam");
		} catch (ClassNotFoundException e) {
			//1.6-
			PacketPlayOutScoreboardTeam = getNMSClass("Packet209SetScoreboardTeam");
		}
		newPacketPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.getConstructor();
		(NAME = PacketPlayOutScoreboardTeam.getDeclaredField("a")).setAccessible(true);
		(DISPLAYNAME = PacketPlayOutScoreboardTeam.getDeclaredField("b")).setAccessible(true);
		(PREFIX = PacketPlayOutScoreboardTeam.getDeclaredField("c")).setAccessible(true);
		(SUFFIX = PacketPlayOutScoreboardTeam.getDeclaredField("d")).setAccessible(true);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			//1.9+
			(VISIBILITY = PacketPlayOutScoreboardTeam.getDeclaredField("e")).setAccessible(true);
			(COLLISION = PacketPlayOutScoreboardTeam.getDeclaredField("f")).setAccessible(true);
			(PLAYERS = PacketPlayOutScoreboardTeam.getDeclaredField("h")).setAccessible(true);
			(ACTION = PacketPlayOutScoreboardTeam.getDeclaredField("i")).setAccessible(true);
			(SIGNATURE = PacketPlayOutScoreboardTeam.getDeclaredField("j")).setAccessible(true);
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
				(CHATFORMAT = PacketPlayOutScoreboardTeam.getDeclaredField("g")).setAccessible(true);
			}
		} else {
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
				//1.8.x
				(VISIBILITY = PacketPlayOutScoreboardTeam.getDeclaredField("e")).setAccessible(true);
				(PLAYERS = PacketPlayOutScoreboardTeam.getDeclaredField("g")).setAccessible(true);
				(ACTION = PacketPlayOutScoreboardTeam.getDeclaredField("h")).setAccessible(true);
				(SIGNATURE = PacketPlayOutScoreboardTeam.getDeclaredField("i")).setAccessible(true);
			} else {
				//1.5.x - 1.7.x
				(PLAYERS = PacketPlayOutScoreboardTeam.getDeclaredField("e")).setAccessible(true);
				(ACTION = PacketPlayOutScoreboardTeam.getDeclaredField("f")).setAccessible(true);
				(SIGNATURE = PacketPlayOutScoreboardTeam.getDeclaredField("g")).setAccessible(true);
			}
		}
	}

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
		Object packet = newPacketPlayOutScoreboardTeam.newInstance();
		NAME.set(packet, name);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
			DISPLAYNAME.set(packet, NMSHook.stringToComponent(IChatBaseComponent.optimizedComponent(name).toString(clientVersion)));
			if (prefix != null && prefix.length() > 0) PREFIX.set(packet, NMSHook.stringToComponent(IChatBaseComponent.optimizedComponent(prefix).toString(clientVersion)));
			if (suffix != null && suffix.length() > 0) SUFFIX.set(packet, NMSHook.stringToComponent(IChatBaseComponent.optimizedComponent(suffix).toString(clientVersion)));
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
		if (name == null || name.length() == 0) throw new IllegalArgumentException("Team name cannot be null/empty");
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
		if (name == null || name.length() == 0) throw new IllegalArgumentException("Team name cannot be null/empty");
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
}