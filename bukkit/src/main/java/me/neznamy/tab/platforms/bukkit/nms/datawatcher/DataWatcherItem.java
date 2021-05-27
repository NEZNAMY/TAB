package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;

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
		NMSStorage nms = NMSStorage.getInstance();
		if (NMSStorage.getInstance().minorVersion >= 9) {
			Object nmsObject = nms.DataWatcherItem_TYPE.get(nmsItem);
			return new DataWatcherItem(new DataWatcherObject(nms.DataWatcherObject_SLOT.getInt(nmsObject), nms.DataWatcherObject_SERIALIZER.get(nmsObject)), nms.DataWatcherItem_VALUE.get(nmsItem));
		} else {
			return new DataWatcherItem(new DataWatcherObject(nms.DataWatcherItem_TYPE.getInt(nmsItem), null), nms.DataWatcherItem_VALUE.get(nmsItem));
		}
	}
}