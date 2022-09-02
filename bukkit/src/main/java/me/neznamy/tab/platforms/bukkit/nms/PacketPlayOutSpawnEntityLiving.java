package me.neznamy.tab.platforms.bukkit.nms;

import java.util.UUID;

import me.neznamy.tab.api.util.Preconditions;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import me.neznamy.tab.api.protocol.TabPacket;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;

/**
 * Custom class for holding data used in PacketPlayOutSpawnEntityLiving minecraft packet.
 */
public class PacketPlayOutSpawnEntityLiving implements TabPacket {

    /** Entity's id */
    private final int entityId;

    /** Entity's UUID (for 1.9+ servers) */
    private final UUID uuid;

    /** Type of entity */
    private final EntityType entityType;

    /** Entity's spawn location */
    private final Location location;

    /** Entity's data for 1.14- servers */
    private final DataWatcher dataWatcher;

    /**
     * Constructs new instance with given parameters
     *
     * @param   entityId
     *          Entity's id
     * @param   uuid
     *          Entity's UUID (for 1.9+ servers)
     * @param   entityType
     *          Type of entity
     * @param   location
     *          Entity's spawn location
     * @param   dataWatcher
     *          Entity's data for 1.14- servers
     */
    public PacketPlayOutSpawnEntityLiving(int entityId, UUID uuid, EntityType entityType, Location location, DataWatcher dataWatcher) {
        Preconditions.checkNotNull(uuid, "uuid");
        Preconditions.checkNotNull(entityType, "entityType");
        Preconditions.checkNotNull(location, "location");
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

    /**
     * Returns {@link #entityId}
     * @return  {@link #entityId}
     */
    public int getEntityId() {
        return entityId;
    }

    /**
     * Returns {@link #uuid}
     * @return  {@link #uuid}
     */
    public UUID getUniqueId() {
        return uuid;
    }

    /**
     * Returns {@link #entityType}
     * @return  {@link #entityType}
     */
    public EntityType getEntityType() {
        return entityType;
    }

    /**
     * Returns {@link #location}
     * @return  {@link #location}
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Returns {@link #dataWatcher}
     * @return  {@link #dataWatcher}
     */
    public DataWatcher getDataWatcher() {
        return dataWatcher;
    }
}