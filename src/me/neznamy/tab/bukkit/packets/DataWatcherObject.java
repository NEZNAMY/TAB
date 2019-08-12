package me.neznamy.tab.bukkit.packets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;

public class DataWatcherObject{
	
	private int position;
	private Object classType;

	public DataWatcherObject(int position, Object classType){
		this.position = position;
		this.classType = classType;
	}
	public int getPosition(){
		return position;
	}
	public Object getClassType(){
		return classType;
	}
	public Object toNMS() throws Exception{
		return newDataWatcherObject.newInstance(position, classType);
	}
	public Object getNMSKey() throws Exception{
		return ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9 ? toNMS() : position;
	}
	
	public static DataWatcherObject fromNMS(Object nmsObject) throws Exception{
		int position = DataWatcherObject_POSITION.getInt(nmsObject);
		Object classType = DataWatcherObject_CLASSTYPE.get(nmsObject);
		return new DataWatcherObject(position, classType);
	}
	
	public static Class<?> DataWatcherObject;
	private static Constructor<?> newDataWatcherObject;
	private static Field DataWatcherObject_POSITION;
	private static Field DataWatcherObject_CLASSTYPE;
	
	static {
		try {
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
				DataWatcherObject = NMSClass.get("DataWatcherObject");
				newDataWatcherObject = NMSClass.getConstructor(DataWatcherObject, 2);
				(DataWatcherObject_POSITION = DataWatcherObject.getDeclaredField("a")).setAccessible(true);
				(DataWatcherObject_CLASSTYPE = DataWatcherObject.getDeclaredField("b")).setAccessible(true);
			}
		} catch (Exception e) {
			Shared.error("Failed to initialize DataWatcherObject class", e);
		}
	}
}