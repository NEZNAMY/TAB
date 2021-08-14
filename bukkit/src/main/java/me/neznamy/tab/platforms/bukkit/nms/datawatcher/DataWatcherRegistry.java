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
	private Object registryOptional_IChatBaseComponent;
	@Deprecated
	private Object registryOptional_ItemStack;
	private Object registryItemStack;
	private Object registryOptional_IBlockData;
	private Object registryBoolean;
	private Object registryParticleParam;
	private Object registryVector3f;
	private Object registryBlockPosition;
	private Object registryOptional_BlockPosition;
	private Object registryEnumDirection;
	private Object registryOptional_UUID;
	private Object registryNBTTagCompound;
	private Object registryVillagerData;
	private Object registryOptionalInt;
	private Object registryEntityPose;

	/**
	 * Initializes required NMS classes and fields
	 */
	public DataWatcherRegistry(NMSStorage nms) {
		if (nms.getMinorVersion() >= 9) {
			Map<String, Object> fields = getStaticFields(nms.getClass("DataWatcherRegistry"), nms);
			registryByte = fields.get("a");
			registryInteger = fields.get("b");
			registryFloat = fields.get("c");
			registryString = fields.get("d");
			registryIChatBaseComponent = fields.get("e");
			if (nms.getMinorVersion() >= 13) {
				registryOptional_IChatBaseComponent = fields.get("f");
				registryItemStack = fields.get("g");
				registryOptional_IBlockData = fields.get("h");
				registryBoolean = fields.get("i");
				registryParticleParam = fields.get("j");
				registryVector3f = fields.get("k");
				registryBlockPosition = fields.get("l");
				registryOptional_BlockPosition = fields.get("m");
				registryEnumDirection = fields.get("n");
				registryOptional_UUID = fields.get("o");
				registryNBTTagCompound = fields.get("p");
				if (nms.getMinorVersion() >= 15) {
					registryVillagerData = fields.get("q");
					registryOptionalInt = fields.get("r");
					registryEntityPose = fields.get("s");
				}
			} else {
				registryOptional_IBlockData = fields.get("g");
				registryBoolean = fields.get("h");
				registryVector3f = fields.get("i");
				registryBlockPosition = fields.get("j");
				registryOptional_BlockPosition = fields.get("k");
				registryEnumDirection = fields.get("l");
				registryOptional_UUID = fields.get("m");
				if (nms.getMinorVersion() >= 12) {
					registryNBTTagCompound = fields.get("n");
				}
				if (nms.getMinorVersion() >= 11) {
					registryItemStack = fields.get("f");
				} else {
					registryOptional_ItemStack = fields.get("f");
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
				} catch (Exception e) {
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
		return registryOptional_IChatBaseComponent;
	}
	
	public Object getOptionalItemStack() {
		return registryOptional_ItemStack;
	}
	
	public Object getItemStack() {
		return registryItemStack;
	}
	
	public Object getOptionalIBlockData() {
		return registryOptional_IBlockData;
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
		return registryOptional_BlockPosition;
	}
	
	public Object getEnumDirection() {
		return registryEnumDirection;
	}
	
	public Object getOptionalUUID() {
		return registryOptional_UUID;
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