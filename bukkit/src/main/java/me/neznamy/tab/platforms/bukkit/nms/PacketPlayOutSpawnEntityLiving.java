package me.neznamy.tab.platforms.bukkit.nms;

import lombok.Data;
import lombok.NonNull;
import me.neznamy.tab.api.protocol.TabPacket;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

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
    @NonNull private final EntityType entityType;

    /** Entity's spawn location */
    @NonNull private final Location location;

    /** Entity's data for 1.14- servers */
    private final DataWatcher dataWatcher;
}