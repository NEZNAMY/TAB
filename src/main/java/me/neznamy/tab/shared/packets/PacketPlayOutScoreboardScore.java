package me.neznamy.tab.shared.packets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

import me.neznamy.tab.shared.ProtocolVersion;
import net.md_5.bungee.protocol.packet.ScoreboardScore;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class PacketPlayOutScoreboardScore extends UniversalPacketPlayOut{

	private static Class<?> PacketPlayOutScoreboardScore = getNMSClass("PacketPlayOutScoreboardScore", "Packet207SetScoreboardScore");
	private static Class<Enum> EnumScoreboardAction = (Class<Enum>) getNMSClass("ScoreboardServer$Action", "PacketPlayOutScoreboardScore$EnumScoreboardAction", "EnumScoreboardAction");
	private static Constructor<?> newPacketPlayOutScoreboardScore0 = getConstructor(PacketPlayOutScoreboardScore, 0);
	private static Constructor<?> newPacketPlayOutScoreboardScore_String = getConstructor(PacketPlayOutScoreboardScore, String.class);
	private static Constructor<?> newPacketPlayOutScoreboardScore4 = getConstructor(PacketPlayOutScoreboardScore, 4);
	private static Map<String, Field> fields = getFields(PacketPlayOutScoreboardScore);
	private static final Field PLAYER = getField(fields, "a");
	private static final Field OBJECTIVENAME = getField(fields, "b");
	private static final Field SCORE = getField(fields, "c");
	private static final Field ACTION = getField(fields, "d");
	
	private Action action;
	private String objectiveName;
	private String player;
	private int score;

	public PacketPlayOutScoreboardScore(Action action, String objectiveName, String player, int score) {
		this.action = action;
		this.objectiveName = objectiveName;
		this.player = player;
		this.score = score;
	}
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		Object packet;
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
			return newPacketPlayOutScoreboardScore4.newInstance(action.toNMS(), objectiveName, player, score);
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
	public Object toBungee(ProtocolVersion clientVersion) {
		return new ScoreboardScore(player, action.toBungee(), objectiveName, score);
	}
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
				nmsEquivalent = Enum.valueOf(EnumScoreboardAction, toString());
			} else {
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