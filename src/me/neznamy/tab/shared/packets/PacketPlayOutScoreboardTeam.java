package me.neznamy.tab.shared.packets;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.bukkit.ChatColor;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
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
			DISPLAYNAME.set(packet, Shared.mainClass.createComponent(team));
			if (prefix != null && prefix.length() > 0) {
				PREFIX.set(packet, Shared.mainClass.createComponent(prefix));
				String last = ChatColor.getLastColors(prefix);
				if (last != null && last.length() > 0) {
					chatFormat = EnumChatFormat.getByCharacter(last.toCharArray()[1]);
				}
				CHATFORMAT.set(packet, chatFormat.toNMS());
			}
			if (suffix != null && suffix.length() > 0) SUFFIX.set(packet, Shared.mainClass.createComponent(suffix));
		} else {
			DISPLAYNAME.set(packet, team);
			if (prefix != null) PREFIX.set(packet, prefix);
			if (suffix != null) SUFFIX.set(packet, suffix);
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			PUSH.set(packet, teamPush);
		}
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
			if (prefix != null) prefix = (String) Shared.mainClass.createComponent(prefix);
			if (prefix != null) suffix = (String) Shared.mainClass.createComponent(suffix);
			teamDisplay = (String) Shared.mainClass.createComponent(team);
			color = chatFormat.toBungee();
		} else {
			if (prefix != null && prefix.length() > 16) prefix = prefix.substring(0, 16);
			if (suffix != null && suffix.length() > 16) suffix = suffix.substring(0, 16);
		}
		return new Team(team, (byte)action, teamDisplay, prefix, suffix, visibility, teamPush, color, (byte)signature, entities.toArray(new String[0]));
	}
	public Object toVelocity(ProtocolVersion clientVersion) {
		return null;
	}
	private static Map<String, Field> fields = getFields(MethodAPI.PacketPlayOutScoreboardTeam);
	private static Field NAME = fields.get("a");
	private static Field DISPLAYNAME = fields.get("b");
	private static Field PREFIX = fields.get("c");
	private static Field SUFFIX = fields.get("d");
	private static Field VISIBILITY;
	private static Field CHATFORMAT;
	private static Field PUSH;
	public static Field PLAYERS;
	private static Field ACTION;
	public static Field SIGNATURE;

	static {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			//1.9+
			VISIBILITY = fields.get("e");
			PUSH = fields.get("f");
			PLAYERS = fields.get("h");
			ACTION = fields.get("i");
			SIGNATURE = fields.get("j");
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
				CHATFORMAT = fields.get("g");
			}
		} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			//1.8.x
			VISIBILITY = fields.get("e");
			PLAYERS = fields.get("g");
			ACTION = fields.get("h");
			SIGNATURE = fields.get("i");
		} else {
			//1.7.x
			PLAYERS = fields.get("e");
			ACTION = fields.get("f");
			SIGNATURE = fields.get("g");
		}
	}
}