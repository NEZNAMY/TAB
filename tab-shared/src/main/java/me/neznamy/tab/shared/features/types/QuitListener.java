package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

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
    void onQuit(@NotNull TabPlayer disconnectedPlayer);
}
