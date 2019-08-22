package me.neznamy.tab.bukkit.unlimitedtags;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import me.neznamy.tab.bukkit.packets.PacketPlayOut;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;

public class PacketPlayOutEntityTeleport extends PacketPlayOut{

	private int entityId;
	private double x;
	private double y;
	private double z;
	private float yaw;
	private float pitch;
	private boolean onGround;
	
	public PacketPlayOutEntityTeleport(Entity entity) {
		this(entity.getEntityId(), entity.getLocation());
		onGround = entity.isOnGround();
	}
	public PacketPlayOutEntityTeleport(int entityId, Location loc) {
		this(entityId, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
	}
	public PacketPlayOutEntityTeleport(int entityId, double x, double y, double z, float yaw, float pitch) {
		this.entityId = entityId;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}
	public Object toNMS() throws Exception {
		Object packet = newPacketPlayOutEntityTeleport.newInstance();
		PacketPlayOutEntityTeleport_ENTITYID.set(packet, entityId);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			PacketPlayOutEntityTeleport_X.set(packet, x);
			PacketPlayOutEntityTeleport_Y.set(packet, y);
			PacketPlayOutEntityTeleport_Z.set(packet, z);
		} else {
			PacketPlayOutEntityTeleport_X.set(packet, floor(x*32));
			PacketPlayOutEntityTeleport_Y.set(packet, floor(y*32));
			PacketPlayOutEntityTeleport_Z.set(packet, floor(z*32));
		}
		if (yaw != 0) PacketPlayOutEntityTeleport_YAW.set(packet, (byte)(yaw * 256.0f / 360.0f));
		if (pitch != 0) PacketPlayOutEntityTeleport_PITCH.set(packet, (byte)(pitch * 256.0f / 360.0f));
		if (onGround) PacketPlayOutEntityTeleport_ONGROUND.set(packet, onGround);
		return packet;
	}
	private int floor(double paramDouble){
		int i = (int)paramDouble;
		return paramDouble < i ? i - 1 : i;
	}

	private static Class<?> PacketPlayOutEntityTeleport;
	private static Constructor<?> newPacketPlayOutEntityTeleport;
	private static Field PacketPlayOutEntityTeleport_ENTITYID;
	private static Field PacketPlayOutEntityTeleport_X;
	private static Field PacketPlayOutEntityTeleport_Y;
	private static Field PacketPlayOutEntityTeleport_Z;
	private static Field PacketPlayOutEntityTeleport_YAW;
	private static Field PacketPlayOutEntityTeleport_PITCH;
	private static Field PacketPlayOutEntityTeleport_ONGROUND;


	static {
		try {
			PacketPlayOutEntityTeleport = getNMSClass("PacketPlayOutEntityTeleport");
			newPacketPlayOutEntityTeleport = PacketPlayOutEntityTeleport.getConstructor();
			(PacketPlayOutEntityTeleport_ENTITYID = PacketPlayOutEntityTeleport.getDeclaredField("a")).setAccessible(true);
			(PacketPlayOutEntityTeleport_X = PacketPlayOutEntityTeleport.getDeclaredField("b")).setAccessible(true);
			(PacketPlayOutEntityTeleport_Y = PacketPlayOutEntityTeleport.getDeclaredField("c")).setAccessible(true);
			(PacketPlayOutEntityTeleport_Z = PacketPlayOutEntityTeleport.getDeclaredField("d")).setAccessible(true);
			(PacketPlayOutEntityTeleport_YAW = PacketPlayOutEntityTeleport.getDeclaredField("e")).setAccessible(true);
			(PacketPlayOutEntityTeleport_PITCH = PacketPlayOutEntityTeleport.getDeclaredField("f")).setAccessible(true);
			(PacketPlayOutEntityTeleport_ONGROUND = PacketPlayOutEntityTeleport.getDeclaredField("g")).setAccessible(true);
		} catch (Throwable e) {
			Shared.error("Failed to initialize PacketPlayOutEntityTeleport class", e);
		}
	}
}