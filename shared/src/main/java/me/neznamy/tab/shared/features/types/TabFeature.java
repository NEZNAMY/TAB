package me.neznamy.tab.shared.features.types;

import org.jetbrains.annotations.NotNull;

/**
 * Abstract class representing a core feature of the plugin.
 */
public abstract class TabFeature {

    /**
     * Returns name of this feature displayed in /tab cpu
     *
     * @return  name of this feature display in /tab cpu
     */
    public abstract @NotNull String getFeatureName();
}