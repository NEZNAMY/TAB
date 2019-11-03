package me.neznamy.tab.platforms.bukkit.packets;

import java.lang.reflect.Field;
import java.util.*;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;

public class DataWatcher{

	private static final Field ENTITY = PacketPlayOut.getFields(MethodAPI.DataWatcher, MethodAPI.Entity).get(0);
	
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
	public List<Item> getObjectsThatNeedUpdate(){
		ArrayList<Item> arraylist = new ArrayList<Item>();
		for (Item object : dataValues.values()) {
			if (object.needsUpdate){
				object.needsUpdate = false;
				arraylist.add(object);
			}
		}
		if (arraylist.isEmpty()) arraylist = null;
		return arraylist;
	}
	public List<Item> getAllObjects(){
		ArrayList<Item> arraylist = new ArrayList<Item>();
		arraylist.addAll(dataValues.values());
		return arraylist;
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
		for (Object watchableObject : MethodAPI.getInstance().getDataWatcherItems(nmsWatcher)) {
			Item w = Item.fromNMS(watchableObject);
			watcher.setValue(w.type, w.value);
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