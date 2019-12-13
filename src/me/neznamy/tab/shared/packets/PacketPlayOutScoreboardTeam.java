package me.neznamy.tab.shared.packets;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.placeholders.Placeholders;
import net.md_5.bungee.protocol.packet.Team;

public class PacketPlayOutScoreboardTeam extends UniversalPacketPlayOut{

	private String team;
	private String prefix;
	private String suffix;
	private String visibility;
	private String teamPush;
	private EnumChatFormat chatFormat;
	private Collection<String> entities;
	private int action;
	private int signature;

	@SuppressWarnings("unchecked")
	public PacketPlayOutScoreboardTeam(String team, String prefix, String suffix, String visibility, String teamPush, Collection<String> entities, int action, int signature, EnumChatFormat format) {
		this.team = team;
		this.prefix = prefix;
		this.suffix = suffix;
		this.visibility = visibility;
		this.teamPush = teamPush;
		this.entities = (Collection<String>) (entities == null ? Collections.emptyList() : entities);
		this.action = action;
		this.signature = signature;
		this.chatFormat = format == null ? EnumChatFormat.RESET : format;
	}
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		if (team == null || team.length() == 0) throw new IllegalArgumentException("Team name cannot be null/empty");
		String prefix = this.prefix;
		String suffix = this.suffix;
		if (clientVersion.getMinorVersion() < 13) {
			if (prefix != null && prefix.length() > 16) prefix = prefix.substring(0, 16);
			if (suffix != null && suffix.length() > 16) suffix = suffix.substring(0, 16);
		}
		Object packet = MethodAPI.getInstance().newPacketPlayOutScoreboardTeam();
		NAME.set(packet, team);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
			DISPLAYNAME.set(packet, MethodAPI.getInstance().ICBC_fromString(new IChatBaseComponent(team).toString()));
			if (prefix != null && prefix.length() > 0) {
				PREFIX.set(packet, MethodAPI.getInstance().ICBC_fromString(new IChatBaseComponent(prefix).toString()));
				String last = Placeholders.getLastColors(prefix);
				if (last != null && last.length() > 0) {
					chatFormat = EnumChatFormat.getByCharacter(last.toCharArray()[1]);
				}
				if (chatFormat != null) CHATFORMAT.set(packet, chatFormat.toNMS());
			}
			if (suffix != null && suffix.length() > 0) SUFFIX.set(packet, MethodAPI.getInstance().ICBC_fromString(new IChatBaseComponent(suffix).toString()));
		} else {
			DISPLAYNAME.set(packet, team);
			if (prefix != null) PREFIX.set(packet, prefix);
			if (suffix != null) SUFFIX.set(packet, suffix);
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) PUSH.set(packet, teamPush);
		PLAYERS.set(packet, entities);
		ACTION.set(packet, action);
		SIGNATURE.set(packet, signature);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) VISIBILITY.set(packet, visibility);
		return packet;
	}
	public Object toBungee(ProtocolVersion clientVersion) {
		String teamDisplay = team;
		int color = 0;
		if (clientVersion.getMinorVersion() >= 13) {
			prefix = new IChatBaseComponent(prefix).toString();
			suffix = new IChatBaseComponent(suffix).toString();
			teamDisplay = new IChatBaseComponent(team).toString();
			color = chatFormat.toBungee();
		} else {
			if (prefix != null && prefix.length() > 16) prefix = prefix.substring(0, 16);
			if (suffix != null && suffix.length() > 16) suffix = suffix.substring(0, 16);
		}
		return new Team(team, (byte)action, teamDisplay, prefix, suffix, visibility, teamPush, color, (byte)signature, entities.toArray(new String[0]));
	}
	public Object toVelocity(ProtocolVersion clientVersion) {
		String teamDisplay = team;
		int color = 0;
		if (clientVersion.getMinorVersion() >= 13) {
			prefix = new IChatBaseComponent(prefix).toString();
			suffix = new IChatBaseComponent(suffix).toString();
			teamDisplay = new IChatBaseComponent(team).toString();
			color = chatFormat.toBungee();
		} else {
			if (prefix != null && prefix.length() > 16) prefix = prefix.substring(0, 16);
			if (suffix != null && suffix.length() > 16) suffix = suffix.substring(0, 16);
		}
		return new me.neznamy.tab.platforms.velocity.protocol.Team(team, (byte)action, teamDisplay, prefix, suffix, visibility, teamPush, color, (byte)signature, entities.toArray(new String[0]));
	}
	private static Map<String, Field> fields = getFields(MethodAPI.PacketPlayOutScoreboardTeam);
	private static final Field NAME = getField(fields, "a");
	private static final Field DISPLAYNAME = getField(fields, "b");
	private static final Field PREFIX = getField(fields, "c");
	private static final Field SUFFIX = getField(fields, "d");
	private static final Field VISIBILITY;
	private static final Field CHATFORMAT;
	private static final Field PUSH;
	public static final Field PLAYERS;
	private static final Field ACTION;
	public static final Field SIGNATURE;

	static {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			//1.9+
			VISIBILITY = getField(fields, "e");
			PUSH = getField(fields, "f");
			PLAYERS = getField(fields, "h");
			ACTION = getField(fields, "i");
			SIGNATURE = getField(fields, "j");
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
				CHATFORMAT = getField(fields, "g");
			} else {
				CHATFORMAT = null;
			}
		} else {
			CHATFORMAT = null;
			PUSH = null;
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
				//1.8.x
				VISIBILITY = getField(fields, "e");
				PLAYERS = getField(fields, "g");
				ACTION = getField(fields, "h");
				SIGNATURE = getField(fields, "i");
			} else {
				//1.5.x - 1.7.x
				VISIBILITY = null;
				PLAYERS = getField(fields, "e");
				ACTION = getField(fields, "f");
				SIGNATURE = getField(fields, "g");
			}
		}
	}
}