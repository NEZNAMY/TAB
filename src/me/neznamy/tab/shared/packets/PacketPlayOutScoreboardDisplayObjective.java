package me.neznamy.tab.shared.packets;

import java.lang.reflect.Field;
import java.util.Map;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;
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
		POSITION.set(packet, position);
		OBJECTIVENAME.set(packet, objectiveName);
		return packet;
	}
	public Object toBungee(ProtocolVersion clientVersion) {
		return new ScoreboardDisplay((byte)position, objectiveName);
	}
	public Object toVelocity(ProtocolVersion clientVersion) {
		return null;
	}

	private static Map<String, Field> fields = getFields(MethodAPI.PacketPlayOutScoreboardDisplayObjective);
	private static Field POSITION = fields.get("a");
	private static Field OBJECTIVENAME = fields.get("b");
}