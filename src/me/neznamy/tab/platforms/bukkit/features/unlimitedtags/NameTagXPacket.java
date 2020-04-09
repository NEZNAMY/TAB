package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.lang.reflect.Field;

import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOut;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;

public class NameTagXPacket {

	private PacketType type;
	public Object entity;

	public NameTagXPacket(PacketType type, Object entity) {
		this.type = type;
		this.entity = entity;
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
		return null;
	}

	public static enum PacketType{
		NAMED_ENTITY_SPAWN,
		ENTITY_DESTROY,
	}
	
	private static final Field PacketPlayOutNamedEntitySpawn_ENTITYID = PacketPlayOut.getFields(MethodAPI.PacketPlayOutNamedEntitySpawn).get("a");
	private static final Field PacketPlayOutEntityDestroy_ENTITIES = PacketPlayOut.getFields(MethodAPI.PacketPlayOutEntityDestroy).get("a");
}