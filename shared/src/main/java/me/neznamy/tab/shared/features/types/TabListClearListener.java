package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for features listening to tablist entry clear
 * on server switch to re-add entries.
 */
public interface TabListClearListener {

    /**
     * Called after player's TabList is cleared by the proxy
     * on server switch.
     *
     * @param   player
     *          Player who got their TabList entries cleared
     */
    void onTabListClear(@NotNull TabPlayer player);
}
