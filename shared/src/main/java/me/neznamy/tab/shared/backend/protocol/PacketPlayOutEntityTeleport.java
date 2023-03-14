package me.neznamy.tab.shared.backend.protocol;

import lombok.Data;
import me.neznamy.tab.api.protocol.TabPacket;

/**
 * Custom class for holding data used in PacketPlayOutEntityTeleport minecraft packet.
 */
@Data
public class PacketPlayOutEntityTeleport implements TabPacket {

    /** Entity's id */
    private final int entityId;

    /** Entity location */
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
}