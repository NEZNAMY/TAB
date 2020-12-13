package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOut;
import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing the n.m.s.DataWatcherRegistry class to make work with it much easier
 */
public class DataWatcherRegistry {

	//used registry types
	public static Object Byte;
	public static Object Integer;
	public static Object Float;
	public static Object String;
	public static Object Optional_IChatBaseComponent;
	public static Object Boolean;

	/**
	 * Initializes required NMS classes and fields
	 * @throws Exception - if something fails
	 */
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

	/**
	 * Gets values of all static fields in a class
	 * @param clazz class to return field values from
	 * @return map of values
	 */
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