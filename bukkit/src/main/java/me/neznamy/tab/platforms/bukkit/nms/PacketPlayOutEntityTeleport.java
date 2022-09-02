package me.neznamy.tab.platforms.bukkit.nms;

import me.neznamy.tab.api.util.Preconditions;
import org.bukkit.Location;

import me.neznamy.tab.api.protocol.TabPacket;

/**
 * Custom class for holding data used in PacketPlayOutEntityTeleport minecraft packet.
 */
public class PacketPlayOutEntityTeleport implements TabPacket {

    /** Entity's id */
    private final int entityId;

    /** Entity's new location */
    private final Location location;

    /**
     * Constructs new instance with given parameters
     *
     * @param   entityId
     *          Entity's id
     * @param   location
     *          Entity's spawn location
     */
    public PacketPlayOutEntityTeleport(int entityId, Location location) {
        Preconditions.checkNotNull(location, "location");
        this.entityId = entityId;
        this.location = location;
    }

    @Override
    public String toString() {
        return String.format("PacketPlayOutEntityTeleport{entityId=%s,location=%s}", entityId, location);
    }

    /**
     * Returns {@link #entityId}
     * @return  {@link #entityId}
     */
    public int getEntityId() {
        return entityId;
    }

    /**
     * Returns {@link #location}
     * @return  {@link #location}
     */
    public Location getLocation() {
        return location;
    }
}