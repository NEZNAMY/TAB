package me.neznamy.tab.api.feature;

import me.neznamy.tab.api.TabPlayer;

import java.util.UUID;

/**
 * Called when a packet to change player's latency is sent.
 */
public interface LatencyListener {

    /**
     * Called when a player receives a packet for latency change.
     *
     * @param   packetReceiver
     *          Player who received the packet
     * @param   id
     *          UUID of affected player
     * @param   latency
     *          Latency in the packet
     * @return  New latency
     */
    int onLatencyChange(TabPlayer packetReceiver, UUID id, int latency);
}
