package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;

public class DataWatcherItem {
	
	//type of value (position + data type (1.9+))
	private DataWatcherObject type;
	
	//actual data value
	private Object value;
	
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
	 * @throws IllegalAccessException 
	 */
	public static DataWatcherItem fromNMS(Object nmsItem) throws IllegalAccessException {
		NMSStorage nms = NMSStorage.getInstance();
		if (NMSStorage.getInstance().getMinorVersion() >= 9) {
			Object nmsObject = nms.getField("DataWatcherItem_TYPE").get(nmsItem);
			return new DataWatcherItem(new DataWatcherObject(nms.getField("DataWatcherObject_SLOT").getInt(nmsObject), nms.getField("DataWatcherObject_SERIALIZER").get(nmsObject)), nms.getField("DataWatcherItem_VALUE").get(nmsItem));
		} else {
			return new DataWatcherItem(new DataWatcherObject(nms.getField("DataWatcherItem_TYPE").getInt(nmsItem), null), nms.getField("DataWatcherItem_VALUE").get(nmsItem));
		}
	}

	public DataWatcherObject getType() {
		return type;
	}

	public Object getValue() {
		return value;
	}
}