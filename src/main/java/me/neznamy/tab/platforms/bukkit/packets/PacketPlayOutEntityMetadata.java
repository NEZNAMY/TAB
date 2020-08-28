package me.neznamy.tab.platforms.bukkit.packets;

import java.lang.reflect.Constructor;

import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing the n.m.s.PacketPlayOutEntityMetadata class to make work with it much easier
 */
public class PacketPlayOutEntityMetadata extends PacketPlayOut {

	public static Class<?> PacketPlayOutEntityMetadata;
	private static Constructor<?> newPacketPlayOutEntityMetadata;
	
	private int entityId;
	private DataWatcher dataWatcher;
	
	public static void initializeClass() throws Exception {
		try {
			//1.7+
			PacketPlayOutEntityMetadata = getNMSClass("PacketPlayOutEntityMetadata");
		} catch (ClassNotFoundException e) {
			//1.6-
			PacketPlayOutEntityMetadata = getNMSClass("Packet40EntityMetadata");
		}
		newPacketPlayOutEntityMetadata = PacketPlayOutEntityMetadata.getConstructor(int.class, getNMSClass("DataWatcher"), boolean.class);
	}

	public PacketPlayOutEntityMetadata(int entityId, DataWatcher dataWatcher) {
		this.entityId = entityId;
		this.dataWatcher = dataWatcher;
	}
	
	@Override
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		return newPacketPlayOutEntityMetadata.newInstance(entityId, dataWatcher.toNMS(), true);
	}
}