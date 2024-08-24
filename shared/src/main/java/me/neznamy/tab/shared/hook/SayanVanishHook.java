package me.neznamy.tab.shared.hook;

import lombok.Getter;
import lombok.Setter;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class for hooking into SayanVanish to get vanish status of players.
 */
public abstract class SayanVanishHook {

    /** Instance of the class, if SayanVanish is available */
    @Nullable
    @Getter
    @Setter
    private static SayanVanishHook instance;

    /**
     * Returns {@code true} if {@code viewer} can see the {@code target} player,
     * {@code false} if not.
     *
     * @param   viewer
     *          Viewing player
     * @param   target
     *          Player who is being viewed
     * @return  {@code true} if can see, {@code false} if not.
     */
    public abstract boolean canSee(@NotNull TabPlayer viewer, @NotNull TabPlayer target);

    /**
     * Returns {@code true} if player is vanished, {@code false} if not.
     *
     * @param   player
     *          Player to check
     * @return  {@code true} if player is vanished, {@code false} if not.
     */
    public abstract boolean isVanished(@NotNull TabPlayer player);
}
