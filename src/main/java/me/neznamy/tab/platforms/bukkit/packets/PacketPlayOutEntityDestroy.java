package me.neznamy.tab.platforms.bukkit.packets;

import java.lang.reflect.Constructor;

import me.neznamy.tab.shared.ProtocolVersion;

public class PacketPlayOutEntityDestroy extends PacketPlayOut {

	public static Class<?> PacketPlayOutEntityDestroy = getNMSClass("PacketPlayOutEntityDestroy", "Packet29DestroyEntity");
	private static Constructor<?> newPacketPlayOutEntityDestroy = getConstructor(PacketPlayOutEntityDestroy, 1);
	
	private int[] ids;
	
	public PacketPlayOutEntityDestroy(int... ids) {
		this.ids = ids;
	}
	
	@Override
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		return newPacketPlayOutEntityDestroy.newInstance(ids);
	}
}