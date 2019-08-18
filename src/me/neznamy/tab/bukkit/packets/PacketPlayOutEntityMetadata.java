package me.neznamy.tab.bukkit.packets;

import java.lang.reflect.Field;
import java.util.List;

import com.google.common.collect.Lists;

import me.neznamy.tab.bukkit.packets.DataWatcher.Item;
import me.neznamy.tab.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.Shared;

public class PacketPlayOutEntityMetadata extends PacketPlayOut{

	private int entityId;
	private List<Item> list;

	public PacketPlayOutEntityMetadata(int entityId, List<Item> items) {
		this.entityId = entityId;
		list = items;
	}
	public PacketPlayOutEntityMetadata(int entityId, DataWatcher dataWatcher, boolean force){
		this.entityId = entityId;
		if (force) {
			list = dataWatcher.getAllObjects();
		} else {
			list = dataWatcher.getObjectsThatNeedUpdate();
		}
	}
	public int getEntityId() {
		return entityId;
	}
	public List<Item> getList() {
		return list;
	}
	public Object toNMS() throws Exception{
		DataWatcher w = new DataWatcher(null);
		for (Item item : list) {
			w.setValue(item.getType(), item.getValue());
		}
		return MethodAPI.getInstance().newPacketPlayOutEntityMetadata(entityId, w.toNMS(), true);
	}

	@SuppressWarnings("unchecked")
	public static PacketPlayOutEntityMetadata fromNMS(Object nmsPacket) throws Exception{
		if (!PacketPlayOutEntityMetadata.isInstance(nmsPacket)) return null;
		int entityId = PacketPlayOutEntityMetadata_ENTITYID.getInt(nmsPacket);
		List<Item> list = Lists.newArrayList();
		List<Object> items = (List<Object>)PacketPlayOutEntityMetadata_LIST.get(nmsPacket);
		if (items != null) 
			for (Object o : items) {
				list.add(Item.fromNMS(o));
			}
		return new PacketPlayOutEntityMetadata(entityId, list);
	}

	private static Class<?> PacketPlayOutEntityMetadata;
	private static Field PacketPlayOutEntityMetadata_ENTITYID;
	private static Field PacketPlayOutEntityMetadata_LIST;

	static {
		try {
			PacketPlayOutEntityMetadata = getNMSClass("PacketPlayOutEntityMetadata");
			(PacketPlayOutEntityMetadata_ENTITYID = PacketPlayOutEntityMetadata.getDeclaredField("a")).setAccessible(true);
			(PacketPlayOutEntityMetadata_LIST = PacketPlayOutEntityMetadata.getDeclaredField("b")).setAccessible(true);
		} catch (Throwable e) {
			Shared.error("Failed to initialize PacketPlayOutEntityMetadata class", e);
		}
	}
}