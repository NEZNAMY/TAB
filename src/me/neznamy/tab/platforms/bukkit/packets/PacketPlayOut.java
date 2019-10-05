package me.neznamy.tab.platforms.bukkit.packets;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import me.neznamy.tab.shared.ProtocolVersion;

public abstract class PacketPlayOut{

	public abstract Object toNMS(ProtocolVersion clientVersion) throws Exception;

	public static Map<String, Field> getFields(Class<?> clazz){
		Map<String, Field> fields = new HashMap<String, Field>();
		if (clazz == null) return fields;
		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			fields.put(field.getName(), field);
		}
		return fields;
	}
	public static Map<String, Object> getStaticFields(Class<?> clazz){
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