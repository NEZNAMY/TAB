package me.neznamy.tab.shared.packets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import me.neznamy.tab.shared.ProtocolVersion;
import net.md_5.bungee.protocol.packet.ScoreboardScore;

/**
 * A class representing platform specific packet class
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class PacketPlayOutScoreboardScore extends UniversalPacketPlayOut{

	private static Class<?> PacketPlayOutScoreboardScore;
	private static Class<Enum> EnumScoreboardAction;
	private static Constructor<?> newPacketPlayOutScoreboardScore0;
	private static Constructor<?> newPacketPlayOutScoreboardScore_String;
	private static Constructor<?> newPacketPlayOutScoreboardScore_1_13;
	private static Field PLAYER;
	private static Field OBJECTIVENAME;
	private static Field SCORE;
	private static Field ACTION;
	
	private Action action;
	private String objectiveName;
	private String player;
	private int score;

	public static void initializeClass() throws Exception {
		try {
			//1.7+
			PacketPlayOutScoreboardScore = getNMSClass("PacketPlayOutScoreboardScore");
		} catch (ClassNotFoundException e) {
			//1.6-
			PacketPlayOutScoreboardScore = getNMSClass("Packet207SetScoreboardScore");
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
			EnumScoreboardAction = (Class<Enum>) getNMSClass("ScoreboardServer$Action");
			newPacketPlayOutScoreboardScore_1_13 = PacketPlayOutScoreboardScore.getConstructor(EnumScoreboardAction, String.class, String.class, int.class);
		} else {
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
				try {
					//v1_8_R2+
					EnumScoreboardAction = (Class<Enum>) getNMSClass("PacketPlayOutScoreboardScore$EnumScoreboardAction");
				} catch (ClassNotFoundException e) {
					//v1_8_R1
					EnumScoreboardAction = (Class<Enum>) getNMSClass("EnumScoreboardAction");
				}
			}
			newPacketPlayOutScoreboardScore0 = PacketPlayOutScoreboardScore.getConstructor();
			newPacketPlayOutScoreboardScore_String = PacketPlayOutScoreboardScore.getConstructor(String.class);
		}
		(PLAYER = PacketPlayOutScoreboardScore.getDeclaredField("a")).setAccessible(true);
		(OBJECTIVENAME = PacketPlayOutScoreboardScore.getDeclaredField("b")).setAccessible(true);
		(SCORE = PacketPlayOutScoreboardScore.getDeclaredField("c")).setAccessible(true);
		(ACTION = PacketPlayOutScoreboardScore.getDeclaredField("d")).setAccessible(true);
	}
	
	public PacketPlayOutScoreboardScore(Action action, String objectiveName, String player, int score) {
		this.action = action;
		this.objectiveName = objectiveName;
		this.player = player;
		this.score = score;
	}
	
	@Override
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		Object packet;
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
			return newPacketPlayOutScoreboardScore_1_13.newInstance(action.toNMS(), objectiveName, player, score);
		} else {
			if (action == Action.REMOVE) {
				return newPacketPlayOutScoreboardScore_String.newInstance(player);
			} else {
				packet = newPacketPlayOutScoreboardScore0.newInstance();
				PLAYER.set(packet, player);
				OBJECTIVENAME.set(packet, objectiveName);
				SCORE.set(packet, score);
				ACTION.set(packet, action.toNMS());
			}
		}
		return packet;
	}
	
	@Override
	public Object toBungee(ProtocolVersion clientVersion) {
		return new ScoreboardScore(player, action.toBungee(), objectiveName, score);
	}
	
	@Override
	public Object toVelocity(ProtocolVersion clientVersion) {
		return new me.neznamy.tab.platforms.velocity.protocol.ScoreboardScore(player, action.toBungee(), objectiveName, score);
	}
	
	public enum Action{

		CHANGE((byte) 0),
		REMOVE((byte) 1);

		private byte ordinal;
		private Object nmsEquivalent;

		private Action(byte ordinal) {
			this.ordinal = ordinal;
			if (EnumScoreboardAction != null) {
				//1.8+
				nmsEquivalent = Enum.valueOf(EnumScoreboardAction, toString());
			} else {
				//1.7-
				nmsEquivalent = ordinal;
			}
		}
		public Object toNMS() {
			return nmsEquivalent;
		}
		public byte toBungee() {
			return ordinal;
		}
	}
}