package me.neznamy.tab.platforms.sponge8.nms;

import lombok.Data;
import lombok.NonNull;
import me.neznamy.tab.api.protocol.TabPacket;

import java.util.UUID;

/**
 * Custom class for holding data used in PacketPlayOutSpawnEntityLiving minecraft packet.
 */
@Data
public class PacketPlayOutSpawnEntityLiving implements TabPacket {

    /** Entity's id */
    private final int entityId;

    /** Entity's UUID (for 1.9+ servers) */
    @NonNull private final UUID uniqueId;

    /** Type of entity */
    private final int entityType;

    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
}