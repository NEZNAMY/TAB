package me.neznamy.tab.platforms.bukkit.nms;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import me.neznamy.tab.api.protocol.TabPacket;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;

public class PacketPlayOutSpawnEntityLiving implements TabPacket {

	private int entityId;

	private UUID uuid;

	private EntityType entityType;

	private Location location;

	private DataWatcher dataWatcher;

	public PacketPlayOutSpawnEntityLiving(int entityId, UUID uuid, EntityType entityType, Location location, DataWatcher dataWatcher) {
		this.entityId = entityId;
		this.uuid = uuid;
		this.entityType = entityType;
		this.location = location;
		this.dataWatcher = dataWatcher;
	}

	@Override
	public String toString() {
		return String.format("PacketPlayOutSpawnEntityLiving{entityId=%s,uuid=%s,entityType=%s,location=%s,dataWatcher=%s}", 
				entityId, uuid, entityType, location, dataWatcher);
	}

	public int getEntityId() {
		return entityId;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public EntityType getEntityType() {
		return entityType;
	}

	public Location getLocation() {
		return location;
	}

	public DataWatcher getDataWatcher() {
		return dataWatcher;
	}
}