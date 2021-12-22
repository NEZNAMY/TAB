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
}
