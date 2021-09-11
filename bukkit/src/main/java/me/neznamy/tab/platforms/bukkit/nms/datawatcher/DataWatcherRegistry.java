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

	private Object registryByte;
	private Object registryShort;
	private Object registryInteger;
	private Object registryFloat;
	private Object registryString;
	private Object registryIChatBaseComponent;
	private Object registryOptionalIChatBaseComponent;
	private Object registryOptionalItemStack;
	private Object registryItemStack;
	private Object registryOptionalIBlockData;
	private Object registryBoolean;
	private Object registryParticleParam;
	private Object registryVector3f;
	private Object registryBlockPosition;
	private Object registryOptionalBlockPosition;
	private Object registryEnumDirection;
	private Object registryOptionalUUID;
	private Object registryNBTTagCompound;
	private Object registryVillagerData;
	private Object registryOptionalInt;
	private Object registryEntityPose;

	/**
	 * Initializes required NMS classes and fields
	 */
	public DataWatcherRegistry(NMSStorage nms) {
		if (nms.getMinorVersion() >= 9) {
			Map<String, Object> fields = getStaticFields(nms.DataWatcherRegistry, nms);
			registryByte = fields.get("a");
			registryInteger = fields.get("b");
			registryFloat = fields.get("c");
			registryString = fields.get("d");
			registryIChatBaseComponent = fields.get("e");
			if (nms.getMinorVersion() >= 13) {
				registryOptionalIChatBaseComponent = fields.get("f");
				registryItemStack = fields.get("g");
				registryOptionalIBlockData = fields.get("h");
				registryBoolean = fields.get("i");
				registryParticleParam = fields.get("j");
				registryVector3f = fields.get("k");
				registryBlockPosition = fields.get("l");
				registryOptionalBlockPosition = fields.get("m");
				registryEnumDirection = fields.get("n");
				registryOptionalUUID = fields.get("o");
				registryNBTTagCompound = fields.get("p");
				if (nms.getMinorVersion() >= 15) {
					registryVillagerData = fields.get("q");
					registryOptionalInt = fields.get("r");
					registryEntityPose = fields.get("s");
				}
			} else {
				registryOptionalIBlockData = fields.get("g");
				registryBoolean = fields.get("h");
				registryVector3f = fields.get("i");
				registryBlockPosition = fields.get("j");
				registryOptionalBlockPosition = fields.get("k");
				registryEnumDirection = fields.get("l");
				registryOptionalUUID = fields.get("m");
				if (nms.getMinorVersion() >= 12) {
					registryNBTTagCompound = fields.get("n");
				}
				if (nms.getMinorVersion() >= 11) {
					registryItemStack = fields.get("f");
				} else {
					registryOptionalItemStack = fields.get("f");
				}
			}
		}
	}

	/**
	 * Gets values of all static fields in a class
	 * @param clazz class to return field values from
	 * @return map of values
	 */
	private Map<String, Object> getStaticFields(Class<?> clazz, NMSStorage nms){
		Map<String, Object> fields = new HashMap<>();
		for (Field field : clazz.getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers())) {
				nms.setAccessible(field);
				try {
					fields.put(field.getName(), field.get(null));
				} catch (IllegalAccessException e) {
					//this will never happen
				}
			}
		}
		return fields;
	}

	public Object getByte() {
		return registryByte;
	}
	
	public Object getShort() {
		return registryShort;
	}
	
	public Object getInteger() {
		return registryInteger;
	}
	
	public Object getFloat() {
		return registryFloat;
	}
	
	public Object getString() {
		return registryString;
	}
	
	public Object getComponent() {
		return registryIChatBaseComponent;
	}
	
	public Object getOptionalComponent() {
		return registryOptionalIChatBaseComponent;
	}
	
	public Object getOptionalItemStack() {
		return registryOptionalItemStack;
	}
	
	public Object getItemStack() {
		return registryItemStack;
	}
	
	public Object getOptionalIBlockData() {
		return registryOptionalIBlockData;
	}
	
	public Object getBoolean() {
		return registryBoolean;
	}
	
	public Object getParticleParam() {
		return registryParticleParam;
	}
	
	public Object getVector3f() {
		return registryVector3f;
	}
	
	public Object getBlockPosition() {
		return registryBlockPosition;
	}
	
	public Object getOptionalBlockPosition() {
		return registryOptionalBlockPosition;
	}
	
	public Object getEnumDirection() {
		return registryEnumDirection;
	}
	
	public Object getOptionalUUID() {
		return registryOptionalUUID;
	}
	
	public Object getNBTTagCompound() {
		return registryNBTTagCompound;
	}
	
	public Object getVillagerData() {
		return registryVillagerData;
	}
	
	public Object getOptionalInt() {
		return registryOptionalInt;
	}
	
	public Object getEntityPose() {
		return registryEntityPose;
	}
}