package me.neznamy.tab.bukkit.packets;

import java.lang.reflect.Constructor;

import me.neznamy.tab.shared.ProtocolVersion;

public class NMSClass {
	
	public static Class<?> getNMSClass(String name) throws Exception{
		return get(name);
	}
	public static Class<?> get(String name) throws Exception{
		return Class.forName("net.minecraft.server." + ProtocolVersion.packageName + "." + name);
	}
	public static Constructor<?> getConstructor(Class<?> clazz, int parameterCount){
		for (Constructor<?> c : clazz.getDeclaredConstructors()) {
			if (c.getParameterCount() == parameterCount) return c;
		}
		return null;
	}
}