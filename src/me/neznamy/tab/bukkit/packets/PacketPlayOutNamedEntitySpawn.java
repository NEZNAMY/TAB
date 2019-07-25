package me.neznamy.tab.bukkit.packets;

import java.lang.reflect.Field;

import me.neznamy.tab.shared.Shared;

public class PacketPlayOutNamedEntitySpawn extends PacketPlayOut{

	private int entityId;

	public PacketPlayOutNamedEntitySpawn(int entityId) {
		this.entityId = entityId;
	}
	public int getEntityId() {
		return entityId;
	}
	public Object toNMS() {
		throw new IllegalStateException();
	}
	public static PacketPlayOutNamedEntitySpawn read(Object nmsPacket) throws Exception {
		if (!PacketPlayOutNamedEntitySpawn.isInstance(nmsPacket)) return null;
		return new PacketPlayOutNamedEntitySpawn(PacketPlayOutNamedEntitySpawn_ENTITYID.getInt(nmsPacket));
	}

	private static Class<?> PacketPlayOutNamedEntitySpawn;
	private static Field PacketPlayOutNamedEntitySpawn_ENTITYID;
	
	static {
		try {
			if (versionNumber >= 8) {
				PacketPlayOutNamedEntitySpawn = getNMSClass("PacketPlayOutNamedEntitySpawn");
				(PacketPlayOutNamedEntitySpawn_ENTITYID = PacketPlayOutNamedEntitySpawn.getDeclaredField("a")).setAccessible(true);
			}
		} catch (Exception e) {
			Shared.error("Failed to initialize PacketPlayOutMount class", e);
		}
	}
}