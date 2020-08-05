package me.neznamy.tab.platforms.bukkit.packets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;

public class DataWatcher{

	public static final Class<?> DataWatcher = PacketPlayOut.getNMSClass("DataWatcher");
	private static final Constructor<?> newDataWatcher = PacketPlayOut.getConstructor(DataWatcher, 1, 0);
	
	private static final Class<?> DataWatcherObject_ = PacketPlayOut.getNMSClass("DataWatcherObject");
	private static final Constructor<?> newDataWatcherObject = PacketPlayOut.getConstructor(DataWatcherObject_, 2);
	
	private static final Class<?> WatchableObject = PacketPlayOut.getNMSClass("DataWatcher$Item", "WatchableObject");
	private static final Constructor<?> newWatchableObject = PacketPlayOut.getConstructor(WatchableObject, 3, 2);
	
	private static final Field ENTITY = PacketPlayOut.getObjectAt(PacketPlayOut.getFields(DataWatcher, PacketPlayOut.getNMSClass("Entity")), 0);
	private static final Method REGISTER = getRegisterMethod();

	private Object entity;
	private Map<Integer, Item> dataValues = new HashMap<Integer, Item>();
	private DataWatcherHelper helper;

	private static Method getRegisterMethod() {
		try {
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
				return DataWatcher.getMethod("register", DataWatcherObject_, Object.class);
			} else {
				return DataWatcher.getMethod("a", int.class, Object.class);
			}
		} catch (Exception e) {
			Shared.errorManager.criticalError("Failed to inialize DataWatcher class", e);
			return null;
		}
	}
	public DataWatcher(Object entity) {
		this.entity = entity;
		helper = new DataWatcherHelper(this);
	}

	public void setValue(DataWatcherObject type, Object value){
		dataValues.put(type.position, new Item(type, value));
	}

	public void removeValue(int position) {
		dataValues.remove(position);
	}

	public Item getItem(int position) {
		return dataValues.get(position);
	}

	public DataWatcherHelper helper() {
		return helper;
	}

	public static class Item{

		public DataWatcherObject type;
		public Object value;

		public Item(DataWatcherObject type, Object value){
			this.type = type;
			this.value = value;
		}
		public Object toNMS() throws Exception{
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
				return newWatchableObject.newInstance(type.toNMS(), value);
			} else {
				return newWatchableObject.newInstance(type.classType, type.position, value);
			}
		}
		public static Item fromNMS(Object nmsItem) throws Exception{
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
				DataWatcherObject object = DataWatcherObject.fromNMS(nmsItem.getClass().getDeclaredField("a").get(nmsItem));
				Object value = nmsItem.getClass().getDeclaredField("b").get(nmsItem);
				return new Item(object, value);
			} else {
				Object classType = nmsItem.getClass().getDeclaredField("a").getInt(nmsItem);
				int position = nmsItem.getClass().getDeclaredField("b").getInt(nmsItem);
				Object value = nmsItem.getClass().getDeclaredField("c").getInt(nmsItem);
				return new Item(new DataWatcherObject(position, classType), value);
			}
		}
	}
	public Object toNMS() throws Exception{
		Object nmsWatcher;
		if (newDataWatcher.getParameterCount() == 1) {
			nmsWatcher = newDataWatcher.newInstance(entity);
		} else {
			nmsWatcher = newDataWatcher.newInstance();
		}
		for (Item item : dataValues.values()) {
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
				REGISTER.invoke(nmsWatcher, item.type.toNMS(), item.value);
			} else {
				REGISTER.invoke(nmsWatcher, item.type.position, item.value);
			}
		}
		return nmsWatcher;
	}
	@SuppressWarnings("unchecked")
	public static DataWatcher fromNMS(Object nmsWatcher) throws Exception{
		DataWatcher watcher = new DataWatcher(ENTITY.get(nmsWatcher));
		List<Object> items = (List<Object>) nmsWatcher.getClass().getMethod("c").invoke(nmsWatcher);
		if (items != null) {
			for (Object watchableObject : items) {
				Item w = Item.fromNMS(watchableObject);
				watcher.setValue(w.type, w.value);
			}
		}
		return watcher;
	}
	public static class DataWatcherObject {

		public int position;
		public Object classType;

		public DataWatcherObject(int position, Object classType){
			this.position = position;
			this.classType = classType;
		}
		
		public static DataWatcherObject fromNMS(Object nmsObject) throws Exception {
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
				int position = nmsObject.getClass().getDeclaredField("a").getInt(nmsObject);
				Object classType = nmsObject.getClass().getDeclaredField("b").get(nmsObject);
				return new DataWatcherObject(position, classType);
			} else {
				throw new IllegalStateException();
			}
		}
		public Object toNMS() throws Exception {
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
				return newDataWatcherObject.newInstance(position, classType);
			} else {
				throw new IllegalStateException();
			}
		}
	}
}