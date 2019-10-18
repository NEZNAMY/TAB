package me.neznamy.tab.platforms.bukkit.packets;

import java.lang.reflect.Field;
import java.util.List;

import me.neznamy.tab.platforms.bukkit.packets.DataWatcher.Item;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;

public class PacketPlayOutEntityMetadata extends PacketPlayOut{

	public int entityId;
	public List<Item> items;

	public PacketPlayOutEntityMetadata(int entityId, List<Item> items) {
		this.entityId = entityId;
		this.items = items;
	}
	public PacketPlayOutEntityMetadata(int entityId, DataWatcher dataWatcher, boolean force){
		this.entityId = entityId;
		items = (force ? dataWatcher.getAllObjects() : dataWatcher.getObjectsThatNeedUpdate());
	}
	public Object toNMS(ProtocolVersion clientVersion){
		DataWatcher w = new DataWatcher(null);
		for (Item item : items) w.setValue(item.type, item.value);
		return MethodAPI.getInstance().newPacketPlayOutEntityMetadata(entityId, w.toNMS(), true);
	}
	public static final Field LIST = getFields(MethodAPI.PacketPlayOutEntityMetadata).get("b");
}