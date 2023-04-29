package me.neznamy.tab.shared.features.types;

import lombok.NonNull;
import me.neznamy.tab.shared.platform.TabPlayer;

/**
 * Interface for features listening to player quit event
 */
public interface QuitListener {

    /**
     * Called when player disconnected from the server. The player is
     * still present in online player list.
     *
     * @param   disconnectedPlayer
     *          Player who disconnected
     */
    void onQuit(@NonNull TabPlayer disconnectedPlayer);
}
