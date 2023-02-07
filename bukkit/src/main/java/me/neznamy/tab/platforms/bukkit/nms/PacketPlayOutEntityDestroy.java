package me.neznamy.tab.platforms.bukkit.nms;

import lombok.Data;
import me.neznamy.tab.api.protocol.TabPacket;

/**
 * Custom class for holding data used in PacketPlayOutEntityDestroy minecraft packet.
 */
@Data
public class PacketPlayOutEntityDestroy implements TabPacket {

    /** Destroyed entities */
    private final int[] entities;

    /**
     * Constructs new instance with given parameter
     *
     * @param   entities
     *          Destroyed entities
     */
    public PacketPlayOutEntityDestroy(int... entities) {
        this.entities = entities;
    }
}