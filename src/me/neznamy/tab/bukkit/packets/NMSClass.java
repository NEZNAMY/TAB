package me.neznamy.tab.bukkit.packets;

import java.lang.reflect.Constructor;

import org.bukkit.Bukkit;

public class NMSClass {
	
	public static final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	public static final int versionNumber = Integer.parseInt(version.split("_")[1]);
	private static final boolean versionSupported = (versionNumber >= 8 && versionNumber <= 14);

	public static boolean isVersionSupported() {
		return versionSupported;
	}
	public static Class<?> getNMSClass(String name) throws Exception{
		return get(name);
	}
	public static Class<?> get(String name) throws Exception{
		return Class.forName("net.minecraft.server." + version + "." + name);
	}
	public static Constructor<?> getConstructor(Class<?> clazz, int parameterCount){
		for (Constructor<?> c : clazz.getDeclaredConstructors()) {
			if (c.getParameterCount() == parameterCount) return c;
		}
		return null;
	}
}