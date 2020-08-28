package me.neznamy.tab.platforms.bukkit.packets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing the n.m.s.DataWatcher class to make work with it much easier
 */
public class DataWatcher {

	public static Class<?> DataWatcher;
	private static Constructor<?> newDataWatcher;

	private static Class<?> DataWatcherObject;
	public static Constructor<?> newDataWatcherObject;

	private static Class<?> DataWatcherItem;
	private static Constructor<?> newDataWatcherItem;

	private static Method REGISTER;

	private Map<Integer, Item> dataValues = new HashMap<Integer, Item>();
	private DataWatcherHelper helper = new DataWatcherHelper(this);

	public static void initializeClass() throws Exception {
		DataWatcher = PacketPlayOut.getNMSClass("DataWatcher");
		try {
			//1.7+
			newDataWatcher = DataWatcher.getConstructor(PacketPlayOut.getNMSClass("Entity"));
		} catch (Exception e1) {
			//1.6-
			newDataWatcher = DataWatcher.getConstructor();
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			//1.9+
			DataWatcherItem = PacketPlayOut.getNMSClass("DataWatcher$Item");
			DataWatcherObject = PacketPlayOut.getNMSClass("DataWatcherObject");
			newDataWatcherObject = DataWatcherObject.getConstructors()[0];
			REGISTER = DataWatcher.getMethod("register", DataWatcherObject, Object.class);
		} else {
			//1.8-
			try {
				//v1_8_R2+
				DataWatcherItem = PacketPlayOut.getNMSClass("DataWatcher$WatchableObject");
			} catch (ClassNotFoundException e) {
				//v1_8_R1-
				DataWatcherItem = PacketPlayOut.getNMSClass("WatchableObject");
			}
			REGISTER = DataWatcher.getMethod("a", int.class, Object.class);
		}
		newDataWatcherItem = DataWatcherItem.getConstructors()[0];
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
				return newDataWatcherItem.newInstance(type.toNMS(), value);
			} else {
				return newDataWatcherItem.newInstance(type.classType, type.position, value);
			}
		}
		public static Item fromNMS(Object nmsItem) throws Exception{
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
				DataWatcherObject object = me.neznamy.tab.platforms.bukkit.packets.DataWatcherObject.fromNMS(getValue(nmsItem, "a"));
				Object value = getValue(nmsItem, "b");
				return new Item(object, value);
			} else {
				Object classType = getValue(nmsItem, "a");
				int position = (int) getValue(nmsItem, "b");
				Object value = getValue(nmsItem, "c");
				return new Item(new DataWatcherObject(position, classType), value);
			}
		}
	}
	public Object toNMS() throws Exception{
		Object nmsWatcher;
		if (newDataWatcher.getParameterCount() == 1) {
			nmsWatcher = newDataWatcher.newInstance(new Object[] {null});
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
		DataWatcher watcher = new DataWatcher();
		List<Object> items = (List<Object>) nmsWatcher.getClass().getMethod("c").invoke(nmsWatcher);
		if (items != null) {
			for (Object watchableObject : items) {
				Item w = Item.fromNMS(watchableObject);
				watcher.setValue(w.type, w.value);
			}
		}
		return watcher;
	}

	static Object getValue(Object obj, String field) throws Exception {
		Field f = obj.getClass().getDeclaredField(field);
		f.setAccessible(true);
		return f.get(obj);
	}


}