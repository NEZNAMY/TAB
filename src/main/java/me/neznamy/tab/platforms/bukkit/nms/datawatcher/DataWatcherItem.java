package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOut;
import me.neznamy.tab.shared.ProtocolVersion;

public class DataWatcherItem {

	private static Constructor<?> newDataWatcherItem;
	
	public DataWatcherObject type;
	public Object value;

	public static void initializeClass() throws Exception {
		Class<?> DataWatcherItem;
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			//1.9+
			DataWatcherItem = PacketPlayOut.getNMSClass("DataWatcher$Item");
		} else {
			//1.8-
			try {
				//v1_8_R2, v1_8_R3
				DataWatcherItem = PacketPlayOut.getNMSClass("DataWatcher$WatchableObject");
			} catch (ClassNotFoundException e) {
				//v1_8_R1-
				DataWatcherItem = PacketPlayOut.getNMSClass("WatchableObject");
			}
		}
		newDataWatcherItem = DataWatcherItem.getConstructors()[0];
	}
	public DataWatcherItem(DataWatcherObject type, Object value){
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
	public static DataWatcherItem fromNMS(Object nmsItem) throws Exception{
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			DataWatcherObject object = DataWatcherObject.fromNMS(getValue(nmsItem, "a"));
			Object value = getValue(nmsItem, "b");
			return new DataWatcherItem(object, value);
		} else {
			Object classType = getValue(nmsItem, "a");
			int position = (int) getValue(nmsItem, "b");
			Object value = getValue(nmsItem, "c");
			return new DataWatcherItem(new DataWatcherObject(position, classType), value);
		}
	}
	
	public static Object getValue(Object obj, String field) throws Exception {
		Field f = obj.getClass().getDeclaredField(field);
		f.setAccessible(true);
		return f.get(obj);
	}
}