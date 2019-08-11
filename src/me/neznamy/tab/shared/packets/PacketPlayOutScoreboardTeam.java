package me.neznamy.tab.shared.packets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;

import org.bukkit.ChatColor;

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
	public Object toNMS() throws Exception {
		if (team == null || team.length() == 0) throw new IllegalArgumentException("Team name cannot be null/empty");
		Object packet = newPacketPlayOutScoreboardTeam.newInstance();
		PacketPlayOutScoreboardTeam_NAME.set(packet, team);
		if (versionNumber >= 13) {
			PacketPlayOutScoreboardTeam_DISPLAYNAME.set(packet, Shared.mainClass.createComponent(team));
			if (prefix != null && prefix.length() > 0) {
				PacketPlayOutScoreboardTeam_PREFIX.set(packet, Shared.mainClass.createComponent(prefix));
				String last = ChatColor.getLastColors(prefix);
				if (last != null && last.length() > 0) {
					chatFormat = EnumChatFormat.getByCharacter(last.toCharArray()[1]);
				}
				PacketPlayOutScoreboardTeam_CHATFORMAT.set(packet, chatFormat.toNMS());
			}
			if (suffix != null && suffix.length() > 0) PacketPlayOutScoreboardTeam_SUFFIX.set(packet, Shared.mainClass.createComponent(suffix));
		} else {
			if (prefix != null && prefix.length() > 16) prefix = prefix.substring(0, 16);
			if (suffix != null && suffix.length() > 16) suffix = suffix.substring(0, 16);
			PacketPlayOutScoreboardTeam_DISPLAYNAME.set(packet, team);
			if (prefix != null) PacketPlayOutScoreboardTeam_PREFIX.set(packet, prefix);
			if (suffix != null) PacketPlayOutScoreboardTeam_SUFFIX.set(packet, suffix);
		}
		if (versionNumber >= 9) {
			PacketPlayOutScoreboardTeam_PUSH.set(packet, teamPush);
		}
		PacketPlayOutScoreboardTeam_PLAYERS.set(packet, entities);
		PacketPlayOutScoreboardTeam_ACTION.set(packet, action);
		PacketPlayOutScoreboardTeam_SIGNATURE.set(packet, signature);
		PacketPlayOutScoreboardTeam_VISIBILITY.set(packet, visibility);
		return packet;
	}
	public Object toBungee(int clientVersion) {
		if (team == null || team.length() == 0) throw new IllegalArgumentException("Team name cannot be null/empty");
		String teamDisplay = team;
		int color = chatFormat.toBungee();
		if (clientVersion > 340) {
			if (prefix != null) prefix = (String) Shared.mainClass.createComponent(prefix);
			if (prefix != null) suffix = (String) Shared.mainClass.createComponent(suffix);
			teamDisplay = (String) Shared.mainClass.createComponent(team);
		} else {
			if (prefix != null && prefix.length() > 16) prefix = prefix.substring(0, 16);
			if (suffix != null && suffix.length() > 16) suffix = suffix.substring(0, 16);
			color = 0;
		}
		return new Team(team, (byte)action, teamDisplay, prefix, suffix, visibility, teamPush, color, (byte)signature, entities.toArray(new String[0]));
	}

	public static Class<?> PacketPlayOutScoreboardTeam;
	private static Constructor<?> newPacketPlayOutScoreboardTeam;
	private static Field PacketPlayOutScoreboardTeam_NAME;
	private static Field PacketPlayOutScoreboardTeam_DISPLAYNAME;
	private static Field PacketPlayOutScoreboardTeam_PREFIX;
	private static Field PacketPlayOutScoreboardTeam_SUFFIX;
	private static Field PacketPlayOutScoreboardTeam_VISIBILITY;
	private static Field PacketPlayOutScoreboardTeam_CHATFORMAT;
	private static Field PacketPlayOutScoreboardTeam_PUSH;
	public static Field PacketPlayOutScoreboardTeam_PLAYERS;
	private static Field PacketPlayOutScoreboardTeam_ACTION;
	public static Field PacketPlayOutScoreboardTeam_SIGNATURE;

	static {
		try {
			if (versionNumber >= 8) {
				PacketPlayOutScoreboardTeam = getNMSClass("PacketPlayOutScoreboardTeam");
				newPacketPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.getConstructor();
				if (versionNumber >= 9) {
					(PacketPlayOutScoreboardTeam_PUSH = PacketPlayOutScoreboardTeam.getDeclaredField("f")).setAccessible(true);
					PacketPlayOutScoreboardTeam_PLAYERS = PacketPlayOutScoreboardTeam.getDeclaredField("h");
					PacketPlayOutScoreboardTeam_ACTION = PacketPlayOutScoreboardTeam.getDeclaredField("i");
					PacketPlayOutScoreboardTeam_SIGNATURE = PacketPlayOutScoreboardTeam.getDeclaredField("j");
					if (versionNumber >= 13) {
						(PacketPlayOutScoreboardTeam_CHATFORMAT = PacketPlayOutScoreboardTeam.getDeclaredField("g")).setAccessible(true);
					}
				} else {
					PacketPlayOutScoreboardTeam_PLAYERS = PacketPlayOutScoreboardTeam.getDeclaredField("g");
					PacketPlayOutScoreboardTeam_ACTION = PacketPlayOutScoreboardTeam.getDeclaredField("h");
					PacketPlayOutScoreboardTeam_SIGNATURE = PacketPlayOutScoreboardTeam.getDeclaredField("i");
				}
				(PacketPlayOutScoreboardTeam_NAME = PacketPlayOutScoreboardTeam.getDeclaredField("a")).setAccessible(true);
				(PacketPlayOutScoreboardTeam_DISPLAYNAME = PacketPlayOutScoreboardTeam.getDeclaredField("b")).setAccessible(true);
				(PacketPlayOutScoreboardTeam_PREFIX = PacketPlayOutScoreboardTeam.getDeclaredField("c")).setAccessible(true);
				(PacketPlayOutScoreboardTeam_SUFFIX = PacketPlayOutScoreboardTeam.getDeclaredField("d")).setAccessible(true);
				(PacketPlayOutScoreboardTeam_VISIBILITY = PacketPlayOutScoreboardTeam.getDeclaredField("e")).setAccessible(true);
				PacketPlayOutScoreboardTeam_PLAYERS.setAccessible(true);
				PacketPlayOutScoreboardTeam_ACTION.setAccessible(true);
				PacketPlayOutScoreboardTeam_SIGNATURE.setAccessible(true);
			}
		} catch (Exception e) {
			Shared.error("Failed to initialize PacketPlayOutScoreboardTeam class", e);
		}
	}
}