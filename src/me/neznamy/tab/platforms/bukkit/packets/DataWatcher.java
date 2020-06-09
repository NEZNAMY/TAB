package me.neznamy.tab.platforms.bukkit.packets;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.packets.IChatBaseComponent;

public class DataWatcher{

	private static final Field ENTITY = PacketPlayOut.getObjectAt(PacketPlayOut.getFields(MethodAPI.DataWatcher, MethodAPI.Entity), 0);

	private Object entity;
	private Map<Integer, Item> dataValues = new HashMap<Integer, Item>();

	public DataWatcher(Object entity) {
		this.entity = entity;
	}
	public void setValue(DataWatcherObject type, Object value){
		dataValues.put(type.position, new Item(type, value));
	}
	public Item getItem(int position) {
		return dataValues.get(position);
	}

	public static class Item{

		public DataWatcherObject type;
		public Object value;
		public boolean needsUpdate;

		public Item(DataWatcherObject type, Object value){
			this.type = type;
			this.value = value;
			this.needsUpdate = true;
		}
		public Item setNeedsUpdate(boolean needsUpdate) {
			this.needsUpdate = needsUpdate;
			return this;
		}
		public Object toNMS(){
			return MethodAPI.getInstance().newDataWatcherItem(type, value, needsUpdate);
		}
		public static Item fromNMS(Object nmsObject){
			return MethodAPI.getInstance().readDataWatcherItem(nmsObject);
		}
	}
	public Object toNMS(){
		Object nmsWatcher = MethodAPI.getInstance().newDataWatcher(entity);
		for (Item item : dataValues.values()) MethodAPI.getInstance().DataWatcher_register(nmsWatcher, item.type, item.value);
		return nmsWatcher;
	}
	public static DataWatcher fromNMS(Object nmsWatcher) throws Exception{
		DataWatcher watcher = new DataWatcher(ENTITY.get(nmsWatcher));
		List<Object> items = MethodAPI.getInstance().getDataWatcherItems(nmsWatcher);
		if (items != null) {
			for (Object watchableObject : items) {
				Item w = Item.fromNMS(watchableObject);
				watcher.setValue(w.type, w.value);
			}
		}
		return watcher;
	}
	public static class DataWatcherObject{

		public int position;
		public Object classType;

		public DataWatcherObject(int position, Object classType){
			this.position = position;
			this.classType = classType;
		}
	}
	public static class Helper{
		
		private static final int ARMOR_STAND_BYTEFLAGS_POSITION = getArmorStandFlagsPosition();
		
		private static int getArmorStandFlagsPosition() {
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 15) {
				//1.15+
				return 14;
			} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 14) {
				//1.14.x
				return 13;
			} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 10) {
				//1.10.x - 1.13.x
				return 11;
			} else {
				//1.8.1 - 1.9.x
				return 10;
			}
		}
		
		public static void setEntityFlags(DataWatcher dataWatcher, byte flags) {
			dataWatcher.setValue(new DataWatcherObject(0, DataWatcherSerializer.Byte), flags);
		}
		public static void setCustomName(DataWatcher dataWatcher, String customName, ProtocolVersion clientVersion) {
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
				dataWatcher.setValue(new DataWatcherObject(2, DataWatcherSerializer.Optional_IChatBaseComponent), Optional.ofNullable(MethodAPI.getInstance().ICBC_fromString(IChatBaseComponent.fromColoredText(customName).toString(clientVersion))));
			} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8){
				dataWatcher.setValue(new DataWatcherObject(2, DataWatcherSerializer.String), customName);
			} else {
				if (customName.length() > 64) customName = customName.substring(0, 64);
				
				if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 6){
					dataWatcher.setValue(new DataWatcherObject(10, DataWatcherSerializer.String), customName);
				} else {
					dataWatcher.setValue(new DataWatcherObject(5, DataWatcherSerializer.String), customName);
				}
			}
				
		}
		public static void setCustomNameVisible(DataWatcher dataWatcher, boolean visible) {
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
				dataWatcher.setValue(new DataWatcherObject(3, DataWatcherSerializer.Boolean), visible);
			} else {
				dataWatcher.setValue(new DataWatcherObject(3, DataWatcherSerializer.Byte), (byte)(visible?1:0));
			}
		}
		public static void setHealth(DataWatcher dataWatcher, float health) {
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 6) {
				dataWatcher.setValue(new DataWatcherObject(6, DataWatcherSerializer.Float), health);
			} else {
				dataWatcher.setValue(new DataWatcherObject(16, DataWatcherSerializer.Integer), (int)health);
			}
		}
		public static void setArmorStandFlags(DataWatcher dataWatcher, byte flags) {
			dataWatcher.setValue(new DataWatcherObject(ARMOR_STAND_BYTEFLAGS_POSITION, DataWatcherSerializer.Byte), flags);
		}
	}
}