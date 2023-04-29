package me.neznamy.tab.shared.features.types;

import lombok.NonNull;
import me.neznamy.tab.shared.platform.TabPlayer;

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
    void onDisplayObjective(@NonNull TabPlayer receiver, int slot, @NonNull String objective);
}
