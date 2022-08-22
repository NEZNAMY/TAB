package me.neznamy.tab.platforms.bukkit.nms;

import me.neznamy.tab.api.protocol.TabPacket;
import me.neznamy.tab.api.util.Preconditions;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;

/**
 * Custom class for holding data used in PacketPlayOutEntityMetadata minecraft packet.
 */
public class PacketPlayOutEntityMetadata implements TabPacket {

    /** Entity's id */
    private final int entityId;

    /** Entity's data that changed */
    private final DataWatcher dataWatcher;

    /**
     * Constructs new instance with given parameters
     *
     * @param   entityId
     *          Entity's id
     * @param   dataWatcher
     *          Entity's data that changed
     */
    public PacketPlayOutEntityMetadata(int entityId, DataWatcher dataWatcher) {
        Preconditions.checkNotNull(dataWatcher, "data watcher");
        this.entityId = entityId;
        this.dataWatcher = dataWatcher;
    }

    @Override
    public String toString() {
        return String.format("PacketPlayOutEntityMetadata{entityId=%s,dataWatcher=%s}", entityId, dataWatcher);
    }

    /**
     * Returns {@link #entityId}
     * @return  {@link #entityId}
     */
    public int getEntityId() {
        return entityId;
    }

    /**
     * Returns {@link #dataWatcher}
     * @return  {@link #dataWatcher}
     */
    public DataWatcher getDataWatcher() {
        return dataWatcher;
    }
}