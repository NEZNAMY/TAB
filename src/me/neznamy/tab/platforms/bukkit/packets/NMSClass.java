package me.neznamy.tab.platforms.bukkit.packets;

import me.neznamy.tab.shared.ProtocolVersion;

public class NMSClass {

	public static Class<?> getClass(String name) throws Exception{
		return Class.forName("net.minecraft.server." + ProtocolVersion.packageName + "." + name);
	}
}