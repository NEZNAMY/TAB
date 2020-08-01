package me.neznamy.tab.platforms.bukkit.packets;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;

public class DataWatcher{

	private static final Field ENTITY = PacketPlayOut.getObjectAt(PacketPlayOut.getFields(MethodAPI.DataWatcher, MethodAPI.Entity), 0);

	private Object entity;
	private Map<Integer, Item> dataValues = new HashMap<Integer, Item>();
	private DataWatcherHelper helper;

	public DataWatcher(Object entity) {
		this.entity = entity;
		helper = new DataWatcherHelper(this);
	}
	
	public void setValue(DataWatcherObject type, Object value){
		dataValues.put(type.position, new Item(type, value));
	}
	
	public void removeValue(int position) {
		dataValues.remove(position);
	}
	
	public Item getItem(int position) {
		return dataValues.get(position);
	}
	
	public DataWatcherHelper helper() {
		return helper;
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
		for (Item item : dataValues.values()) MethodAPI.getInstance().registerDataWatcherObject(nmsWatcher, item.type, item.value);
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
}