package me.neznamy.tab.shared.packets;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;
import net.md_5.bungee.protocol.packet.Team;

public class PacketPlayOutScoreboardTeam extends UniversalPacketPlayOut{

	private String name;
//	private String displayName;
	private String playerPrefix;
	private String playerSuffix;
	private String nametagVisibility;
	private String collisionRule;
//	private EnumChatFormat color;
	private Collection<String> players = Collections.emptyList();
	private EnumTeamAction method;
	private int options;

	public PacketPlayOutScoreboardTeam(String team, String prefix, String suffix, String visibility, String collision, Collection<String> players, int options) {
		this.method = EnumTeamAction.CREATE_TEAM;
		this.name = team;
		this.playerPrefix = prefix;
		this.playerSuffix = suffix;
		this.nametagVisibility = visibility;
		this.collisionRule = collision;
		this.players = players;
		this.options = options;
	}
	public PacketPlayOutScoreboardTeam(String team) {
		this.method = EnumTeamAction.REMOVE_TEAM;
		this.name = team;
	}
	public PacketPlayOutScoreboardTeam(String team, String prefix, String suffix, String visibility, String collision, int options) {
		this.method = EnumTeamAction.UPDATE_TEAM_INFO;
		this.name = team;
		this.playerPrefix = prefix;
		this.playerSuffix = suffix;
		this.nametagVisibility = visibility;
		this.collisionRule = collision;
		this.options = options;
	}
	public PacketPlayOutScoreboardTeam(String team, Collection<String> players, EnumTeamAction method) {
		this.method = method;
		this.name = team;
		this.players = players;
	}
	
	public PacketPlayOutScoreboardTeam setTeamOptions(int options) {
		this.options = options;
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
			DISPLAYNAME.set(packet, MethodAPI.getInstance().ICBC_fromString(new IChatBaseComponent(name).toString()));
			if (prefix != null && prefix.length() > 0) PREFIX.set(packet, MethodAPI.getInstance().ICBC_fromString(new IChatBaseComponent(prefix).toString()));
			if (suffix != null && suffix.length() > 0) SUFFIX.set(packet, MethodAPI.getInstance().ICBC_fromString(new IChatBaseComponent(suffix).toString()));
			CHATFORMAT.set(packet, EnumChatFormat.lastColorsOf(prefix).toNMS());
		} else {
			DISPLAYNAME.set(packet, name);
			PREFIX.set(packet, prefix);
			SUFFIX.set(packet, suffix);
		}
		if (COLLISION != null) COLLISION.set(packet, collisionRule);
		PLAYERS.set(packet, players);
		ACTION.set(packet, method.getNetworkId());
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
			prefix = new IChatBaseComponent(this.playerPrefix).toString();
			suffix = new IChatBaseComponent(this.playerSuffix).toString();
			teamDisplay = new IChatBaseComponent(name).toString();
			color = EnumChatFormat.lastColorsOf(prefix).getNetworkId();
		} else {
			prefix = cutTo(this.playerPrefix, 16);
			suffix = cutTo(this.playerSuffix, 16);
		}
		return new Team(name, (byte)method.getNetworkId(), teamDisplay, prefix, suffix, nametagVisibility, collisionRule, color, (byte)options, players.toArray(new String[0]));
	}
	public Object toVelocity(ProtocolVersion clientVersion) {
		String teamDisplay = name;
		int color = 0;
		String prefix;
		String suffix;
		if (clientVersion.getMinorVersion() >= 13) {
			prefix = new IChatBaseComponent(this.playerPrefix).toString();
			suffix = new IChatBaseComponent(this.playerSuffix).toString();
			teamDisplay = new IChatBaseComponent(name).toString();
			color = EnumChatFormat.lastColorsOf(prefix).getNetworkId();
		} else {
			prefix = cutTo(this.playerPrefix, 16);
			suffix = cutTo(this.playerSuffix, 16);
		}
		return new me.neznamy.tab.platforms.velocity.protocol.Team(name, (byte)method.getNetworkId(), teamDisplay, prefix, suffix, nametagVisibility, collisionRule, color, (byte)options, players.toArray(new String[0]));
	}
	
	@Override
	public String toString() {
		switch(method) {
		case CREATE_TEAM:
			return "PacketPlayOutScoreboardTeam{name=" + name + ",playerPrefix=" + playerPrefix + ",playerSuffix=" + playerSuffix + ",nametagVisibility=" + nametagVisibility + ",collisionRule=" + collisionRule + ",players=" + players + ",method=" + method + ",options=" + options + "}";
		case REMOVE_TEAM:
			return "PacketPlayOutScoreboardTeam{name=" + name + ",method=" + method + "}";
		case UPDATE_TEAM_INFO:
			return "PacketPlayOutScoreboardTeam{name=" + name + ",playerPrefix=" + playerPrefix + ",playerSuffix=" + playerSuffix + ",nametagVisibility=" + nametagVisibility + ",collisionRule=" + collisionRule + ",method=" + method + ",options=" + options + "}";
		default: 
			return "PacketPlayOutScoreboardTeam{name=" + name + ",method=" + method + ",players=" + players + "}";
		}
	}
	
	public enum EnumTeamAction{
		
		CREATE_TEAM(0),
		REMOVE_TEAM(1),
		UPDATE_TEAM_INFO(2),
		ADD_PLAYERS(3),
		REMOVE_PLAYERS(4);
		
		private int networkId;
		
		EnumTeamAction(int networkId) {
			this.networkId = networkId;
		}
		public int getNetworkId() {
			return networkId;
		}
	}
	
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