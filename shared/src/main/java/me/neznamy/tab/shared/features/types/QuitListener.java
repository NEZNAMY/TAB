package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.player.TabPlayer;

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
