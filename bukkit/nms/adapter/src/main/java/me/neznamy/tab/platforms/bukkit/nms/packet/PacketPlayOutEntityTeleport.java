package me.neznamy.tab.platforms.bukkit.nms.packet;

import me.neznamy.tab.api.protocol.TabPacket;
import org.bukkit.Location;

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
