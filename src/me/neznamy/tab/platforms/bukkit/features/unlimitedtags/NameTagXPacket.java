package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.lang.reflect.Field;
import java.util.Map;

import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOut;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;

public class NameTagXPacket {

	private PacketType type;
	public Object a;
	public Object b;
	public Object c;

	public NameTagXPacket(PacketType type, Object a) {
		this(type, a, null, null);
	}
	public NameTagXPacket(PacketType type, Object a, Object b, Object c) {
		this.type = type;
		this.a = a;
		this.b = b;
		this.c = c;
	}
	public PacketType getPacketType() {
		return type;
	}
	public static NameTagXPacket fromNMS(Object nmsPacket) throws Exception {
		if (MethodAPI.PacketPlayOutNamedEntitySpawn.isInstance(nmsPacket)) {
			return new NameTagXPacket(PacketType.NAMED_ENTITY_SPAWN, PacketPlayOutNamedEntitySpawn_ENTITYID.get(nmsPacket));
		}
		if (MethodAPI.PacketPlayOutEntityDestroy.isInstance(nmsPacket)) {
			return new NameTagXPacket(PacketType.ENTITY_DESTROY, PacketPlayOutEntityDestroy_ENTITIES.get(nmsPacket));
		}
		if (MethodAPI.PacketPlayOutEntityTeleport.isInstance(nmsPacket)) {
			return new NameTagXPacket(PacketType.ENTITY_TELEPORT, PacketPlayOutEntityTeleport_ENTITYID.get(nmsPacket));
		}
		if (MethodAPI.PacketPlayOutRelEntityMove.isInstance(nmsPacket) || MethodAPI.PacketPlayOutRelEntityMoveLook.isInstance(nmsPacket)) {
			return new NameTagXPacket(PacketType.ENTITY_MOVE, PacketPlayOutEntity_ENTITYID.get(nmsPacket));
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9 && MethodAPI.PacketPlayOutMount.isInstance(nmsPacket)) {
			return new NameTagXPacket(PacketType.MOUNT, PacketPlayOutMount_VEHICLE.get(nmsPacket), PacketPlayOutMount_PASSENGERS.get(nmsPacket), null);
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() == 8 && MethodAPI.PacketPlayOutAttachEntity.isInstance(nmsPacket)) {
			return new NameTagXPacket(PacketType.ATTACH_ENTITY, PacketPlayOutAttachEntity_A.get(nmsPacket), PacketPlayOutAttachEntity_PASSENGER.get(nmsPacket), PacketPlayOutAttachEntity_VEHICLE.get(nmsPacket));
		}
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
	
	private static final Field PacketPlayOutNamedEntitySpawn_ENTITYID = PacketPlayOut.getFields(MethodAPI.PacketPlayOutNamedEntitySpawn).get("a");
	private static final Field PacketPlayOutEntityDestroy_ENTITIES = PacketPlayOut.getFields(MethodAPI.PacketPlayOutEntityDestroy).get("a");
	private static final Field PacketPlayOutEntityTeleport_ENTITYID = PacketPlayOut.getFields(MethodAPI.PacketPlayOutEntityTeleport).get("a");
	private static final Field PacketPlayOutEntity_ENTITYID = PacketPlayOut.getFields(MethodAPI.PacketPlayOutEntity).get("a");
	
	private static Map<String, Field> mount = PacketPlayOut.getFields(MethodAPI.PacketPlayOutMount);
	private static final Field PacketPlayOutMount_VEHICLE = mount.get("a");
	private static final Field PacketPlayOutMount_PASSENGERS = mount.get("b");
	
	private static Map<String, Field> attachentity = PacketPlayOut.getFields(MethodAPI.PacketPlayOutAttachEntity);
	private static final Field PacketPlayOutAttachEntity_A = attachentity.get("a");
	private static final Field PacketPlayOutAttachEntity_PASSENGER = attachentity.get("b");
	private static final Field PacketPlayOutAttachEntity_VEHICLE = attachentity.get("c");
}