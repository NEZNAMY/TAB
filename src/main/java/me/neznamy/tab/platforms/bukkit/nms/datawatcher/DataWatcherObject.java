package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing the n.m.s.DataWatcherObject class to make work with it much easier
 */
public class DataWatcherObject {

	public int position;
	public Object classType;

	public DataWatcherObject(int position, Object classType){
		this.position = position;
		this.classType = classType;
	}
	
	public static DataWatcherObject fromNMS(Object nmsObject) throws Exception {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			int position = (int) DataWatcherItem.getValue(nmsObject, "a");
			Object classType = DataWatcherItem.getValue(nmsObject, "b");
			return new DataWatcherObject(position, classType);
		} else {
			throw new IllegalStateException();
		}
	}
	public Object toNMS() throws Exception {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			return DataWatcher.newDataWatcherObject.newInstance(position, classType);
		} else {
			throw new IllegalStateException();
		}
	}
}