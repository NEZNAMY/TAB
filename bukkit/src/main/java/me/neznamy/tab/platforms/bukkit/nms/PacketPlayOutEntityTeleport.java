package me.neznamy.tab.platforms.bukkit.nms;

import org.bukkit.Location;

import me.neznamy.tab.api.protocol.TabPacket;

public class PacketPlayOutEntityTeleport implements TabPacket {

	private final int entityId;

	private final Location location;

	public PacketPlayOutEntityTeleport(int entityId, Location location) {
		this.entityId = entityId;
		this.location = location;
	}

	@Override
	public String toString() {
		return String.format("PacketPlayOutEntityTeleport{entityId=%s,location=%s}", entityId, location);
	}

	public int getEntityId() {
		return entityId;
	}

	public Location getLocation() {
		return location;
	}
}