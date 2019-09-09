package me.neznamy.tab.platforms.bukkit.packets;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import com.google.common.collect.Lists;

import me.neznamy.tab.platforms.bukkit.packets.DataWatcher.Item;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;

public class PacketPlayOutSpawnEntityLiving extends PacketPlayOut{

	private int entityId;
	private UUID uuid;
	private int entityType;
	private double X;
	private double Y;
	private double Z;
	private int motX;
	private int motY;
	private int motZ;
	private float yaw;
	private float pitch;
	private float l;
	private DataWatcher dataWatcher;
	private List<Item> watchableObjects;

	public PacketPlayOutSpawnEntityLiving() {
		dataWatcher = new DataWatcher(null);
	}
	public PacketPlayOutSpawnEntityLiving(int entityId, UUID uuid, EntityType entityType, Location loc) {
		this(entityId, uuid, entityIds.get(entityType), loc);
	}
	public PacketPlayOutSpawnEntityLiving(int entityId, UUID uuid, int entityType, Location loc) {
		this.entityId = entityId;
		this.uuid = uuid;
		this.entityType = entityType;
		this.X = loc.getX();
		this.Y = loc.getY();
		this.Z = loc.getZ();
		this.yaw = loc.getYaw();
		this.pitch = loc.getPitch();
		dataWatcher = new DataWatcher(null);
	}
	public PacketPlayOutSpawnEntityLiving setMotX(int motX) {
		this.motX = motX;
		return this;
	}
	public PacketPlayOutSpawnEntityLiving setMotY(int motY) {
		this.motY = motY;
		return this;
	}
	public PacketPlayOutSpawnEntityLiving setMotZ(int motZ) {
		this.motZ = motZ;
		return this;
	}
	public PacketPlayOutSpawnEntityLiving setDataWatcher(DataWatcher dataWatcher) {
		this.dataWatcher = dataWatcher;
		return this;
	}
	public PacketPlayOutSpawnEntityLiving setL(float l) {
		this.l = l;
		return this;
	}
	public PacketPlayOutSpawnEntityLiving setItems(List<Item> watchableObjects) {
		this.watchableObjects = watchableObjects;
		return this;
	}
	public int getEntityId() {
		return entityId;
	}
	public DataWatcher getDataWatcher() {
		return dataWatcher;
	}
	public Object toNMS() throws Exception {
		Object packet = MethodAPI.getInstance().newPacketPlayOutSpawnEntityLiving();
		PacketPlayOutSpawnEntityLiving_ENTITYID.set(packet, entityId);
		PacketPlayOutSpawnEntityLiving_ENTITYTYPE.set(packet, entityType);
		if (motX != 0) PacketPlayOutSpawnEntityLiving_MOTX.set(packet, motX);
		if (motY != 0) PacketPlayOutSpawnEntityLiving_MOTY.set(packet, motY);
		if (motZ != 0) PacketPlayOutSpawnEntityLiving_MOTZ.set(packet, motZ);
		if (yaw != 0) PacketPlayOutSpawnEntityLiving_YAW.set(packet, (byte)(yaw * 256.0f / 360.0f));
		if (pitch != 0) PacketPlayOutSpawnEntityLiving_PITCH.set(packet, (byte)(pitch * 256.0f / 360.0f));
		if (l != 0) PacketPlayOutSpawnEntityLiving_L.set(packet, (byte)(l * 256.0f / 360.0f));
		PacketPlayOutSpawnEntityLiving_DATAWATCHER.set(packet, dataWatcher.toNMS());
		if (watchableObjects != null) {
			List<Object> list = Lists.newArrayList();
			for (Item o : this.watchableObjects) {
				list.add(o.toNMS());
			}
			PacketPlayOutSpawnEntityLiving_DATAWATCHERITEMS.set(packet, list);
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			PacketPlayOutSpawnEntityLiving_UUID.set(packet, uuid);
			if (X != 0) PacketPlayOutSpawnEntityLiving_X.set(packet, X);
			if (Y != 0) PacketPlayOutSpawnEntityLiving_Y.set(packet, Y);
			if (Z != 0) PacketPlayOutSpawnEntityLiving_Z.set(packet, Z);
		} else {
			if (X != 0) PacketPlayOutSpawnEntityLiving_X.set(packet, floor((double)X*32));
			if (Y != 0) PacketPlayOutSpawnEntityLiving_Y.set(packet, floor((double)Y*32));
			if (Z != 0) PacketPlayOutSpawnEntityLiving_Z.set(packet, floor((double)Z*32));
		}
		return packet;
	}
	@SuppressWarnings({ "unchecked" })
	public static PacketPlayOutSpawnEntityLiving fromNMS(Object nmsPacket) throws Exception{
		if (!PacketPlayOutSpawnEntityLiving.isInstance(nmsPacket)) return null;
		int entityId = PacketPlayOutSpawnEntityLiving_ENTITYID.getInt(nmsPacket);
		UUID uuid = null;
		int typeInt = PacketPlayOutSpawnEntityLiving_ENTITYTYPE.getInt(nmsPacket);
		double x;
		double y;
		double z;
		int motX = PacketPlayOutSpawnEntityLiving_MOTX.getInt(nmsPacket);
		int motY = PacketPlayOutSpawnEntityLiving_MOTY.getInt(nmsPacket);
		int motZ = PacketPlayOutSpawnEntityLiving_MOTZ.getInt(nmsPacket);
		float yaw = (float) (PacketPlayOutSpawnEntityLiving_YAW.getByte(nmsPacket) / 256f * 360f);
		float pitch = (float) (PacketPlayOutSpawnEntityLiving_PITCH.getByte(nmsPacket) / 256f * 360f);
		float l = (float) (PacketPlayOutSpawnEntityLiving_L.getByte(nmsPacket) / 256f * 360f);
		DataWatcher dataWatcher = DataWatcher.fromNMS(PacketPlayOutSpawnEntityLiving_DATAWATCHER.get(nmsPacket));
		List<Item> list = Lists.newArrayList();
		List<Object> items = (List<Object>)PacketPlayOutSpawnEntityLiving_DATAWATCHERITEMS.get(nmsPacket);
		if (items != null) 
			for (Object o : items) {
				list.add(Item.fromNMS(o));
			}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			uuid = (UUID) PacketPlayOutSpawnEntityLiving_UUID.get(nmsPacket);
			x = PacketPlayOutSpawnEntityLiving_X.getDouble(nmsPacket);
			y = PacketPlayOutSpawnEntityLiving_Y.getDouble(nmsPacket);
			z = PacketPlayOutSpawnEntityLiving_Z.getDouble(nmsPacket);
		} else {
			x = (double)(PacketPlayOutSpawnEntityLiving_X.getInt(nmsPacket)) / 32;
			y = (double)(PacketPlayOutSpawnEntityLiving_Y.getInt(nmsPacket)) / 32;
			z = (double)(PacketPlayOutSpawnEntityLiving_Z.getInt(nmsPacket)) / 32;
		}
		return new PacketPlayOutSpawnEntityLiving(entityId, uuid, typeInt, new Location(null, x,y,z,yaw,pitch)).setMotX(motX).setMotY(motY).setMotZ(motZ).setL(l).setDataWatcher(dataWatcher).setItems(list);
	}
	private int floor(double paramDouble){
		int i = (int)paramDouble;
		return paramDouble < i ? i - 1 : i;
	}

	private static HashMap<EntityType, Integer> entityIds = new HashMap<EntityType, Integer>();

	private static Class<?> PacketPlayOutSpawnEntityLiving;
	private static Field PacketPlayOutSpawnEntityLiving_ENTITYID;
	private static Field PacketPlayOutSpawnEntityLiving_UUID;
	private static Field PacketPlayOutSpawnEntityLiving_ENTITYTYPE;
	private static Field PacketPlayOutSpawnEntityLiving_X;
	private static Field PacketPlayOutSpawnEntityLiving_Y;
	private static Field PacketPlayOutSpawnEntityLiving_Z;
	private static Field PacketPlayOutSpawnEntityLiving_MOTX;
	private static Field PacketPlayOutSpawnEntityLiving_MOTY;
	private static Field PacketPlayOutSpawnEntityLiving_MOTZ;
	private static Field PacketPlayOutSpawnEntityLiving_YAW;
	private static Field PacketPlayOutSpawnEntityLiving_PITCH;
	private static Field PacketPlayOutSpawnEntityLiving_L;
	private static Field PacketPlayOutSpawnEntityLiving_DATAWATCHER;
	private static Field PacketPlayOutSpawnEntityLiving_DATAWATCHERITEMS;

	static {
		try {
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
				entityIds.put(EntityType.ARMOR_STAND, 1);
				entityIds.put(EntityType.WITHER, 83);
			} else {
				entityIds.put(EntityType.ARMOR_STAND, 30);
				entityIds.put(EntityType.WITHER, 64);
			}

			PacketPlayOutSpawnEntityLiving = getClass("PacketPlayOutSpawnEntityLiving");
			(PacketPlayOutSpawnEntityLiving_ENTITYID = PacketPlayOutSpawnEntityLiving.getDeclaredField("a")).setAccessible(true);
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
				(PacketPlayOutSpawnEntityLiving_UUID = PacketPlayOutSpawnEntityLiving.getDeclaredField("b")).setAccessible(true);
				(PacketPlayOutSpawnEntityLiving_ENTITYTYPE = PacketPlayOutSpawnEntityLiving.getDeclaredField("c")).setAccessible(true);
				(PacketPlayOutSpawnEntityLiving_X = PacketPlayOutSpawnEntityLiving.getDeclaredField("d")).setAccessible(true);
				(PacketPlayOutSpawnEntityLiving_Y = PacketPlayOutSpawnEntityLiving.getDeclaredField("e")).setAccessible(true);
				(PacketPlayOutSpawnEntityLiving_Z = PacketPlayOutSpawnEntityLiving.getDeclaredField("f")).setAccessible(true);
				(PacketPlayOutSpawnEntityLiving_MOTX = PacketPlayOutSpawnEntityLiving.getDeclaredField("g")).setAccessible(true);
				(PacketPlayOutSpawnEntityLiving_MOTY = PacketPlayOutSpawnEntityLiving.getDeclaredField("h")).setAccessible(true);
				(PacketPlayOutSpawnEntityLiving_MOTZ = PacketPlayOutSpawnEntityLiving.getDeclaredField("i")).setAccessible(true);
				(PacketPlayOutSpawnEntityLiving_YAW = PacketPlayOutSpawnEntityLiving.getDeclaredField("j")).setAccessible(true);
				(PacketPlayOutSpawnEntityLiving_PITCH = PacketPlayOutSpawnEntityLiving.getDeclaredField("k")).setAccessible(true);
				(PacketPlayOutSpawnEntityLiving_L = PacketPlayOutSpawnEntityLiving.getDeclaredField("l")).setAccessible(true);
				(PacketPlayOutSpawnEntityLiving_DATAWATCHER = PacketPlayOutSpawnEntityLiving.getDeclaredField("m")).setAccessible(true);
				(PacketPlayOutSpawnEntityLiving_DATAWATCHERITEMS = PacketPlayOutSpawnEntityLiving.getDeclaredField("n")).setAccessible(true);
			} else {
				(PacketPlayOutSpawnEntityLiving_ENTITYTYPE = PacketPlayOutSpawnEntityLiving.getDeclaredField("b")).setAccessible(true);
				(PacketPlayOutSpawnEntityLiving_X = PacketPlayOutSpawnEntityLiving.getDeclaredField("c")).setAccessible(true);
				(PacketPlayOutSpawnEntityLiving_Y = PacketPlayOutSpawnEntityLiving.getDeclaredField("d")).setAccessible(true);
				(PacketPlayOutSpawnEntityLiving_Z = PacketPlayOutSpawnEntityLiving.getDeclaredField("e")).setAccessible(true);
				(PacketPlayOutSpawnEntityLiving_MOTX = PacketPlayOutSpawnEntityLiving.getDeclaredField("f")).setAccessible(true);
				(PacketPlayOutSpawnEntityLiving_MOTY = PacketPlayOutSpawnEntityLiving.getDeclaredField("g")).setAccessible(true);
				(PacketPlayOutSpawnEntityLiving_MOTZ = PacketPlayOutSpawnEntityLiving.getDeclaredField("h")).setAccessible(true);
				(PacketPlayOutSpawnEntityLiving_YAW = PacketPlayOutSpawnEntityLiving.getDeclaredField("i")).setAccessible(true);
				(PacketPlayOutSpawnEntityLiving_PITCH = PacketPlayOutSpawnEntityLiving.getDeclaredField("j")).setAccessible(true);
				(PacketPlayOutSpawnEntityLiving_L = PacketPlayOutSpawnEntityLiving.getDeclaredField("k")).setAccessible(true);
				(PacketPlayOutSpawnEntityLiving_DATAWATCHER = PacketPlayOutSpawnEntityLiving.getDeclaredField("l")).setAccessible(true);
				(PacketPlayOutSpawnEntityLiving_DATAWATCHERITEMS = PacketPlayOutSpawnEntityLiving.getDeclaredField("m")).setAccessible(true);
			}
		} catch (Throwable e) {
			Shared.error("Failed to initialize PacketPlayOutSpawnEntityLiving class", e);
		}
	}
}