package me.neznamy.tab.shared.packets;

import java.lang.reflect.Field;
import java.util.Map;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;
import net.md_5.bungee.protocol.packet.ScoreboardScore;

public class PacketPlayOutScoreboardScore extends UniversalPacketPlayOut{

	private static Map<String, Field> fields = getFields(MethodAPI.PacketPlayOutScoreboardScore);
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
			return MethodAPI.getInstance().newPacketPlayOutScoreboardScore(action.toNMS(), objectiveName, player, score);
		} else {
			if (action == Action.REMOVE) {
				return MethodAPI.getInstance().newPacketPlayOutScoreboardScore(player);
			} else {
				packet = MethodAPI.getInstance().newPacketPlayOutScoreboardScore();
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

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private Action(byte ordinal) {
			this.ordinal = ordinal;
			if (MethodAPI.EnumScoreboardAction != null) {
				nmsEquivalent = Enum.valueOf((Class<Enum>)MethodAPI.EnumScoreboardAction, toString());
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