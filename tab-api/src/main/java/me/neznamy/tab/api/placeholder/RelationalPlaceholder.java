package me.neznamy.tab.api.placeholder;

import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * An interface for relational placeholders (returning different value for each player duo)
 */
@SuppressWarnings("unused") // API class
public interface RelationalPlaceholder extends Placeholder {

    /**
     * Force-updates placeholder value. It will still be overridden by periodic refresh later,
     * so this is (only) useful for force an update when value is supposed to change, and you have
     * a high refresh interval set.
     *
     * @param   viewer
     *          Placeholder viewer
     * @param   target
     *          Placeholder target
     * @param   value
     *          New value
     */
    void updateValue(@NonNull TabPlayer viewer, @NonNull TabPlayer target, @Nullable Object value);

    /**
     * Force-updates placeholder value using previously provided update function.
     *
     * @param   viewer
     *          Placeholder viewer
     * @param   target
     *          Placeholder target
     */
    void update(@NonNull TabPlayer viewer, @NonNull TabPlayer target);
}