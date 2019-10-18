package me.neznamy.tab.platforms.bukkit.packets;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import com.google.common.collect.Lists;

import me.neznamy.tab.platforms.bukkit.packets.DataWatcher.Item;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;

public class PacketPlayOutSpawnEntityLiving extends PacketPlayOut{

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
	public DataWatcher dataWatcher = new DataWatcher(null);
	private List<Item> watchableObjects;

	public PacketPlayOutSpawnEntityLiving(int entityId, UUID uuid, EntityType entityType, Location loc) {
		this(entityId, uuid, entityIds.get(entityType), loc);
	}
	public PacketPlayOutSpawnEntityLiving(int entityId, UUID uuid, int entityType, Location loc) {
		this.entityId = entityId;
		this.uuid = uuid;
		this.entityType = entityType;
		this.x = loc.getX();
		this.y = loc.getY();
		this.z = loc.getZ();
		this.yaw = loc.getYaw();
		this.pitch = loc.getPitch();
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
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		Object packet = MethodAPI.getInstance().newPacketPlayOutSpawnEntityLiving();
		ENTITYID.set(packet, entityId);
		ENTITYTYPE.set(packet, entityType);
		if (motX != 0) MOTX.set(packet, motX);
		if (motY != 0) MOTY.set(packet, motY);
		if (motZ != 0) MOTZ.set(packet, motZ);
		if (yaw != 0) YAW.set(packet, (byte)(yaw * 256.0f / 360.0f));
		if (pitch != 0) PITCH.set(packet, (byte)(pitch * 256.0f / 360.0f));
		if (l != 0) L.set(packet, (byte)(l * 256.0f / 360.0f));
		DATAWATCHER.set(packet, dataWatcher.toNMS());
		if (watchableObjects != null) {
			List<Object> list = Lists.newArrayList();
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
	@SuppressWarnings({ "unchecked" })
	public static PacketPlayOutSpawnEntityLiving fromNMS(Object nmsPacket) throws Exception{
		if (!MethodAPI.PacketPlayOutSpawnEntityLiving.isInstance(nmsPacket)) return null;
		int entityId = ENTITYID.getInt(nmsPacket);
		UUID uuid = null;
		int typeInt = ENTITYTYPE.getInt(nmsPacket);
		double x;
		double y;
		double z;
		int motX = MOTX.getInt(nmsPacket);
		int motY = MOTY.getInt(nmsPacket);
		int motZ = MOTZ.getInt(nmsPacket);
		float yaw = (float) (YAW.getByte(nmsPacket) / 256f * 360f);
		float pitch = (float) (PITCH.getByte(nmsPacket) / 256f * 360f);
		float l = (float) (L.getByte(nmsPacket) / 256f * 360f);
		DataWatcher dataWatcher = DataWatcher.fromNMS(DATAWATCHER.get(nmsPacket));
		List<Item> list = Lists.newArrayList();
		List<Object> items = (List<Object>)DATAWATCHERITEMS.get(nmsPacket);
		if (items != null) 
			for (Object o : items) {
				list.add(Item.fromNMS(o));
			}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			uuid = (UUID) UUID.get(nmsPacket);
			x = X.getDouble(nmsPacket);
			y = Y.getDouble(nmsPacket);
			z = Z.getDouble(nmsPacket);
		} else {
			x = (double)(X.getInt(nmsPacket)) / 32;
			y = (double)(Y.getInt(nmsPacket)) / 32;
			z = (double)(Z.getInt(nmsPacket)) / 32;
		}
		return new PacketPlayOutSpawnEntityLiving(entityId, uuid, typeInt, new Location(null, x,y,z,yaw,pitch)).setMotX(motX).setMotY(motY).setMotZ(motZ).setL(l).setDataWatcher(dataWatcher).setItems(list);
	}
	private int floor(double paramDouble){
		int i = (int)paramDouble;
		return paramDouble < i ? i - 1 : i;
	}

	private static HashMap<EntityType, Integer> entityIds = new HashMap<EntityType, Integer>();

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
	private static Field DATAWATCHER;
	private static Field DATAWATCHERITEMS;

	static {
		Map<String, Field> fields = getFields(MethodAPI.PacketPlayOutSpawnEntityLiving);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
			entityIds.put(EntityType.valueOf("ARMOR_STAND"), 1);
			entityIds.put(EntityType.WITHER, 83);
		} else {
			entityIds.put(EntityType.WITHER, 64);
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8){
				entityIds.put(EntityType.valueOf("ARMOR_STAND"), 30);
			}
		}
		ENTITYID = fields.get("a");
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			UUID = fields.get("b");
			ENTITYTYPE = fields.get("c");
			X = fields.get("d");
			Y = fields.get("e");
			Z = fields.get("f");
			MOTX = fields.get("g");
			MOTY = fields.get("h");
			MOTZ = fields.get("i");
			YAW = fields.get("j");
			PITCH = fields.get("k");
			L = fields.get("l");
			DATAWATCHER = fields.get("m");
			DATAWATCHERITEMS = fields.get("n");
		} else {
			ENTITYTYPE = fields.get("b");
			X = fields.get("c");
			Y = fields.get("d");
			Z = fields.get("e");
			MOTX = fields.get("f");
			MOTY = fields.get("g");
			MOTZ = fields.get("h");
			YAW = fields.get("i");
			PITCH = fields.get("j");
			L = fields.get("k");
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 7) {
				DATAWATCHER = fields.get("l");
				DATAWATCHERITEMS = fields.get("m");
			} else {
				DATAWATCHER = fields.get("t");
				DATAWATCHERITEMS = fields.get("u");
			}
		}
	}
}