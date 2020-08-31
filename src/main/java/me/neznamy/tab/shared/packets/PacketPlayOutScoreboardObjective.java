package me.neznamy.tab.shared.packets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import me.neznamy.tab.platforms.bukkit.packets.NMSHook;
import me.neznamy.tab.shared.ProtocolVersion;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.ScoreboardObjective.HealthDisplay;

/**
 * A class representing platform specific packet class
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class PacketPlayOutScoreboardObjective extends UniversalPacketPlayOut{

	private static Class<?> PacketPlayOutScoreboardObjective;
	private static Class<Enum> EnumScoreboardHealthDisplay;
	private static Constructor<?> newPacketPlayOutScoreboardObjective;
	private static Field OBJECTIVENAME;
	private static Field DISPLAYNAME;
	private static Field RENDERTYPE;
	private static Field METHOD;
	
	private String objectiveName;
	private String displayName;
	private EnumScoreboardHealthDisplay renderType;
	private int method;

	public static void initializeClass() throws Exception {
		try {
			//1.7+
			PacketPlayOutScoreboardObjective = getNMSClass("PacketPlayOutScoreboardObjective");
		} catch (ClassNotFoundException e) {
			//1.6-
			PacketPlayOutScoreboardObjective = getNMSClass("Packet206SetScoreboardObjective");
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			try {
				//v1_8_R2+
				EnumScoreboardHealthDisplay = (Class<Enum>) getNMSClass("IScoreboardCriteria$EnumScoreboardHealthDisplay");
			} catch (ClassNotFoundException e) {
				//v1_8_R1
				EnumScoreboardHealthDisplay = (Class<Enum>) getNMSClass("EnumScoreboardHealthDisplay");
			}
		}
		newPacketPlayOutScoreboardObjective = PacketPlayOutScoreboardObjective.getConstructor();
		(OBJECTIVENAME = PacketPlayOutScoreboardObjective.getDeclaredField("a")).setAccessible(true);
		(DISPLAYNAME = PacketPlayOutScoreboardObjective.getDeclaredField("b")).setAccessible(true);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			(RENDERTYPE = PacketPlayOutScoreboardObjective.getDeclaredField("c")).setAccessible(true);
			(METHOD = PacketPlayOutScoreboardObjective.getDeclaredField("d")).setAccessible(true);
		} else {
			(METHOD = PacketPlayOutScoreboardObjective.getDeclaredField("c")).setAccessible(true);
		}
	}

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
	
	@Override
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		String displayName = this.displayName;
		if (clientVersion.getMinorVersion() < 13) {
			displayName = cutTo(displayName, 32);
		}
		Object packet = newPacketPlayOutScoreboardObjective.newInstance();
		OBJECTIVENAME.set(packet, objectiveName);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
			DISPLAYNAME.set(packet, NMSHook.stringToComponent(IChatBaseComponent.optimizedComponent(displayName).toString(clientVersion)));
		} else {
			DISPLAYNAME.set(packet, displayName);
		}
		if (RENDERTYPE != null && renderType != null) RENDERTYPE.set(packet, renderType.toNMS());
		METHOD.set(packet, method);
		return packet;
	}
	
	@Override
	public Object toBungee(ProtocolVersion clientVersion) {
		String displayName = this.displayName;
		if (clientVersion.getMinorVersion() >= 13) {
			displayName = IChatBaseComponent.optimizedComponent(displayName).toString(clientVersion);
		} else {
			displayName = cutTo(displayName, 32);
		}
		return new ScoreboardObjective(objectiveName, displayName, renderType == null ? null : renderType.toBungee(), (byte)method);
	}
	
	@Override
	public Object toVelocity(ProtocolVersion clientVersion) {
		String displayName = this.displayName;
		if (clientVersion.getMinorVersion() >= 13) {
			displayName = IChatBaseComponent.optimizedComponent(displayName).toString(clientVersion);
		} else {
			displayName = cutTo(displayName, 32);
		}
		return new me.neznamy.tab.platforms.velocity.protocol.ScoreboardObjective(objectiveName, displayName, renderType == null ? null : renderType.toVelocity(), (byte)method);
	}
	
	public enum EnumScoreboardHealthDisplay{

		INTEGER, HEARTS;

		private Object nmsEquivalent;

		private EnumScoreboardHealthDisplay() {
			if (EnumScoreboardHealthDisplay != null) {
				nmsEquivalent = Enum.valueOf(EnumScoreboardHealthDisplay, toString());
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
}