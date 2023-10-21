package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for features listening to players switching worlds
 */
public interface WorldSwitchListener {

    /**
     * Called when player switched world
     *
     * @param   changed
     *          Player who changed world
     * @param   from
     *          Name of previous world
     * @param   to
     *          Name of new world
     */
    void onWorldChange(@NotNull TabPlayer changed, @NotNull String from, @NotNull String to);
}
