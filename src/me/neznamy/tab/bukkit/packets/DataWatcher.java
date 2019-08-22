package me.neznamy.tab.bukkit.packets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import gnu.trove.map.hash.TIntObjectHashMap;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;

public class DataWatcher{

	private Object entity;
	private Map<Integer, Item> dataValues = new HashMap<Integer, Item>();

	public DataWatcher(Object entity) {
		this.entity = entity;
	}
	public void setValue(DataWatcherObject type, Object value){
		dataValues.put(type.getPosition(), new Item(type, value));
	}
	public Item getItem(int position) {
		return dataValues.get(position);
	}
	public List<Item> getObjectsThatNeedUpdate(){
		ArrayList<Item> arraylist = Lists.newArrayList();
		for (Item object : dataValues.values()) {
			if (object.needsUpdate()){
				object.setNeedsUpdate(false);
				arraylist.add(object);
			}
		}
		if (arraylist.isEmpty()) arraylist = null;
		return arraylist;
	}
	public List<Item> getAllObjects(){
		ArrayList<Item> arraylist = Lists.newArrayList();
		arraylist.addAll(dataValues.values());
		return arraylist;
	}

	public static class Item{

		private DataWatcherObject type;
		private Object value;
		private boolean needsUpdate;

		public Item(DataWatcherObject type, Object value){
			this.type = type;
			this.value = value;
			this.needsUpdate = true;
		}
		public Item setValue(Object value){
			this.value = value;
			return this;
		}
		public Item setNeedsUpdate(boolean flag){
			this.needsUpdate = flag;
			return this;
		}
		public DataWatcherObject getType(){
			return type;
		}
		public Object getValue(){
			return value;
		}
		public boolean needsUpdate(){
			return needsUpdate;
		}
		public Object toNMS() throws Exception{
			Object nmsObject;
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
				nmsObject = newItem.newInstance(type.toNMS(), value);
			} else {
				nmsObject = newItem.newInstance(type.getClassType(), type.getPosition(), value);
			}
			Item_NEEDSUPDATE.set(nmsObject, needsUpdate);
			return nmsObject;
		}

		public static Item fromNMS(Object nmsObject) throws Exception{
			Object value = Item_VALUE.get(nmsObject);
			boolean needsUpdate = Item_NEEDSUPDATE.getBoolean(nmsObject);
			DataWatcherObject type;
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
				type = DataWatcherObject.fromNMS(Item_CLASSTYPE.get(nmsObject));
			} else {
				int classType = Item_CLASSTYPE.getInt(nmsObject);
				int position = Item_POSITION.getInt(nmsObject);
				type = new DataWatcherObject(position, classType);
			}
			return new Item(type, value).setNeedsUpdate(needsUpdate);
		}

		private static Class<?> Item;
		private static Constructor<?> newItem;
		private static Field Item_CLASSTYPE;
		private static Field Item_POSITION;
		private static Field Item_VALUE;
		private static Field Item_NEEDSUPDATE;

		static {
			try {
				if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
					Item = NMSClass.get("DataWatcher$Item");
					newItem = NMSClass.getConstructor(Item, 2);
					(Item_CLASSTYPE = Item.getDeclaredField("a")).setAccessible(true);
					(Item_VALUE = Item.getDeclaredField("b")).setAccessible(true);
					(Item_NEEDSUPDATE = Item.getDeclaredField("c")).setAccessible(true);
				} else {
					if (ProtocolVersion.packageName.equals("v1_8_R1")) {
						Item = NMSClass.get("WatchableObject");
					} else {
						Item = NMSClass.get("DataWatcher$WatchableObject");
					}
					newItem = NMSClass.getConstructor(Item, 3);
					(Item_CLASSTYPE = Item.getDeclaredField("a")).setAccessible(true);
					(Item_POSITION = Item.getDeclaredField("b")).setAccessible(true);
					(Item_VALUE = Item.getDeclaredField("c")).setAccessible(true);
					(Item_NEEDSUPDATE = Item.getDeclaredField("d")).setAccessible(true);
				}
			} catch (Throwable e) {
				Shared.error("Failed to initialize DataWatcherItem class", e);
			}
		}
	}
	public Object toNMS() throws Exception{
		Object nmsWatcher = newDataWatcher.newInstance(entity);
		for (Item item : dataValues.values()) {
			DataWatcher_register.invoke(nmsWatcher, item.getType().getNMSKey(), item.getValue());
		}
		return nmsWatcher;
	}
	@SuppressWarnings("unchecked")
	public static DataWatcher fromNMS(Object nmsWatcher) throws Exception{
		DataWatcher watcher = new DataWatcher(DataWatcher_ENTITY.get(nmsWatcher));
		Object map = DataWatcher_DATAVALUES.get(nmsWatcher);
		Collection<Object> values;
		if (map instanceof Map) {
			//1.9+, tacospigot, paperspigot
			values = ((Map<?, Object>)map).values();
		} else {
			//1.8.x
			values = ((TIntObjectHashMap<Object>)map).valueCollection();
		}
		for (Object watchableObject : values) {
			Item w = Item.fromNMS(watchableObject);
			watcher.setValue(w.getType(), w.getValue());
		}
		return watcher;
	}

	private static Class<?> DataWatcher;
	private static Constructor<?> newDataWatcher;
	private static Field DataWatcher_ENTITY;
	private static Field DataWatcher_DATAVALUES;
	private static Method DataWatcher_register;

	static {
		try {
			DataWatcher = NMSClass.get("DataWatcher");
			newDataWatcher = NMSClass.getConstructor(DataWatcher, 1);
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
				DataWatcher_register = DataWatcher.getMethod("register", DataWatcherObject.DataWatcherObject, Object.class);
				if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 10) {
					//1.10+
					try {
						//1.14.4+
						(DataWatcher_ENTITY = DataWatcher.getDeclaredField("entity")).setAccessible(true);
						(DataWatcher_DATAVALUES = DataWatcher.getDeclaredField("entries")).setAccessible(true);
					} catch (Throwable e) {
						//1.14.3-
						(DataWatcher_ENTITY = DataWatcher.getDeclaredField("c")).setAccessible(true);
						(DataWatcher_DATAVALUES = DataWatcher.getDeclaredField("d")).setAccessible(true);
					}
				} else {
					//1.9.x
					(DataWatcher_ENTITY = DataWatcher.getDeclaredField("b")).setAccessible(true);
					(DataWatcher_DATAVALUES = DataWatcher.getDeclaredField("c")).setAccessible(true);
				}
			} else {
				//1.8.x
				DataWatcher_register = DataWatcher.getMethod("a", int.class, Object.class);
				(DataWatcher_ENTITY = DataWatcher.getDeclaredField("a")).setAccessible(true);
				(DataWatcher_DATAVALUES = DataWatcher.getDeclaredField("dataValues")).setAccessible(true);
			}
		} catch (Throwable e) {
			Shared.error("Failed to initialize DataWatcher class", e);
		}
	}
}