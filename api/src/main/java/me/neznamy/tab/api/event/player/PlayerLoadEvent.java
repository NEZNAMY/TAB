package me.neznamy.tab.api.event.player;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.TabEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the player has been fully loaded by TAB.
 */
public interface PlayerLoadEvent extends TabEvent {

    /**
     * Gets the player that was loaded.
     *
     * @return  the player that was loaded
     */
    @NotNull TabPlayer getPlayer();

    /**
     * Returns {@code true} if player was loaded because they joined,
     * {@code false} if the player was loaded because tab reload command was executed
     *
     * @return  {@code true} if player joined, {@code false} if plugin was reloaded
     */
    boolean isJoin();
}
