package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;

/**
 * A class representing the n.m.s.DataWatcherRegistry class to make work with it much easier
 */
public class DataWatcherRegistry {

	//used registry types
	private Object registryByte;
	private Object registryInteger;
	private Object registryFloat;
	private Object registryString;
	private Object registryOptionalComponent;
	private Object registryBoolean;

	/**
	 * Initializes required NMS classes and fields
	 */
	public DataWatcherRegistry(NMSStorage nms, Class<?> registry) {
		if (nms.getMinorVersion() >= 9) {
			Map<String, Object> fields = getStaticFields(registry);
			registryByte = fields.get("a");
			registryInteger = fields.get("b");
			registryFloat = fields.get("c");
			registryString = fields.get("d");
			if (nms.getMinorVersion() >= 13) {
				registryOptionalComponent = fields.get("f");
				registryBoolean = fields.get("i");
			} else {
				registryBoolean = fields.get("h");
			}
		}
	}

	/**
	 * Gets values of all static fields in a class
	 * @param clazz class to return field values from
	 * @return map of values
	 */
	private Map<String, Object> getStaticFields(Class<?> clazz){
		Map<String, Object> fields = new HashMap<>();
		if (clazz == null) return fields;
		for (Field field : clazz.getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers())) {
				try {
					fields.put(field.getName(), field.get(null));
				} catch (Exception e) {
					//this will never happen
				}
			}
		}
		return fields;
	}

	public Object getBoolean() {
		return registryBoolean;
	}

	public Object getByte() {
		return registryByte;
	}

	public Object getFloat() {
		return registryFloat;
	}

	public Object getInteger() {
		return registryInteger;
	}

	public Object getString() {
		return registryString;
	}

	public Object getOptionalComponent() {
		return registryOptionalComponent;
	}
}