package me.neznamy.tab.bukkit.packets;

import me.neznamy.tab.bukkit.packets.method.MethodAPI;

public class PacketPlayOutEntityDestroy extends PacketPlayOut{
	
    private int[] entities;
    
    public PacketPlayOutEntityDestroy(int... entities) {
        this.entities = entities;
    }
	public Object toNMS(){
		return MethodAPI.getInstance().newPacketPlayOutEntityDestroy(entities);
	}
}