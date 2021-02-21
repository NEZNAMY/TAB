package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing the n.m.s.DataWatcherRegistry class to make work with it much easier
 */
public class DataWatcherRegistry {

	//used registry types
	public Object Byte;
	public Object Integer;
	public Object Float;
	public Object String;
	public Object Optional_IChatBaseComponent;
	public Object Boolean;

	/**
	 * Initializes required NMS classes and fields
	 * @throws Exception - if something fails
	 */
	public DataWatcherRegistry(Class<?> registry) throws Exception {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			Map<String, Object> fields = getStaticFields(registry);
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
	private Map<String, Object> getStaticFields(Class<?> clazz){
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