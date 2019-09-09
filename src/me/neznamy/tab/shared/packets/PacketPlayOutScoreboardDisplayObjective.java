package me.neznamy.tab.shared.packets;

import java.lang.reflect.Field;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
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
		Object packet = MethodAPI.getInstance().newPacketPlayOutScoreboardDisplayObjective();
		PacketPlayOutScoreboardDisplayObjective_POSITION.set(packet, position);
		PacketPlayOutScoreboardDisplayObjective_OBJECTIVENAME.set(packet, objectiveName);
		return packet;
	}
	public Object toBungee(ProtocolVersion clientVersion) {
		return new ScoreboardDisplay((byte)position, objectiveName);
	}
	public Object toVelocity(ProtocolVersion clientVersion) {
		return null;
	}
	
	private static Class<?> PacketPlayOutScoreboardDisplayObjective;
	private static Field PacketPlayOutScoreboardDisplayObjective_POSITION;
	private static Field PacketPlayOutScoreboardDisplayObjective_OBJECTIVENAME;

	static {
		try {
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
				PacketPlayOutScoreboardDisplayObjective = getNMSClass("PacketPlayOutScoreboardDisplayObjective");
				(PacketPlayOutScoreboardDisplayObjective_POSITION = PacketPlayOutScoreboardDisplayObjective.getDeclaredField("a")).setAccessible(true);
				(PacketPlayOutScoreboardDisplayObjective_OBJECTIVENAME = PacketPlayOutScoreboardDisplayObjective.getDeclaredField("b")).setAccessible(true);
			}
		} catch (Exception e) {
			Shared.error("Failed to initialize PacketPlayOutScoreboardDisplayObjective", e);
		}
	}
}