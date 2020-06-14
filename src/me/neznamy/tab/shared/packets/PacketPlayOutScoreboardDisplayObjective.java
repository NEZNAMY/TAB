package me.neznamy.tab.shared.packets;

import java.lang.reflect.Field;
import java.util.Map;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;
import net.md_5.bungee.protocol.packet.ScoreboardDisplay;

public class PacketPlayOutScoreboardDisplayObjective extends UniversalPacketPlayOut{
	
	private static Map<String, Field> fields = getFields(MethodAPI.PacketPlayOutScoreboardDisplayObjective);
	private static final Field POSITION = getField(fields, "a");
	private static final Field OBJECTIVENAME = getField(fields, "b");
	
	private int slot;
	private String objectiveName;

	public PacketPlayOutScoreboardDisplayObjective(int slot, String objectiveName) {
		this.slot = slot;
		this.objectiveName = objectiveName;
	}
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		Object packet = MethodAPI.getInstance().newPacketPlayOutScoreboardDisplayObjective();
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