package me.neznamy.tab.api.feature;

import me.neznamy.tab.api.TabPlayer;

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
    void onQuit(TabPlayer disconnectedPlayer);
}
