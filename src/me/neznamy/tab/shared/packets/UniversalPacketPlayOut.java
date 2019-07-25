package me.neznamy.tab.shared.packets;

import java.lang.reflect.Constructor;

import org.bukkit.Bukkit;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

public abstract class UniversalPacketPlayOut{

	public static String version;
	public static int versionNumber;

	static {
		try {
			Class.forName("org.bukkit.Bukkit");
			version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
			versionNumber = Integer.parseInt(version.split("_")[1]);
		} catch (Exception e) {
			//bungee version
		}
	}
	
	public abstract Object toNMS() throws Exception;
	public abstract Object toBungee(int clientVersion);
	
	public static Class<?> getNMSClass(String name) throws Exception{
		return Class.forName("net.minecraft.server." + version + "." + name);
	}
	public static Constructor<?> getConstructor(Class<?> clazz, int parameterCount){
		for (Constructor<?> c : clazz.getDeclaredConstructors()) {
			if (c.getParameterCount() == parameterCount) return c;
		}
		return null;
	}
	public void send(ITabPlayer to) {
		try {
			Object packet = Shared.mainClass.toNMS(this, to.getVersion());
			if (packet != null) to.sendPacket(packet);
		} catch (Exception e) {
			Shared.error("An error occured when creating " + getClass().getSimpleName(), e);
		}
	}
}