package me.neznamy.tab.platforms.bukkit.nms;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.bukkit.Location;

import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing the n.m.s.PacketPlayOutEntityTeleport class to make work with it much easier
 */
public class PacketPlayOutEntityTeleport extends PacketPlayOut {

	//used NMS constructor and fields
	private static Constructor<?> newPacketPlayOutEntityTeleport;
	private static Field ENTITYID;
	private static Field X;
	private static Field Y;
	private static Field Z;
	private static Field YAW;
	private static Field PITCH;
	
	//entity id
	private int entityId;
	
	//location to teleport to
	private Location location;
	
	/**
	 * Initializes required NMS classes and fields
	 * @throws Exception - if something fails
	 */
	public static void initializeClass() throws Exception {
		Class<?> PacketPlayOutEntityTeleport;
		try {
			//1.7+
			PacketPlayOutEntityTeleport = getNMSClass("PacketPlayOutEntityTeleport");
		} catch (ClassNotFoundException e) {
			//1.6-
			PacketPlayOutEntityTeleport = getNMSClass("Packet34EntityTeleport");
		}
		newPacketPlayOutEntityTeleport = PacketPlayOutEntityTeleport.getConstructor();
		(ENTITYID = PacketPlayOutEntityTeleport.getDeclaredField("a")).setAccessible(true);
		(X = PacketPlayOutEntityTeleport.getDeclaredField("b")).setAccessible(true);
		(Y = PacketPlayOutEntityTeleport.getDeclaredField("c")).setAccessible(true);
		(Z = PacketPlayOutEntityTeleport.getDeclaredField("d")).setAccessible(true);
		(YAW = PacketPlayOutEntityTeleport.getDeclaredField("e")).setAccessible(true);
		(PITCH = PacketPlayOutEntityTeleport.getDeclaredField("f")).setAccessible(true);
	}
	
	/**
	 * Constructs new instance with given parameters
	 * @param entityId entity id
	 * @param location location to teleport to
	 */
	public PacketPlayOutEntityTeleport(int entityId, Location location) {
		this.entityId = entityId;
		this.location = location;
	}
	
	/**
	 * Converts the custom class into an actual minecraft packet
	 * @param clientVersion client version to build the packet for
	 * @return NMS packet
	 * @throws Exception if something fails
	 */
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
		if (location.getYaw() != 0) YAW.set(packet, (byte)((float)location.getYaw()/360*256));
		if (location.getPitch() != 0) PITCH.set(packet, (byte)((float)location.getPitch()/360*256));
		return packet;
	}
	
	/**
	 * A method yoinked from minecraft code used to convert double to int
	 * @param paramDouble double value
	 * @return int value
	 */
	private int floor(double paramDouble){
		int i = (int)paramDouble;
		return paramDouble < i ? i - 1 : i;
	}
}