package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for features listening to gamemode changes.
 */
public interface GameModeListener {

    /**
     * Called when player changes game mode. New game mode is available in
     * {@link TabPlayer#getGamemode()}.
     *
     * @param   player
     *          Player who changed game mode
     */
    void onGameModeChange(@NotNull TabPlayer player);
}
