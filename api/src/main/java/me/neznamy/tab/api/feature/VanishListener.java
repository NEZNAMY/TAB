package me.neznamy.tab.api.feature;

import me.neznamy.tab.api.TabPlayer;

/**
 * Interface for features listening to vanish status change
 */
public interface VanishListener {

    /**
     * Called when player's vanish status changes. New status
     * is available in {@link TabPlayer#isVanished()}.
     *
     * @param   player
     *          Player who changed vanish status
     */
    void onVanishStatusChange(TabPlayer player);
}
