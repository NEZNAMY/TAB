package me.neznamy.tab.platforms.bukkit.packets;

import java.lang.reflect.*;
import java.util.*;

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
	public static List<Field> getFields(Class<?> clazz, Class<?> type){
		List<Field> list = new ArrayList<Field>();
		if (clazz == null) return list;
		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			if (field.getType() == type) list.add(field);
		}
		return list;
	}
	public static Field getObjectAt(List<Field> list, int index) {
		try {
			return list.get(index);
		} catch (Exception e) {
			return null;
		}
	}
}