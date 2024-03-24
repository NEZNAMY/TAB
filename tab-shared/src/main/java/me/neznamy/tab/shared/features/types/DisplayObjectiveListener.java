package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

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
    void onDisplayObjective(@NotNull TabPlayer receiver, int slot, @NotNull String objective);
}
