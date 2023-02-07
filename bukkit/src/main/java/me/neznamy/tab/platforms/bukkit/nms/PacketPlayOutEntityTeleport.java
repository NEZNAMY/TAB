package me.neznamy.tab.platforms.bukkit.nms;

import lombok.Data;
import lombok.NonNull;
import me.neznamy.tab.api.protocol.TabPacket;
import org.bukkit.Location;

/**
 * Custom class for holding data used in PacketPlayOutEntityTeleport minecraft packet.
 */
@Data
public class PacketPlayOutEntityTeleport implements TabPacket {

    /** Entity's id */
    private final int entityId;

    /** Entity's new location */
    @NonNull private final Location location;
}