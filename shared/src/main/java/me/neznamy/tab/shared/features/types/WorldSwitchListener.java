package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.data.World;
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
     *          Previous world
     * @param   to
     *          New world
     */
    void onWorldChange(@NotNull TabPlayer changed, @NotNull World from, @NotNull World to);
}
