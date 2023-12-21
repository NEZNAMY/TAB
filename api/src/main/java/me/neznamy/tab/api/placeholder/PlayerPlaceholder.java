package me.neznamy.tab.api.placeholder;

import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for player placeholders (placeholders returning different value for each player)
 */
public interface PlayerPlaceholder extends Placeholder {

    /**
     * Force-updates placeholder value. It will still be overridden by periodic refresh later,
     * so this is (only) useful for force an update when value is supposed to change, and you have
     * a high refresh interval set.
     *
     * @param   player
     *          Player to update value for
     * @param   value
     *          New value
     */
    void updateValue(@NonNull TabPlayer player, @Nullable Object value);

    /**
     * Force-updates placeholder value using previously provided update function.
     *
     * @param   player
     *          Player to update value for
     */
    void update(@NonNull TabPlayer player);
}