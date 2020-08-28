package me.neznamy.tab.platforms.bukkit.packets;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing the n.m.s.DataWatcherRegistry class to make work with it much easier
 */
public class DataWatcherRegistry {

	public static Object Byte;
	public static Object Integer;
	public static Object Float;
	public static Object String;
	public static Object Optional_IChatBaseComponent;
	public static Object Boolean;

	public static void initializeClass() throws Exception {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			Class<?> DataWatcherRegistry = PacketPlayOut.getNMSClass("DataWatcherRegistry");
			Map<String, Object> fields = getStaticFields(DataWatcherRegistry);
			Byte = fields.get("a");
			Integer = fields.get("b");
			Float = fields.get("c");
			String = fields.get("d");
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
				Optional_IChatBaseComponent = fields.get("f");
				Boolean = fields.get("i");
			} else {
				Boolean = fields.get("h");
			}
		} else {
			Byte = 0;
			Integer = 2;
			Float = 3;
			String = 4;
		}
	}

	private static Map<String, Object> getStaticFields(Class<?> clazz){
		Map<String, Object> fields = new HashMap<String, Object>();
		if (clazz == null) return fields;
		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			if (Modifier.isStatic(field.getModifiers())) {
				try {
					fields.put(field.getName(), field.get(null));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return fields;
	}
}