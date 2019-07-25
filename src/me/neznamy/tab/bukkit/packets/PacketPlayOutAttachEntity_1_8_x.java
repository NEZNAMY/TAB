package me.neznamy.tab.bukkit.packets;

import java.lang.reflect.Field;

import me.neznamy.tab.shared.Shared;

public class PacketPlayOutAttachEntity_1_8_x extends PacketPlayOut{
	
	private int a;
    private int passenger;
    private int vehicle;
    
    public PacketPlayOutAttachEntity_1_8_x(int a, int passenger, int vehicle) {
    	this.a = a;
    	this.passenger = passenger;
    	this.vehicle = vehicle;
    }
    public int getVehicle() {
    	return vehicle;
    }
    public int getPassenger() {
    	return passenger;
    }
    public int getA() {
    	return a;
    }
	public Object toNMS() {
		throw new IllegalStateException();
	}
	public static PacketPlayOutAttachEntity_1_8_x read(Object nmsPacket) throws Exception{
		if (PacketPlayOutAttachEntity == null || !PacketPlayOutAttachEntity.isInstance(nmsPacket)) return null;
		int a = PacketPlayOutAttachEntity_A.getInt(nmsPacket);
		int passenger = PacketPlayOutAttachEntity_PASSENGER.getInt(nmsPacket);
		int vehicle = PacketPlayOutAttachEntity_VEHICLE.getInt(nmsPacket);
		return new PacketPlayOutAttachEntity_1_8_x(a, passenger, vehicle);
	}
	
	private static Class<?> PacketPlayOutAttachEntity;
	private static Field PacketPlayOutAttachEntity_A;
	private static Field PacketPlayOutAttachEntity_PASSENGER;
	private static Field PacketPlayOutAttachEntity_VEHICLE;
	
	static {
		try {
			if (versionNumber == 8) {
				PacketPlayOutAttachEntity = getNMSClass("PacketPlayOutAttachEntity");
				(PacketPlayOutAttachEntity_A = PacketPlayOutAttachEntity.getDeclaredField("a")).setAccessible(true);
				(PacketPlayOutAttachEntity_PASSENGER = PacketPlayOutAttachEntity.getDeclaredField("b")).setAccessible(true);
				(PacketPlayOutAttachEntity_VEHICLE = PacketPlayOutAttachEntity.getDeclaredField("c")).setAccessible(true);
			}
		} catch (Exception e) {
			Shared.error("Failed to initialize PacketPlayOutAttachEntity class", e);
		}
	}
}