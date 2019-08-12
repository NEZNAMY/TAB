package me.neznamy.tab.shared.packets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import net.md_5.bungee.protocol.packet.ScoreboardDisplay;

public class PacketPlayOutScoreboardDisplayObjective extends UniversalPacketPlayOut{
	
    private int position;
    private String objectiveName;

    public PacketPlayOutScoreboardDisplayObjective(int position, String objectiveName) {
        this.position = position;
        this.objectiveName = objectiveName;
    }
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		Object packet = newPacketPlayOutScoreboardDisplayObjective.newInstance();
		PacketPlayOutScoreboardDisplayObjective_POSITION.set(packet, position);
		PacketPlayOutScoreboardDisplayObjective_OBJECTIVENAME.set(packet, objectiveName);
		return packet;
	}
	public Object toBungee(ProtocolVersion clientVersion) {
		return new ScoreboardDisplay((byte)position, objectiveName);
	}
	
	private static Class<?> PacketPlayOutScoreboardDisplayObjective;
	private static Constructor<?> newPacketPlayOutScoreboardDisplayObjective;
	private static Field PacketPlayOutScoreboardDisplayObjective_POSITION;
	private static Field PacketPlayOutScoreboardDisplayObjective_OBJECTIVENAME;

	static {
		try {
			if (versionNumber >= 8) {
				PacketPlayOutScoreboardDisplayObjective = getNMSClass("PacketPlayOutScoreboardDisplayObjective");
				newPacketPlayOutScoreboardDisplayObjective = PacketPlayOutScoreboardDisplayObjective.getConstructor();
				(PacketPlayOutScoreboardDisplayObjective_POSITION = PacketPlayOutScoreboardDisplayObjective.getDeclaredField("a")).setAccessible(true);
				(PacketPlayOutScoreboardDisplayObjective_OBJECTIVENAME = PacketPlayOutScoreboardDisplayObjective.getDeclaredField("b")).setAccessible(true);
			}
		} catch (Exception e) {
			Shared.error("Failed to initialize PacketPlayOutScoreboardDisplayObjective", e);
		}
	}
}