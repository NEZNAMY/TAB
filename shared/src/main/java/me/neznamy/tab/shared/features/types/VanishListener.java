package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.platform.TabPlayer;

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
