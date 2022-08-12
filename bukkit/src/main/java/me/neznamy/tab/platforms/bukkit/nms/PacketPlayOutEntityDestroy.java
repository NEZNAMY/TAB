package me.neznamy.tab.platforms.bukkit.nms;

import java.util.Arrays;

import me.neznamy.tab.api.protocol.TabPacket;

/**
 * Custom class for holding data used in PacketPlayOutEntityDestroy minecraft packet.
 */
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

    @Override
    public String toString() {
        return String.format("PacketPlayOutEntityDestroy{entities=%s}", Arrays.toString(entities));
    }

    /**
     * Returns {@link #entities}
     * @return  {@link #entities}
     */
    public int[] getEntities() {
        return entities;
    }
}