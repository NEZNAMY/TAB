package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for features listening to player join event
 */
public interface JoinListener {

    /**
     * Called when player connects to the server. The player is
     * already added to online player list.
     *
     * @param   connectedPlayer
     *          Player who connected
     */
    void onJoin(@NotNull TabPlayer connectedPlayer);
}
