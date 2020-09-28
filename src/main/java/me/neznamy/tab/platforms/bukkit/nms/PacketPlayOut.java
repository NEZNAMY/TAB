package me.neznamy.tab.platforms.bukkit.nms;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

import me.neznamy.tab.shared.ProtocolVersion;

/**
 * An abstract class extended by packet classes
 */
public abstract class PacketPlayOut {
	
	/**
	 * Converts the custom class into an actual minecraft packet
	 * @param clientVersion client version to build the packet for
	 * @return NMS packet
	 * @throws Exception if something fails
	 */
	public abstract Object toNMS(ProtocolVersion clientVersion) throws Exception;

	/**
	 * Gets all fields of given class with given class type
	 * @param clazz class to check fields of
	 * @param type class type to check for
	 * @return List of all fields with requested class type
	 */
	public static List<Field> getFields(Class<?> clazz, Class<?> type){
		List<Field> list = new ArrayList<Field>();
		if (clazz == null) return list;
		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			if (field.getType() == type) list.add(field);
		}
		return list;
	}

	/**
	 * Returns an NMS class with given name
	 * @param name name of class
	 * @return NMS class
	 * @throws ClassNotFoundException if class was not found
	 */
	public static Class<?> getNMSClass(String name) throws ClassNotFoundException {
		return Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + "." + name);
	}
}