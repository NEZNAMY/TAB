package me.neznamy.tab.shared.packets;

import java.lang.reflect.Field;
import java.util.Map;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.ScoreboardObjective.HealthDisplay;

public class PacketPlayOutScoreboardObjective extends UniversalPacketPlayOut{

	private String objectiveName;
	private String displayName;
	private EnumScoreboardHealthDisplay renderType;
	private int method;

	public PacketPlayOutScoreboardObjective(String objectiveName, String displayName, EnumScoreboardHealthDisplay renderType, int method) {
		this.objectiveName = objectiveName;
		this.displayName = displayName;
		this.renderType = renderType;
		this.method = method;
	}
	public PacketPlayOutScoreboardObjective(String objectiveName) {
		this.objectiveName = objectiveName;
		this.method = 1; // REMOVE
	}
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		String displayName = this.displayName;
		if (clientVersion.getMinorVersion() < 13) {
			displayName = cutTo(displayName, 32);
		}
		Object packet = MethodAPI.getInstance().newPacketPlayOutScoreboardObjective();
		OBJECTIVENAME.set(packet, objectiveName);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
			DISPLAYNAME.set(packet, MethodAPI.getInstance().ICBC_fromString(new IChatBaseComponent(displayName).toString()));
		} else {
			DISPLAYNAME.set(packet, displayName);
		}
		if (RENDERTYPE != null && renderType != null) RENDERTYPE.set(packet, renderType.toNMS());
		METHOD.set(packet, method);
		return packet;
	}
	public Object toBungee(ProtocolVersion clientVersion) {
		String displayName = this.displayName;
		if (clientVersion.getMinorVersion() >= 13) {
			displayName = new IChatBaseComponent(displayName).toString();
		} else {
			displayName = cutTo(displayName, 32);
		}
		return new ScoreboardObjective(objectiveName, displayName, renderType == null ? null : renderType.toBungee(), (byte)method);
	}
	public Object toVelocity(ProtocolVersion clientVersion) {
		String displayName = this.displayName;
		if (clientVersion.getMinorVersion() >= 13) {
			displayName = new IChatBaseComponent(displayName).toString();
		} else {
			displayName = cutTo(displayName, 32);
		}
		return new me.neznamy.tab.platforms.velocity.protocol.ScoreboardObjective(objectiveName, displayName, renderType == null ? null : renderType.toVelocity(), (byte)method);
	}
	public enum EnumScoreboardHealthDisplay{

		INTEGER, HEARTS;

		private Object nmsEquivalent;

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private EnumScoreboardHealthDisplay() {
			if (MethodAPI.EnumScoreboardHealthDisplay != null) {
				nmsEquivalent = Enum.valueOf((Class<Enum>)MethodAPI.EnumScoreboardHealthDisplay, toString());
			} else {
				nmsEquivalent = ordinal();
			}
		}
		public Object toNMS() {
			return nmsEquivalent;
		}
		public HealthDisplay toBungee() {
			return HealthDisplay.valueOf(toString());
		}
		public me.neznamy.tab.platforms.velocity.protocol.ScoreboardObjective.HealthDisplay toVelocity() {
			return me.neznamy.tab.platforms.velocity.protocol.ScoreboardObjective.HealthDisplay.valueOf(toString());
		}
	}
	private static Map<String, Field> fields = getFields(MethodAPI.PacketPlayOutScoreboardObjective);
	private static final Field OBJECTIVENAME = getField(fields, "a");
	private static final Field DISPLAYNAME = getField(fields, "b");
	private static Field RENDERTYPE;
	private static final Field METHOD;

	static {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			RENDERTYPE = getField(fields, "c");
			METHOD = getField(fields, "d");
		} else {
			METHOD = getField(fields, "c");
		}
	}
}