package me.neznamy.tab.platforms.bukkit.unlimitedtags;

import java.lang.reflect.Field;
import java.util.Map;

import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOut;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;

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
		if (MethodAPI.PacketPlayOutNamedEntitySpawn.isInstance(nmsPacket)) return new NameTagXPacket(PacketType.NAMED_ENTITY_SPAWN, PacketPlayOutNamedEntitySpawn_ENTITYID.getInt(nmsPacket), null, -1);
		if (MethodAPI.PacketPlayOutEntityDestroy.isInstance(nmsPacket)) return new NameTagXPacket(PacketType.ENTITY_DESTROY, -1, (int[]) PacketPlayOutEntityDestroy_ENTITIES.get(nmsPacket), -1);
		if (MethodAPI.PacketPlayOutEntityTeleport.isInstance(nmsPacket)) return new NameTagXPacket(PacketType.ENTITY_TELEPORT, PacketPlayOutEntityTeleport_ENTITYID.getInt(nmsPacket), null, -1);
		if (MethodAPI.PacketPlayOutRelEntityMove.isInstance(nmsPacket)) return new NameTagXPacket(PacketType.ENTITY_MOVE, PacketPlayOutEntity_ENTITYID.getInt(nmsPacket), null, -1);
		if (MethodAPI.PacketPlayOutRelEntityMoveLook.isInstance(nmsPacket)) return new NameTagXPacket(PacketType.ENTITY_MOVE, PacketPlayOutEntity_ENTITYID.getInt(nmsPacket), null, -1);
		if (MethodAPI.PacketPlayOutMount != null && MethodAPI.PacketPlayOutMount.isInstance(nmsPacket)) return new NameTagXPacket(PacketType.MOUNT, PacketPlayOutMount_VEHICLE.getInt(nmsPacket), (int[]) PacketPlayOutMount_PASSENGERS.get(nmsPacket), -1);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() == 8 && MethodAPI.PacketPlayOutAttachEntity.isInstance(nmsPacket)) return new NameTagXPacket(PacketType.ATTACH_ENTITY, PacketPlayOutAttachEntity_VEHICLE.getInt(nmsPacket), new int[] {PacketPlayOutAttachEntity_PASSENGER.getInt(nmsPacket)}, PacketPlayOutAttachEntity_A.getInt(nmsPacket));
		return null;
	}

	public static enum PacketType{
		NAMED_ENTITY_SPAWN, //spawning armor stand
		ENTITY_DESTROY, //destroying armor stand
		ENTITY_TELEPORT, //teleporting armor stand
		ENTITY_MOVE, //teleporting armor stand
		MOUNT, //1.9+ mount detection
		ATTACH_ENTITY; //1.8.x mount detection
	}
	
	private static Field PacketPlayOutNamedEntitySpawn_ENTITYID = PacketPlayOut.getFields(MethodAPI.PacketPlayOutNamedEntitySpawn).get("a");
	
	private static Field PacketPlayOutEntityDestroy_ENTITIES = PacketPlayOut.getFields(MethodAPI.PacketPlayOutEntityDestroy).get("a");
	
	private static Field PacketPlayOutEntityTeleport_ENTITYID = PacketPlayOut.getFields(MethodAPI.PacketPlayOutEntityTeleport).get("a");
	
	private static Field PacketPlayOutEntity_ENTITYID = PacketPlayOut.getFields(MethodAPI.PacketPlayOutEntity).get("a");
	
	private static Map<String, Field> mount = PacketPlayOut.getFields(MethodAPI.PacketPlayOutMount);
	private static Field PacketPlayOutMount_VEHICLE = mount.get("a");
	private static Field PacketPlayOutMount_PASSENGERS = mount.get("b");
	
	private static Map<String, Field> attachentity = PacketPlayOut.getFields(MethodAPI.PacketPlayOutAttachEntity);
	private static Field PacketPlayOutAttachEntity_A = attachentity.get("a");
	private static Field PacketPlayOutAttachEntity_PASSENGER = attachentity.get("b");
	private static Field PacketPlayOutAttachEntity_VEHICLE = attachentity.get("c");
}