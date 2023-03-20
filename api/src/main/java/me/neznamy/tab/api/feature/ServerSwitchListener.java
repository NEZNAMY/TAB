package me.neznamy.tab.api.feature;

import me.neznamy.tab.api.TabPlayer;

/**
 * Interface for features listening to players switching servers
 */
public interface ServerSwitchListener {

    /**
     * Called when player switched server
     *
     * @param   changed
     *          Player who changed server
     * @param   from
     *          Name of previous server
     * @param   to
     *          Name of new server
     */
    void onServerChange(TabPlayer changed, String from, String to);
}
