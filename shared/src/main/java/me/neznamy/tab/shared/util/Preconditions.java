package me.neznamy.tab.shared.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Class with utility methods used in the plugin
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Preconditions {

    /**
     * Checks if player is loaded and throws {@code IllegalStateException} if not.
     *
     * @param   player
     *          Player to check
     * @throws  IllegalStateException
     *          If player is not loaded
     */
    public static void checkLoaded(@NotNull TabPlayer player) {
        if (!player.isLoaded()) throw new IllegalStateException("Player is not loaded yet. Try again later.");
    }
}
