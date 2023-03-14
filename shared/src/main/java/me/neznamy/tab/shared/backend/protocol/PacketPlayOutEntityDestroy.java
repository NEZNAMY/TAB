package me.neznamy.tab.shared.backend.protocol;

import lombok.Data;
import me.neznamy.tab.api.protocol.TabPacket;

/**
 * Custom class for holding data used in PacketPlayOutEntityDestroy minecraft packet.
 */
@Data
public class PacketPlayOutEntityDestroy implements TabPacket {

    /** Packet's instance fields */
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