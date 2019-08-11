package me.neznamy.tab.bukkit.packets;

import java.lang.reflect.Field;

import me.neznamy.tab.shared.Shared;

public class NameTagXPacket {

	private PacketType type;
	private int entityId;
	private int[] entityArray;
	private int extra;
	
	public NameTagXPacket(PacketType type, int entityId, int[] entityArray, int extra) {
		this.type = type;
		this.entityId = entityId;
		this.entityArray = entityArray;
		this.extra = extra;
	}
	public PacketType getPacketType() {
		return type;
	}
	public int getEntityId() {
		return entityId;
	}
	public int[] getEntityArray() {
		return entityArray;
	}
	public int getExtra() {
		return extra;
	}
	public static NameTagXPacket fromNMS(Object nmsPacket) throws Exception {
		if (PacketPlayOutNamedEntitySpawn.isInstance(nmsPacket)) return new NameTagXPacket(PacketType.NAMED_ENTITY_SPAWN, PacketPlayOutNamedEntitySpawn_ENTITYID.getInt(nmsPacket), null, -1);
		if (PacketPlayOutEntityDestroy.isInstance(nmsPacket)) return new NameTagXPacket(PacketType.ENTITY_DESTROY, -1, (int[]) PacketPlayOutEntityDestroy_ENTITIES.get(nmsPacket), -1);
		if (PacketPlayOutEntityTeleport.isInstance(nmsPacket)) return new NameTagXPacket(PacketType.ENTITY_TELEPORT, PacketPlayOutEntityTeleport_ENTITYID.getInt(nmsPacket), null, -1);
		if (PacketPlayOutRelEntityMove.isInstance(nmsPacket)) return new NameTagXPacket(PacketType.ENTITY_MOVE, PacketPlayOutEntity_ENTITYID.getInt(nmsPacket), null, -1);
		if (PacketPlayOutRelEntityMoveLook.isInstance(nmsPacket)) return new NameTagXPacket(PacketType.ENTITY_MOVE, PacketPlayOutEntity_ENTITYID.getInt(nmsPacket), null, -1);
		if (PacketPlayOutMount != null && PacketPlayOutMount.isInstance(nmsPacket)) return new NameTagXPacket(PacketType.MOUNT, PacketPlayOutMount_VEHICLE.getInt(nmsPacket), (int[]) PacketPlayOutMount_PASSENGERS.get(nmsPacket), -1);
		if (PacketPlayOutAttachEntity != null && PacketPlayOutAttachEntity.isInstance(nmsPacket)) return new NameTagXPacket(PacketType.ATTACH_ENTITY, PacketPlayOutAttachEntity_VEHICLE.getInt(nmsPacket), new int[] {PacketPlayOutAttachEntity_PASSENGER.getInt(nmsPacket)}, PacketPlayOutAttachEntity_A.getInt(nmsPacket));
		return null;
	}
	
	public static enum PacketType{
		NAMED_ENTITY_SPAWN, //spawning armor stand
		ENTITY_DESTROY, //destroying armor stand
		ENTITY_TELEPORT, //teleporting armor stand
		ENTITY_MOVE, //teleporting armor stand
		MOUNT, //1.9+ mount detection
		@Deprecated
		ATTACH_ENTITY; //1.8.x mount detection
	}
	
	private static Class<?> PacketPlayOutNamedEntitySpawn;
	private static Field PacketPlayOutNamedEntitySpawn_ENTITYID;
	
	private static Class<?> PacketPlayOutEntityDestroy;
	private static Field PacketPlayOutEntityDestroy_ENTITIES;
	
	private static Class<?> PacketPlayOutEntityTeleport;
	private static Field PacketPlayOutEntityTeleport_ENTITYID;
	
	private static Class<?> PacketPlayOutRelEntityMove;
	private static Class<?> PacketPlayOutRelEntityMoveLook;
	private static Field PacketPlayOutEntity_ENTITYID;
	
	private static Class<?> PacketPlayOutMount;
	private static Field PacketPlayOutMount_VEHICLE;
	private static Field PacketPlayOutMount_PASSENGERS;
	
	private static Class<?> PacketPlayOutAttachEntity;
	private static Field PacketPlayOutAttachEntity_A;
	private static Field PacketPlayOutAttachEntity_PASSENGER;
	private static Field PacketPlayOutAttachEntity_VEHICLE;
	
	static {
		try {
			if (NMSClass.versionNumber == 8) {
				PacketPlayOutAttachEntity = NMSClass.get("PacketPlayOutAttachEntity");
				(PacketPlayOutAttachEntity_A = PacketPlayOutAttachEntity.getDeclaredField("a")).setAccessible(true);
				(PacketPlayOutAttachEntity_PASSENGER = PacketPlayOutAttachEntity.getDeclaredField("b")).setAccessible(true);
				(PacketPlayOutAttachEntity_VEHICLE = PacketPlayOutAttachEntity.getDeclaredField("c")).setAccessible(true);
			}
			if (NMSClass.versionNumber >= 8) {
				(PacketPlayOutNamedEntitySpawn_ENTITYID = (PacketPlayOutNamedEntitySpawn = NMSClass.get("PacketPlayOutNamedEntitySpawn")).getDeclaredField("a")).setAccessible(true);
				(PacketPlayOutEntityDestroy_ENTITIES = (PacketPlayOutEntityDestroy = NMSClass.get("PacketPlayOutEntityDestroy")).getDeclaredField("a")).setAccessible(true);
				(PacketPlayOutEntityTeleport_ENTITYID = (NMSClass.get("PacketPlayOutEntityTeleport")).getDeclaredField("a")).setAccessible(true);
				(PacketPlayOutEntity_ENTITYID = (NMSClass.get("PacketPlayOutEntity")).getDeclaredField("a")).setAccessible(true);
				if (NMSClass.version.equals("v1_8_R1")) {
					PacketPlayOutRelEntityMove = NMSClass.get("PacketPlayOutRelEntityMove");
					PacketPlayOutRelEntityMoveLook = NMSClass.get("PacketPlayOutRelEntityMoveLook");
				} else {
					PacketPlayOutRelEntityMove = NMSClass.get("PacketPlayOutEntity$PacketPlayOutRelEntityMove");
					PacketPlayOutRelEntityMoveLook = NMSClass.get("PacketPlayOutEntity$PacketPlayOutRelEntityMoveLook");
				}
				(PacketPlayOutEntityTeleport_ENTITYID = (PacketPlayOutEntityTeleport = NMSClass.get("PacketPlayOutEntityTeleport")).getDeclaredField("a")).setAccessible(true);
			}
			if (NMSClass.versionNumber >= 9) {
				PacketPlayOutMount = NMSClass.get("PacketPlayOutMount");
				(PacketPlayOutMount_VEHICLE = PacketPlayOutMount.getDeclaredField("a")).setAccessible(true);
				(PacketPlayOutMount_PASSENGERS = PacketPlayOutMount.getDeclaredField("b")).setAccessible(true);
			}
		} catch (Exception e) {
			Shared.error("Failed to initialize NameTagXPacket class", e);
		}
	}
}