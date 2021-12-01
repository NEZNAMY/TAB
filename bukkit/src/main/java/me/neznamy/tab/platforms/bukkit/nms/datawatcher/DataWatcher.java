package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;

/**
 * A class representing the n.m.s.DataWatcher class to make work with it much easier
 */
public class DataWatcher {

	//DataWatcher data
	private final Map<Integer, DataWatcherItem> dataValues = new HashMap<>();
	
	//a helper for easier data write
	private final DataWatcherHelper helper = new DataWatcherHelper(this);

	/**
	 * Sets value into data values
	 * @param type - type of value
	 * @param value - value
	 */
	public void setValue(DataWatcherObject type, Object value){
		dataValues.put(type.getPosition(), new DataWatcherItem(type, value));
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
	 * @throws	ReflectiveOperationException
	 * 			if thrown by reflective operation
	 */
	public Object toNMS() throws ReflectiveOperationException {
		NMSStorage nms = NMSStorage.getInstance();
		Object nmsWatcher;
		if (nms.newDataWatcher.getParameterCount() == 1) { //1.7+
			Object[] args = new Object[] {null};
			nmsWatcher = nms.newDataWatcher.newInstance(args);
		} else {
			nmsWatcher = nms.newDataWatcher.newInstance();
		}
		for (DataWatcherItem item : dataValues.values()) {
			Object position;
			if (nms.getMinorVersion() >= 9) {
				position = nms.newDataWatcherObject.newInstance(item.getType().getPosition(), item.getType().getClassType());
			} else {
				position = item.getType().getPosition();
			}
			nms.DataWatcher_REGISTER.invoke(nmsWatcher, position, item.getValue());
		}
		return nmsWatcher;
	}
	
	/**
	 * Reads NMS data watcher and returns and instance of this class with same data
	 * @param nmsWatcher - NMS DataWatcher to read
	 * @return an instance of this class with same values
	 * @throws	ReflectiveOperationException
	 * 			if thrown by reflective operation
	 */
	@SuppressWarnings("unchecked")
	public static DataWatcher fromNMS(Object nmsWatcher) throws ReflectiveOperationException {
		DataWatcher watcher = new DataWatcher();
		List<Object> items = (List<Object>) nmsWatcher.getClass().getMethod("c").invoke(nmsWatcher);
		if (items != null) {
			for (Object watchableObject : items) {
				DataWatcherItem w = DataWatcherItem.fromNMS(watchableObject);
				watcher.setValue(w.getType(), w.getValue());
			}
		}
		return watcher;
	}
}