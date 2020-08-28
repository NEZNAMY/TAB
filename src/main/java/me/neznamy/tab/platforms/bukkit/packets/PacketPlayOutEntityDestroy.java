package me.neznamy.tab.platforms.bukkit.packets;

import java.lang.reflect.Constructor;

import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing the n.m.s.PacketPlayOutEntityDestroy class to make work with it much easier
 */
public class PacketPlayOutEntityDestroy extends PacketPlayOut {

	public static Class<?> PacketPlayOutEntityDestroy;
	private static Constructor<?> newPacketPlayOutEntityDestroy;
	
	private int[] ids;
	
	public static void initializeClass() throws Exception {
		try {
			//1.7+
			PacketPlayOutEntityDestroy = getNMSClass("PacketPlayOutEntityDestroy");
		} catch (ClassNotFoundException e) {
			//1.6-
			PacketPlayOutEntityDestroy = getNMSClass("Packet29DestroyEntity");
		}
		newPacketPlayOutEntityDestroy = PacketPlayOutEntityDestroy.getConstructor(int[].class);
	}
	
	public PacketPlayOutEntityDestroy(int... ids) {
		this.ids = ids;
	}
	
	@Override
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		return newPacketPlayOutEntityDestroy.newInstance(ids);
	}
}