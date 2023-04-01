package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.player.TabPlayer;

/**
 * Interface for features listening to players switching worlds
 */
public interface WorldSwitchListener {

    /**
     * Called when player switched world
     *
     * @param   changed
     *          Player who changed world
     * @param   from
     *          Name of previous world
     * @param   to
     *          Name of new world
     */
    void onWorldChange(TabPlayer changed, String from, String to);
}
