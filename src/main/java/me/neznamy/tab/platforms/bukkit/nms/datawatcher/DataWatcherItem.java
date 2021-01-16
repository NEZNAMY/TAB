package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import java.lang.reflect.Field;

import me.neznamy.tab.shared.ProtocolVersion;

public class DataWatcherItem {
	
	//type of value (position + data type (1.9+))
	public DataWatcherObject type;
	
	//actual data value
	public Object value;
	
	/**
	 * Constructs new instance of the object with given parameters
	 * @param type - value type
	 * @param value - value
	 */
	public DataWatcherItem(DataWatcherObject type, Object value){
		this.type = type;
		this.value = value;
	}
	
	/**
	 * Returns and instance of this class from given NMS item
	 * @param nmsItem - NMS item
	 * @return instance of this class with same data
	 * @throws Exception - if something fails
	 */
	public static DataWatcherItem fromNMS(Object nmsItem) throws Exception {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			Object nmsObject = getValue(nmsItem, "a");
			DataWatcherObject object = new DataWatcherObject((int) DataWatcherItem.getValue(nmsObject, "a"), DataWatcherItem.getValue(nmsObject, "b"));
			return new DataWatcherItem(object, getValue(nmsItem, "b"));
		} else {
			return new DataWatcherItem(new DataWatcherObject((int) getValue(nmsItem, "b"), getValue(nmsItem, "a")), getValue(nmsItem, "c"));
		}
	}
	
	/**
	 * Returns value of a field
	 * @param obj - object to get value from
	 * @param field - name of field to get
	 * @return value of field
	 * @throws Exception - if something fails
	 */
	public static Object getValue(Object obj, String field) throws Exception {
		Field f = obj.getClass().getDeclaredField(field);
		f.setAccessible(true);
		return f.get(obj);
	}
}