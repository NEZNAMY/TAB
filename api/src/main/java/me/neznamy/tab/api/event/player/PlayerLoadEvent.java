package me.neznamy.tab.api.event.player;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.TabEvent;

/**
 * Called when the player has been fully loaded by TAB.
 */
public final class PlayerLoadEvent implements TabEvent {

    private final TabPlayer player;

    public PlayerLoadEvent(final TabPlayer player) {
        this.player = player;
    }

    public TabPlayer getPlayer() {
        return player;
    }
}
