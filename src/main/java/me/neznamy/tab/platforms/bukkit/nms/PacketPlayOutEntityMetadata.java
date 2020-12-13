package me.neznamy.tab.platforms.bukkit.nms;

import java.lang.reflect.Constructor;

import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing the n.m.s.PacketPlayOutEntityMetadata class to make work with it much easier
 */
public class PacketPlayOutEntityMetadata extends PacketPlayOut {

	//used NMS constructor
	private static Constructor<?> newPacketPlayOutEntityMetadata;
	
	//entity id
	private int entityId;
	
	//entity metadata to change
	private DataWatcher dataWatcher;
	
	/**
	 * Initializes required NMS classes and fields
	 * @throws Exception - if something fails
	 */
	public static void initializeClass() throws Exception {
		Class<?> PacketPlayOutEntityMetadata;
		try {
			//1.7+
			PacketPlayOutEntityMetadata = getNMSClass("PacketPlayOutEntityMetadata");
		} catch (ClassNotFoundException e) {
			//1.6-
			PacketPlayOutEntityMetadata = getNMSClass("Packet40EntityMetadata");
		}
		newPacketPlayOutEntityMetadata = PacketPlayOutEntityMetadata.getConstructor(int.class, getNMSClass("DataWatcher"), boolean.class);
	}

	/**
	 * Constructs new instance with given parameters
	 * @param entityId entity id
	 * @param dataWatcher entity metadata
	 */
	public PacketPlayOutEntityMetadata(int entityId, DataWatcher dataWatcher) {
		this.entityId = entityId;
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
		return newPacketPlayOutEntityMetadata.newInstance(entityId, dataWatcher.toNMS(), true);
	}
}