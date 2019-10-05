package me.neznamy.tab.platforms.bukkit.packets;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.platforms.bukkit.packets.DataWatcher.Item;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;

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
	public Object toNMS(ProtocolVersion clientVersion){
		DataWatcher w = new DataWatcher(null);
		for (Item item : list) w.setValue(item.type, item.value);
		return MethodAPI.getInstance().newPacketPlayOutEntityMetadata(entityId, w.toNMS(), true);
	}
	@SuppressWarnings("unchecked")
	public static PacketPlayOutEntityMetadata fromNMS(Object nmsPacket) throws Exception{
		if (!MethodAPI.PacketPlayOutEntityMetadata.isInstance(nmsPacket)) return null;
		int entityId = ENTITYID.getInt(nmsPacket);
		List<Item> list = new ArrayList<Item>();
		List<Object> items = (List<Object>)LIST.get(nmsPacket);
		if (items != null) 
			for (Object o : items) {
				list.add(Item.fromNMS(o));
			}
		return new PacketPlayOutEntityMetadata(entityId, list);
	}

	private static Map<String, Field> fields = getFields(MethodAPI.PacketPlayOutEntityMetadata);
	private static Field ENTITYID = fields.get("a");
	private static Field LIST = fields.get("b");
}