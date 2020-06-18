package me.neznamy.tab.shared.packets;

import java.lang.reflect.Field;
import java.util.Map;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.ScoreboardObjective.HealthDisplay;

public class PacketPlayOutScoreboardObjective extends UniversalPacketPlayOut{

	private static Map<String, Field> fields = getFields(MethodAPI.PacketPlayOutScoreboardObjective);
	private static final Field OBJECTIVENAME = getField(fields, "a");
	private static final Field DISPLAYNAME = getField(fields, "b");
	private static Field RENDERTYPE;
	private static final Field METHOD;
	
	private String objectiveName;
	private String displayName;
	private EnumScoreboardHealthDisplay renderType;
	private int method;

	private PacketPlayOutScoreboardObjective() {
		
	}
	public static PacketPlayOutScoreboardObjective REGISTER(String objectiveName, String displayName, EnumScoreboardHealthDisplay renderType) {
		PacketPlayOutScoreboardObjective packet =  new PacketPlayOutScoreboardObjective();
		packet.objectiveName = objectiveName;
		packet.displayName = displayName;
		packet.renderType = renderType;
		packet.method = 0;
		return packet;
	}
	public static PacketPlayOutScoreboardObjective UNREGISTER(String objectiveName) {
		PacketPlayOutScoreboardObjective packet =  new PacketPlayOutScoreboardObjective();
		packet.objectiveName = objectiveName;
		packet.displayName = ""; //error on <1.7
		packet.method = 1;
		return packet;
	}
	public static PacketPlayOutScoreboardObjective UPDATE_TITLE(String objectiveName, String displayName, EnumScoreboardHealthDisplay renderType) {
		PacketPlayOutScoreboardObjective packet =  new PacketPlayOutScoreboardObjective();
		packet.objectiveName = objectiveName;
		packet.displayName = displayName;
		packet.renderType = renderType;
		packet.method = 2;
		return packet;
	}
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		String displayName = this.displayName;
		if (clientVersion.getMinorVersion() < 13) {
			displayName = cutTo(displayName, 32);
		}
		Object packet = MethodAPI.getInstance().newPacketPlayOutScoreboardObjective();
		OBJECTIVENAME.set(packet, objectiveName);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
			DISPLAYNAME.set(packet, MethodAPI.getInstance().stringToComponent(IChatBaseComponent.fromColoredText(displayName).toString(clientVersion)));
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
			displayName = IChatBaseComponent.fromColoredText(displayName).toString(clientVersion);
		} else {
			displayName = cutTo(displayName, 32);
		}
		return new ScoreboardObjective(objectiveName, displayName, renderType == null ? null : renderType.toBungee(), (byte)method);
	}
	public Object toVelocity(ProtocolVersion clientVersion) {
		String displayName = this.displayName;
		if (clientVersion.getMinorVersion() >= 13) {
			displayName = IChatBaseComponent.fromColoredText(displayName).toString(clientVersion);
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

	static {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			RENDERTYPE = getField(fields, "c");
			METHOD = getField(fields, "d");
		} else {
			METHOD = getField(fields, "c");
		}
	}
}