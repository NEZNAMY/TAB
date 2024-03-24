package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.platform.TabPlayer;

import java.util.UUID;

/**
 * An interface for features listening to ping change packet
 */
public interface LatencyListener {

    /**
     * Called when latency of a player
     * @param   viewer
     *          Player who received the packet
     * @param   packetId
     *          UUID of the entry
     * @param   ping
     *          Original ping in the packet
     * @return  New ping to use
     */
    int onLatencyChange(TabPlayer viewer, UUID packetId, int ping);
}
