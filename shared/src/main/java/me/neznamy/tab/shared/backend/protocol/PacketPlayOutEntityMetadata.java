package me.neznamy.tab.shared.backend.protocol;

import lombok.Data;
import lombok.NonNull;
import me.neznamy.tab.api.protocol.TabPacket;

/**
 * Custom class for holding data used in PacketPlayOutEntityMetadata minecraft packet.
 */
@Data
public class PacketPlayOutEntityMetadata implements TabPacket {

    /** Entity's id */
    private final int entityId;

    /** Entity's platform specific data watcher object */
    @NonNull private final Object dataWatcher;
}