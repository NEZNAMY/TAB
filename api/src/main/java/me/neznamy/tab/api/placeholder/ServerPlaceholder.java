package me.neznamy.tab.api.placeholder;

import lombok.NonNull;

/**
 * An interface for server placeholders (returning same value for all players)
 */
public interface ServerPlaceholder extends Placeholder {

    /**
     * Force-updates placeholder value. It will still be overridden by periodic refresh later,
     * so this is (only) useful for force an update when value is supposed to change, and you have
     * a high refresh interval set.
     *
     * @param   value
     *          New value
     */
    void updateValue(@NonNull Object value);
}