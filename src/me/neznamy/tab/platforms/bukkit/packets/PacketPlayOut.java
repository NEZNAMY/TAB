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
		for (Field f : clazz.getDeclaredFields()) {
			f.setAccessible(true);
			fields.put(f.getName(), f);
		}
		return fields;
	}
	public static Map<String, Field> getStaticFields(Class<?> clazz){
		Map<String, Field> fields = new HashMap<String, Field>();
		if (clazz == null) return fields;
		for (Field f : clazz.getDeclaredFields()) {
		    if (Modifier.isStatic(f.getModifiers())) {
		    	f.setAccessible(true);
				fields.put(f.getName(), f);
		    }
		}
		return fields;
	}
}