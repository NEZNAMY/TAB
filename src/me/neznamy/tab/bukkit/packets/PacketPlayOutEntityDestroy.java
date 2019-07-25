package me.neznamy.tab.bukkit.packets;

import java.lang.reflect.Field;

import me.neznamy.tab.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.Shared;

public class PacketPlayOutEntityDestroy extends PacketPlayOut{
	
    private int[] entities;
    
    public PacketPlayOutEntityDestroy(int... entities) {
        this.entities = entities;
    }
    public int[] getEntities() {
    	return entities;
    }
	public Object toNMS(){
		return MethodAPI.getInstance().newPacketPlayOutEntityDestroy(entities);
	}
	public static PacketPlayOutEntityDestroy read(Object nmsPacket) throws Exception{
		if (!PacketPlayOutEntityDestroy.isInstance(nmsPacket)) return null;
		return new PacketPlayOutEntityDestroy((int[]) PacketPlayOutEntityDestroy_ENTITIES.get(nmsPacket));
	}
	
	private static Class<?> PacketPlayOutEntityDestroy;
	private static Field PacketPlayOutEntityDestroy_ENTITIES;
	
	static {
		try {
			(PacketPlayOutEntityDestroy_ENTITIES = (PacketPlayOutEntityDestroy = getNMSClass("PacketPlayOutEntityDestroy")).getDeclaredField("a")).setAccessible(true);
		} catch (Exception e) {
			Shared.error("Failed to initialize PacketPlayOutEntityDestroy class", e);
		}
	}
}