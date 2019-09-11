package me.neznamy.tab.platforms.bukkit.packets;

import java.lang.reflect.Field;
import java.util.Map;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;

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
	public static DataWatcherObject fromNMS(Object nmsObject) throws Exception{
		int position = DataWatcherObject_POSITION.getInt(nmsObject);
		Object classType = DataWatcherObject_CLASSTYPE.get(nmsObject);
		return new DataWatcherObject(position, classType);
	}

	private static Field DataWatcherObject_POSITION;
	private static Field DataWatcherObject_CLASSTYPE;

	static {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			Map<String, Field> fields = PacketPlayOut.getFields(MethodAPI.DataWatcherObject);
			DataWatcherObject_POSITION = fields.get("a");
			DataWatcherObject_CLASSTYPE = fields.get("b");
		}
	}
}