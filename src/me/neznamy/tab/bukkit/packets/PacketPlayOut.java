package me.neznamy.tab.bukkit.packets;

public abstract class PacketPlayOut extends NMSClass{
	public abstract Object toNMS() throws Exception;
}