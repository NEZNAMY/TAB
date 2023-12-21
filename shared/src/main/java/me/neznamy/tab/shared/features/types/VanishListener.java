package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for features listening to vanish status change
 */
public interface VanishListener {

    /**
     * Called when a player's vanish status changes. New status
     * is available in {@link TabPlayer#isVanished()}.
     *
     * @param   player
     *          Player who changed vanish status
     */
    void onVanishStatusChange(@NotNull TabPlayer player);
}
