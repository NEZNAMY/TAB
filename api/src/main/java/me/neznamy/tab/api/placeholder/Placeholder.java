package me.neznamy.tab.api.placeholder;

import org.jetbrains.annotations.NotNull;

/**
 * An interface representing some kind of placeholder
 */
public interface Placeholder {

    /**
     * Returns refresh interval of this placeholder
     *
     * @return  refresh interval of this placeholder
     */
    int getRefresh();

    /**
     * Returns placeholder's identifier
     *
     * @return  placeholder's identifier
     */
    @NotNull String getIdentifier();
}