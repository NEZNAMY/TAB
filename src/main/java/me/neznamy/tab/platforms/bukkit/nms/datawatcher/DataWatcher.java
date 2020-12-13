package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOut;
import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing the n.m.s.DataWatcher class to make work with it much easier
 */
public class DataWatcher {

	//required NMS classes, constructors and methods
	public static Class<?> DataWatcher;
	private static Constructor<?> newDataWatcher;
	private static Class<?> DataWatcherObject;
	public static Constructor<?> newDataWatcherObject;
	private static Method REGISTER;

	//datawatcher data
	private Map<Integer, DataWatcherItem> dataValues = new HashMap<Integer, DataWatcherItem>();
	
	//a helper for easier data write
	private DataWatcherHelper helper = new DataWatcherHelper(this);

	/**
	 * Initializes required NMS classes and fields
	 * @throws Exception - if something fails
	 */
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
			DataWatcherObject = PacketPlayOut.getNMSClass("DataWatcherObject");
			newDataWatcherObject = DataWatcherObject.getConstructors()[0];
			REGISTER = DataWatcher.getMethod("register", DataWatcherObject, Object.class);
		} else {
			//1.8-
			REGISTER = DataWatcher.getMethod("a", int.class, Object.class);
		}
	}

	/**
	 * Sets value into data values
	 * @param type - type of value
	 * @param value - value
	 */
	public void setValue(DataWatcherObject type, Object value){
		dataValues.put(type.position, new DataWatcherItem(type, value));
	}

	/**
	 * Removes value by position
	 * @param position - position of value to remove
	 */
	public void removeValue(int position) {
		dataValues.remove(position);
	}

	/**
	 * Returns item with given position
	 * @param position - position of item
	 * @return item or null if not set
	 */
	public DataWatcherItem getItem(int position) {
		return dataValues.get(position);
	}

	/**
	 * Returns helper created by this instance
	 * @return data write helper
	 */
	public DataWatcherHelper helper() {
		return helper;
	}

	/**
	 * Converts the class into an instance of NMS.DataWatcher
	 * @return an instance of NMS.DataWatcher with same data
	 * @throws Exception - if something fails
	 */
	public Object toNMS() throws Exception {
		Object nmsWatcher;
		if (newDataWatcher.getParameterCount() == 1) {
			nmsWatcher = newDataWatcher.newInstance(new Object[] {null});
		} else {
			nmsWatcher = newDataWatcher.newInstance();
		}
		for (DataWatcherItem item : dataValues.values()) {
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
				REGISTER.invoke(nmsWatcher, item.type.toNMS(), item.value);
			} else {
				REGISTER.invoke(nmsWatcher, item.type.position, item.value);
			}
		}
		return nmsWatcher;
	}
	
	/**
	 * Reads NMS data watcher and returns and instance of this class with same data
	 * @param nmsWatcher - NMS datawatcher to read
	 * @return an instance of this class with same values
	 * @throws Exception - if something fails
	 */
	@SuppressWarnings("unchecked")
	public static DataWatcher fromNMS(Object nmsWatcher) throws Exception{
		DataWatcher watcher = new DataWatcher();
		List<Object> items = (List<Object>) nmsWatcher.getClass().getMethod("c").invoke(nmsWatcher);
		if (items != null) {
			for (Object watchableObject : items) {
				DataWatcherItem w = DataWatcherItem.fromNMS(watchableObject);
				watcher.setValue(w.type, w.value);
			}
		}
		return watcher;
	}
}