package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.player.TabPlayer;

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
