package me.neznamy.tab.api.placeholder;

import me.neznamy.tab.api.TabPlayer;

public interface RelationalPlaceholder extends Placeholder {

    void updateValue(TabPlayer viewer, TabPlayer target, Object value);

    /**
     * Calls the placeholder request function and returns the output.
     * If the placeholder threw an exception, it is logged in {@code placeholder-errors.log}
     * file and "ERROR" is returned.
     *
     * @param   viewer
     *          player looking at output of the placeholder
     * @param   target
     *          player the placeholder is displayed on
     * @return  value placeholder returned or "ERROR" if it threw an error
     */
    Object request(TabPlayer viewer, TabPlayer target);

    /**
     * Returns last known value for given players. First player is viewer,
     * second player is target.
     *
     * @param   viewer
     *          viewer of the placeholder
     * @param   target
     *          target who is the text displayed on
     * @return  last known value for entered player duo
     */
    String getLastValue(TabPlayer viewer, TabPlayer target);
}