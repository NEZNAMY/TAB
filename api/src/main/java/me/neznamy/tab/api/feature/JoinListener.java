package me.neznamy.tab.api.feature;

import me.neznamy.tab.api.TabPlayer;

/**
 * Interface for features listening to player join event
 */
public interface JoinListener {

    /**
     * Called when player connects to the server. The player is
     * already added to online player list.
     *
     * @param   connectedPlayer
     *          Player who connected
     */
    void onJoin(TabPlayer connectedPlayer);
}
