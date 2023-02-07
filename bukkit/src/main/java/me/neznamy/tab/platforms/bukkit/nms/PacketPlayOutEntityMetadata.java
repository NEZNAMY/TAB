package me.neznamy.tab.platforms.bukkit.nms;

import lombok.Data;
import lombok.NonNull;
import me.neznamy.tab.api.protocol.TabPacket;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;

/**
 * Custom class for holding data used in PacketPlayOutEntityMetadata minecraft packet.
 */
@Data
public class PacketPlayOutEntityMetadata implements TabPacket {

    /** Entity's id */
    private final int entityId;

    /** Entity's data that changed */
    @NonNull private final DataWatcher dataWatcher;
}