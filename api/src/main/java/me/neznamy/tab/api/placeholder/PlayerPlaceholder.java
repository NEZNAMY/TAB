package me.neznamy.tab.api.placeholder;

import me.neznamy.tab.api.TabPlayer;

public interface PlayerPlaceholder extends Placeholder {

    void updateValue(TabPlayer player, Object value);

    /**
     * Calls the placeholder request function and returns the output.
     * If the placeholder threw an exception, it is logged in {@code placeholder-errors.log}
     * file and "ERROR" is returned.
     *
     * @param   p
     *          player to get placeholder value for
     * @return  value placeholder returned or "ERROR" if it threw an error
     */
    Object request(TabPlayer p);

    /**
     * Returns last known value for given player.
     *
     * @param   player
     *          player to get last value for
     * @return  last known value for entered player
     */
    String getLastValue(TabPlayer player);
}