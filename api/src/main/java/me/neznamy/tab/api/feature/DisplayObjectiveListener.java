package me.neznamy.tab.api.feature;

import me.neznamy.tab.api.TabPlayer;

/**
 * Listener for features listening to display objective packet.
 */
public interface DisplayObjectiveListener {

    /**
     * Called when player is about to receive display objective packet
     *
     * @param   receiver
     *          Packet receiver
     * @param   slot
     *          Objective slot
     * @param   objective
     *          Objective name
     */
    void onDisplayObjective(TabPlayer receiver, int slot, String objective);
}
