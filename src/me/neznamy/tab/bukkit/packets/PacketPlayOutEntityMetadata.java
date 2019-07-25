package me.neznamy.tab.bukkit.packets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

import com.google.common.collect.Lists;

import me.neznamy.tab.bukkit.packets.DataWatcher.Item;
import me.neznamy.tab.shared.Shared;

public class PacketPlayOutEntityMetadata extends PacketPlayOut{

	private int entityId;
	private List<Item> list;

	public PacketPlayOutEntityMetadata() {
	}
	public PacketPlayOutEntityMetadata(int entityId, DataWatcher dataWatcher, boolean force){
		this.entityId = entityId;
		if (force) {
			list = dataWatcher.getAllObjects();
		} else {
			list = dataWatcher.getObjectsThatNeedUpdate();
		}
	}
	public PacketPlayOutEntityMetadata setEntityId(int entityId) {
		this.entityId = entityId;
		return this;
	}
	public PacketPlayOutEntityMetadata setList(List<Item> list) {
		this.list = list;
		return this;
	}
	public int getEntityId() {
		return entityId;
	}
	public List<Item> getList() {
		return list;
	}
	public Object toNMS() throws Exception{
		Object nmsPacket = newPacketPlayOutEntityMetadata.newInstance();
		PacketPlayOutEntityMetadata_ENTITYID.set(nmsPacket, entityId);
		List<Object> list = Lists.newArrayList();
		for (Item o : this.list) {
			list.add(o.toNMS());
		}
		PacketPlayOutEntityMetadata_LIST.set(nmsPacket, list);
		return nmsPacket;
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
		return new PacketPlayOutEntityMetadata().setEntityId(entityId).setList(list);
	}

	private static Class<?> PacketPlayOutEntityMetadata;
	private static Constructor<?> newPacketPlayOutEntityMetadata;
	private static Field PacketPlayOutEntityMetadata_ENTITYID;
	private static Field PacketPlayOutEntityMetadata_LIST;

	static {
		try {
			PacketPlayOutEntityMetadata = getNMSClass("PacketPlayOutEntityMetadata");
			newPacketPlayOutEntityMetadata = PacketPlayOutEntityMetadata.getConstructor();
			(PacketPlayOutEntityMetadata_ENTITYID = PacketPlayOutEntityMetadata.getDeclaredField("a")).setAccessible(true);
			(PacketPlayOutEntityMetadata_LIST = PacketPlayOutEntityMetadata.getDeclaredField("b")).setAccessible(true);
		} catch (Exception e) {
			Shared.error("Failed to initialize PacketPlayOutEntityMetadata class", e);
		}
	}
}