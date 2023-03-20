package me.neznamy.tab.api.feature;

import me.neznamy.tab.api.TabPlayer;

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
