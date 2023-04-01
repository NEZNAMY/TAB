package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.player.TabPlayer;

/**
 * Listener for features listening to objective packet.
 */
public interface ObjectiveListener {

    /**
     * Called when player is about to receive display objective packet
     *
     * @param   receiver
     *          Packet receiver
     * @param   action
     *          Packet action (0 = register, 1 = unregister, 2 = update)
     * @param   objective
     *          Objective name
     */
    void onObjective(TabPlayer receiver, int action, String objective);
}
