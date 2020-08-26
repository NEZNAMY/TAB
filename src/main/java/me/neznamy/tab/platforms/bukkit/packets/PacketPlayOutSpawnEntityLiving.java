package me.neznamy.tab.platforms.bukkit.packets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import me.neznamy.tab.platforms.bukkit.packets.DataWatcher.Item;
import me.neznamy.tab.shared.ProtocolVersion;

public class PacketPlayOutSpawnEntityLiving extends PacketPlayOut {
	
	private static Map<EntityType, Integer> entityIds = new HashMap<EntityType, Integer>();

	public static Class<?> PacketPlayOutSpawnEntityLiving;
	private static Constructor<?> newPacketPlayOutSpawnEntityLiving;
	private static Field ENTITYID;
	private static Field UUID;
	private static Field ENTITYTYPE;
	private static Field X;
	private static Field Y;
	private static Field Z;
	private static Field MOTX;
	private static Field MOTY;
	private static Field MOTZ;
	private static Field YAW;
	private static Field PITCH;
	private static Field L;
	public static Field DATAWATCHER;
	private static Field DATAWATCHERITEMS;
	
	private int entityId;
	private UUID uuid;
	private int entityType;
	private double x;
	private double y;
	private double z;
	private int motX;
	private int motY;
	private int motZ;
	private float yaw;
	private float pitch;
	private float l;
	public DataWatcher dataWatcher = new DataWatcher();
	private List<Item> watchableObjects;

	public static void initializeClass() throws Exception {
		try {
			PacketPlayOutSpawnEntityLiving = getNMSClass("PacketPlayOutSpawnEntityLiving");
		} catch (ClassNotFoundException e) {
			PacketPlayOutSpawnEntityLiving = getNMSClass("Packet24MobSpawn");
		}
		newPacketPlayOutSpawnEntityLiving = PacketPlayOutSpawnEntityLiving.getConstructor();
		(ENTITYID = PacketPlayOutSpawnEntityLiving.getDeclaredField("a")).setAccessible(true);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			(UUID = PacketPlayOutSpawnEntityLiving.getDeclaredField("b")).setAccessible(true);
			(ENTITYTYPE = PacketPlayOutSpawnEntityLiving.getDeclaredField("c")).setAccessible(true);
			(X = PacketPlayOutSpawnEntityLiving.getDeclaredField("d")).setAccessible(true);
			(Y = PacketPlayOutSpawnEntityLiving.getDeclaredField("e")).setAccessible(true);
			(Z = PacketPlayOutSpawnEntityLiving.getDeclaredField("f")).setAccessible(true);
			(MOTX = PacketPlayOutSpawnEntityLiving.getDeclaredField("g")).setAccessible(true);
			(MOTY = PacketPlayOutSpawnEntityLiving.getDeclaredField("h")).setAccessible(true);
			(MOTZ = PacketPlayOutSpawnEntityLiving.getDeclaredField("i")).setAccessible(true);
			(YAW = PacketPlayOutSpawnEntityLiving.getDeclaredField("j")).setAccessible(true);
			(PITCH = PacketPlayOutSpawnEntityLiving.getDeclaredField("k")).setAccessible(true);
			(L = PacketPlayOutSpawnEntityLiving.getDeclaredField("l")).setAccessible(true);
		} else {
			(ENTITYTYPE = PacketPlayOutSpawnEntityLiving.getDeclaredField("b")).setAccessible(true);
			(X = PacketPlayOutSpawnEntityLiving.getDeclaredField("c")).setAccessible(true);
			(Y = PacketPlayOutSpawnEntityLiving.getDeclaredField("d")).setAccessible(true);
			(Z = PacketPlayOutSpawnEntityLiving.getDeclaredField("e")).setAccessible(true);
			(MOTX = PacketPlayOutSpawnEntityLiving.getDeclaredField("f")).setAccessible(true);
			(MOTY = PacketPlayOutSpawnEntityLiving.getDeclaredField("g")).setAccessible(true);
			(MOTZ = PacketPlayOutSpawnEntityLiving.getDeclaredField("h")).setAccessible(true);
			(YAW = PacketPlayOutSpawnEntityLiving.getDeclaredField("i")).setAccessible(true);
			(PITCH = PacketPlayOutSpawnEntityLiving.getDeclaredField("j")).setAccessible(true);
			(L = PacketPlayOutSpawnEntityLiving.getDeclaredField("k")).setAccessible(true);
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 14) {
			(DATAWATCHER = getFields(PacketPlayOutSpawnEntityLiving, DataWatcher.DataWatcher).get(0)).setAccessible(true);
			(DATAWATCHERITEMS = getFields(PacketPlayOutSpawnEntityLiving, List.class).get(0)).setAccessible(true);
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
			entityIds.put(EntityType.ARMOR_STAND, 1);
			entityIds.put(EntityType.WITHER, 83);
		} else {
			entityIds.put(EntityType.WITHER, 64);
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8){
				entityIds.put(EntityType.ARMOR_STAND, 30);
			}
		}
	}
	
	public PacketPlayOutSpawnEntityLiving(int entityId, UUID uuid, EntityType entityType, Location loc) {
		this(entityId, uuid, entityIds.get(entityType), loc);
	}
	
	public PacketPlayOutSpawnEntityLiving(int entityId, UUID uuid, int entityType, Location loc) {
		if (loc == null) throw new IllegalArgumentException("Location cannot be null");
		this.entityId = entityId;
		this.uuid = uuid;
		this.entityType = entityType;
		this.x = loc.getX();
		this.y = loc.getY();
		this.z = loc.getZ();
		this.yaw = loc.getYaw();
		this.pitch = loc.getPitch();
	}
	
	public PacketPlayOutSpawnEntityLiving setDataWatcher(DataWatcher dataWatcher) {
		this.dataWatcher = dataWatcher;
		return this;
	}
	
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		Object packet = newPacketPlayOutSpawnEntityLiving.newInstance();
		ENTITYID.set(packet, entityId);
		ENTITYTYPE.set(packet, entityType);
		if (motX != 0) MOTX.set(packet, motX);
		if (motY != 0) MOTY.set(packet, motY);
		if (motZ != 0) MOTZ.set(packet, motZ);
		if (yaw != 0) YAW.set(packet, (byte)(yaw * 256.0f / 360.0f));
		if (pitch != 0) PITCH.set(packet, (byte)(pitch * 256.0f / 360.0f));
		if (l != 0) L.set(packet, (byte)(l * 256.0f / 360.0f));
		if (DATAWATCHER != null && dataWatcher != null) DATAWATCHER.set(packet, dataWatcher.toNMS());
		if (watchableObjects != null) {
			List<Object> list = new ArrayList<Object>();
			for (Item o : this.watchableObjects) {
				list.add(o.toNMS());
			}
			DATAWATCHERITEMS.set(packet, list);
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			UUID.set(packet, uuid);
			if (x != 0) X.set(packet, x);
			if (y != 0) Y.set(packet, y);
			if (z != 0) Z.set(packet, z);
		} else {
			if (x != 0) X.set(packet, floor((double)x*32));
			if (y != 0) Y.set(packet, floor((double)y*32));
			if (z != 0) Z.set(packet, floor((double)z*32));
		}
		return packet;
	}
	
	private int floor(double paramDouble){
		int i = (int)paramDouble;
		return paramDouble < i ? i - 1 : i;
	}
}