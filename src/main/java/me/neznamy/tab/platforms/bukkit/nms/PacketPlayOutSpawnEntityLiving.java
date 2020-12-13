package me.neznamy.tab.platforms.bukkit.nms;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing the n.m.s.PacketPlayOutSpawnEntityLiving class to make work with it much easier
 */
public class PacketPlayOutSpawnEntityLiving extends PacketPlayOut {
	
	//map of entity type ids
	private static Map<EntityType, Integer> entityIds = new HashMap<EntityType, Integer>();

	//used NMS class, constructor and fields
	public static Class<?> PacketPlayOutSpawnEntityLiving;
	private static Constructor<?> newPacketPlayOutSpawnEntityLiving;
	private static Field ENTITYID;
	private static Field UUID;
	private static Field ENTITYTYPE;
	private static Field X;
	private static Field Y;
	private static Field Z;
	private static Field YAW;
	private static Field PITCH;
	public static Field DATAWATCHER;
	
	//entity id
	private int entityId;
	
	//entity uuid (1.9+)
	private UUID uuid;
	
	//entity type
	private int entityType;
	
	//spawn location
	private Location loc;
	
	//entity metadata (1.14 and lower)
	private DataWatcher dataWatcher;

	/**
	 * Initializes required NMS classes and fields
	 * @throws Exception - if something fails
	 */
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
			(YAW = PacketPlayOutSpawnEntityLiving.getDeclaredField("j")).setAccessible(true);
			(PITCH = PacketPlayOutSpawnEntityLiving.getDeclaredField("k")).setAccessible(true);
		} else {
			(ENTITYTYPE = PacketPlayOutSpawnEntityLiving.getDeclaredField("b")).setAccessible(true);
			(X = PacketPlayOutSpawnEntityLiving.getDeclaredField("c")).setAccessible(true);
			(Y = PacketPlayOutSpawnEntityLiving.getDeclaredField("d")).setAccessible(true);
			(Z = PacketPlayOutSpawnEntityLiving.getDeclaredField("e")).setAccessible(true);
			(YAW = PacketPlayOutSpawnEntityLiving.getDeclaredField("i")).setAccessible(true);
			(PITCH = PacketPlayOutSpawnEntityLiving.getDeclaredField("j")).setAccessible(true);
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 14) {
			(DATAWATCHER = getFields(PacketPlayOutSpawnEntityLiving, DataWatcher.DataWatcher).get(0)).setAccessible(true);
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
	
	/**
	 * Constructs new instance with given parameters
	 * @param entityId entity id
	 * @param uuid entity uuid
	 * @param entityType entity type
	 * @param loc spawn location
	 * @param dataWatcher entity metadata
	 */
	public PacketPlayOutSpawnEntityLiving(int entityId, UUID uuid, EntityType entityType, Location loc, DataWatcher dataWatcher) {
		this.entityId = entityId;
		this.uuid = uuid;
		this.entityType = entityIds.get(entityType);
		this.loc = loc;
		this.dataWatcher = dataWatcher;
	}
	
	/**
	 * Converts the custom class into an actual minecraft packet
	 * @param clientVersion client version to build the packet for
	 * @return NMS packet
	 * @throws Exception if something fails
	 */
	@Override
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		Object packet = newPacketPlayOutSpawnEntityLiving.newInstance();
		ENTITYID.set(packet, entityId);
		ENTITYTYPE.set(packet, entityType);
		YAW.set(packet, (byte)(loc.getYaw() * 256.0f / 360.0f));
		PITCH.set(packet, (byte)(loc.getPitch() * 256.0f / 360.0f));
		if (DATAWATCHER != null) {
			DATAWATCHER.set(packet, dataWatcher.toNMS());
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			UUID.set(packet, uuid);
			X.set(packet, loc.getX());
			Y.set(packet, loc.getY());
			Z.set(packet, loc.getZ());
		} else {
			X.set(packet, floor((double)loc.getX()*32));
			Y.set(packet, floor((double)loc.getY()*32));
			Z.set(packet, floor((double)loc.getZ()*32));
		}
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