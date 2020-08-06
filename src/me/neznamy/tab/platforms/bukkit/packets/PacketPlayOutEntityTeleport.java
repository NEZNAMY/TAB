package me.neznamy.tab.platforms.bukkit.packets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

import org.bukkit.Location;

import me.neznamy.tab.shared.ProtocolVersion;

public class PacketPlayOutEntityTeleport extends PacketPlayOut {

	private static Class<?> PacketPlayOutEntityTeleport = getNMSClass("PacketPlayOutEntityTeleport", "Packet34EntityTeleport");
	private static Constructor<?> newPacketPlayOutEntityTeleport = getConstructor(PacketPlayOutEntityTeleport, 0);
	private static Map<String, Field> fields = getFields(PacketPlayOutEntityTeleport);
	private static final Field ENTITYID = fields.get("a");
	private static final Field X = fields.get("b");
	private static final Field Y = fields.get("c");
	private static final Field Z = fields.get("d");
	private static final Field YAW = fields.get("e");
	private static final Field PITCH = fields.get("f");
	@SuppressWarnings("unused")
	private static final Field ONGROUND = fields.get("g");
	
	private int entityId;
	private Location location;
	
	public PacketPlayOutEntityTeleport(int entityId, Location location) {
		this.entityId = entityId;
		this.location = location;
	}
	
	@Override
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		Object packet = newPacketPlayOutEntityTeleport.newInstance();
		ENTITYID.set(packet, entityId);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			X.set(packet, location.getX());
			Y.set(packet, location.getY());
			Z.set(packet, location.getZ());
		} else {
			X.set(packet, floor((double)location.getX()*32));
			Y.set(packet, floor((double)location.getY()*32));
			Z.set(packet, floor((double)location.getZ()*32));
		}
		if (location.getYaw() != 0) YAW.set(packet, (byte)((float)location.getYaw()/360/256));
		if (location.getPitch() != 0) PITCH.set(packet, (byte)((float)location.getPitch()/360/256));
		return packet;
	}
	
	private int floor(double paramDouble){
		int i = (int)paramDouble;
		return paramDouble < i ? i - 1 : i;
	}
}