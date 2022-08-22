package me.neznamy.tab.api;

/**
 * A helper class for easy management of armor stands of a player
 */
public interface ArmorStandManager {

    /**
     * Sends destroy packet of all armor stands to everyone and clears nearby players list
     */
    void destroy();

    /**
     * Refreshes text of all armor stands for all nearby players
     *
     * @param   force
     *          If refresh should be force despite no update seemingly being needed
     */
    void refresh(boolean force);
}