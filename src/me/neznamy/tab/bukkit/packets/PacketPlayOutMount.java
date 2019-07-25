package me.neznamy.tab.bukkit.packets;

import java.lang.reflect.Field;

import me.neznamy.tab.shared.Shared;

public class PacketPlayOutMount extends PacketPlayOut{
	
	private int vehicle;
    private int[] passengers;
    
    public PacketPlayOutMount(int vehicle, int[] passengers) {
    	this.vehicle = vehicle;
    	this.passengers = passengers;
    }
    public int getVehicle() {
    	return vehicle;
    }
    public int[] getPassengers() {
    	return passengers;
    }
	public Object toNMS() {
		throw new IllegalStateException();
	}
	public static PacketPlayOutMount read(Object nmsPacket) throws Exception {
		if (PacketPlayOutMount == null || !PacketPlayOutMount.isInstance(nmsPacket)) return null;
		return new PacketPlayOutMount(PacketPlayOutMount_VEHICLE.getInt(nmsPacket), (int[]) PacketPlayOutMount_PASSENGERS.get(nmsPacket));
	}
	
	private static Class<?> PacketPlayOutMount;
	private static Field PacketPlayOutMount_VEHICLE;
	private static Field PacketPlayOutMount_PASSENGERS;
	
	static {
		try {
			if (versionNumber >= 9) {
				PacketPlayOutMount = getNMSClass("PacketPlayOutMount");
				(PacketPlayOutMount_VEHICLE = PacketPlayOutMount.getDeclaredField("a")).setAccessible(true);
				(PacketPlayOutMount_PASSENGERS = PacketPlayOutMount.getDeclaredField("b")).setAccessible(true);
			}
		} catch (Exception e) {
			Shared.error("Failed to initialize PacketPlayOutMount class", e);
		}
	}
}