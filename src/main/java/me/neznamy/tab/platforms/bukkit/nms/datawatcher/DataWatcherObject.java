package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing the n.m.s.DataWatcherObject class to make work with it much easier
 */
public class DataWatcherObject {

	//position in datawatcher
	public int position;
	
	//value class type used since 1.9
	public Object classType;

	/**
	 * Constructs a new instance of this class with given parameters
	 * @param position - position in datawatcher
	 * @param classType - value class type
	 */
	public DataWatcherObject(int position, Object classType){
		this.position = position;
		this.classType = classType;
	}
	
	/**
	 * Returns an instance of this class from given NMS object
	 * @param nmsObject - nms object
	 * @return an instance of this class from given object
	 * @throws Exception - if something fails
	 */
	public static DataWatcherObject fromNMS(Object nmsObject) throws Exception {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			int position = (int) DataWatcherItem.getValue(nmsObject, "a");
			Object classType = DataWatcherItem.getValue(nmsObject, "b");
			return new DataWatcherObject(position, classType);
		} else {
			throw new IllegalStateException();
		}
	}
	
	/**
	 * Converts this class to NMS object and returns it
	 * @return NMS version of this object
	 * @throws Exception if something fails
	 */
	public Object toNMS() throws Exception {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			return DataWatcher.newDataWatcherObject.newInstance(position, classType);
		} else {
			throw new IllegalStateException();
		}
	}
}