package me.neznamy.tab.platforms.bukkit.packets;

import me.neznamy.tab.shared.ProtocolVersion;

public class DataWatcherObject {

	public int position;
	public Object classType;

	public DataWatcherObject(int position, Object classType){
		this.position = position;
		this.classType = classType;
	}
	
	public static DataWatcherObject fromNMS(Object nmsObject) throws Exception {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			int position = (int) DataWatcher.getValue(nmsObject, "a");
			Object classType = DataWatcher.getValue(nmsObject, "b");
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