package me.neznamy.tab.platforms.bukkit.nms;

import me.neznamy.tab.api.protocol.TabPacket;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;

public class PacketPlayOutEntityMetadata implements TabPacket {

	private final int entityId;

	private final DataWatcher dataWatcher;

	public PacketPlayOutEntityMetadata(int entityId, DataWatcher dataWatcher) {
		this.entityId = entityId;
		this.dataWatcher = dataWatcher;
	}

	@Override
	public String toString() {
		return String.format("PacketPlayOutEntityMetadata{entityId=%s,dataWatcher=%s}", entityId, dataWatcher);
	}

	public int getEntityId() {
		return entityId;
	}

	public DataWatcher getDataWatcher() {
		return dataWatcher;
	}
}