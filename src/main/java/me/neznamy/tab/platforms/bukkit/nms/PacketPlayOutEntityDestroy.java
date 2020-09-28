package me.neznamy.tab.platforms.bukkit.nms;

import java.lang.reflect.Constructor;

import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing the n.m.s.PacketPlayOutEntityDestroy class to make work with it much easier
 */
public class PacketPlayOutEntityDestroy extends PacketPlayOut {

	//used NMS constructor
	private static Constructor<?> newPacketPlayOutEntityDestroy;
	
	//array of removed entities
	private int[] ids;
	
	/**
	 * Initializes required NMS classes and fields
	 * @throws Exception - if something fails
	 */
	public static void initializeClass() throws Exception {
		Class<?> PacketPlayOutEntityDestroy;
		try {
			//1.7+
			PacketPlayOutEntityDestroy = getNMSClass("PacketPlayOutEntityDestroy");
		} catch (ClassNotFoundException e) {
			//1.6-
			PacketPlayOutEntityDestroy = getNMSClass("Packet29DestroyEntity");
		}
		newPacketPlayOutEntityDestroy = PacketPlayOutEntityDestroy.getConstructor(int[].class);
	}
	
	/**
	 * Constructs new instance with given parameter
	 * @param ids array of entites to remove
	 */
	public PacketPlayOutEntityDestroy(int... ids) {
		this.ids = ids;
	}
	
	/**
	 * Converts the custom class into an actual minecraft packet
	 * @param clientVersion client version to build the packet for
	 * @return NMS packet
	 * @throws Exception if something fails
	 */
	@Override
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		return newPacketPlayOutEntityDestroy.newInstance(ids);
	}
}