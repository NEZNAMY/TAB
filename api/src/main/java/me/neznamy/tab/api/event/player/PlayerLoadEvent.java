package me.neznamy.tab.api.event.player;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.TabEvent;

/**
 * Called when the player has been fully loaded by TAB.
 */
public interface PlayerLoadEvent extends TabEvent {

    /**
     * Gets the player that was loaded.
     *
     * @return the player that was loaded
     */
    TabPlayer getPlayer();

    /**
     * Returns true if player was loaded because they joined,
     * false if the player was loaded because tab reload command was executed
     * @return true if player joined, false if plugin was reloaded
     */
    boolean isJoin();
}
