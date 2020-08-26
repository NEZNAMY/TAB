package me.neznamy.tab.shared.packets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import me.neznamy.tab.shared.ProtocolVersion;
import net.md_5.bungee.protocol.packet.ScoreboardDisplay;

public class PacketPlayOutScoreboardDisplayObjective extends UniversalPacketPlayOut {
	
	private static Class<?> PacketPlayOutScoreboardDisplayObjective;
	private static Constructor<?> newPacketPlayOutScoreboardDisplayObjective;
	private static Field POSITION;
	private static Field OBJECTIVENAME;
	
	private int slot;
	private String objectiveName;
	
	public static void initializeClass() throws Exception {
		try {
			//1.7+
			PacketPlayOutScoreboardDisplayObjective = getNMSClass("PacketPlayOutScoreboardDisplayObjective");
		} catch (ClassNotFoundException e) {
			//1.6-
			PacketPlayOutScoreboardDisplayObjective = getNMSClass("Packet208SetScoreboardDisplayObjective");
		}
		newPacketPlayOutScoreboardDisplayObjective = PacketPlayOutScoreboardDisplayObjective.getConstructor();
		(POSITION = PacketPlayOutScoreboardDisplayObjective.getDeclaredField("a")).setAccessible(true);
		(OBJECTIVENAME = PacketPlayOutScoreboardDisplayObjective.getDeclaredField("b")).setAccessible(true);
	}

	public PacketPlayOutScoreboardDisplayObjective(int slot, String objectiveName) {
		this.slot = slot;
		this.objectiveName = objectiveName;
	}
	
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		Object packet = newPacketPlayOutScoreboardDisplayObjective.newInstance();
		POSITION.set(packet, slot);
		OBJECTIVENAME.set(packet, objectiveName);
		return packet;
	}
	
	public Object toBungee(ProtocolVersion clientVersion) {
		return new ScoreboardDisplay((byte)slot, objectiveName);
	}
	
	public Object toVelocity(ProtocolVersion clientVersion) {
		return new me.neznamy.tab.platforms.velocity.protocol.ScoreboardDisplay((byte)slot, objectiveName);
	}
}