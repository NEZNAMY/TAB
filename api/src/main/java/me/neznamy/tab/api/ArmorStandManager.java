package me.neznamy.tab.api;

/**
 * A helper class for easy management of armor stands of a player
 */
public interface ArmorStandManager {

    /**
     * Sends destroy packet of all armor stands to everyone and clears nearby players list
     */
    void destroy();

    void refresh(boolean force);
}