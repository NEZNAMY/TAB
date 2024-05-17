package me.neznamy.tab.shared.features.types;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract class representing a core feature of the plugin.
 */
@RequiredArgsConstructor
public class TabFeature {

    /** Flag tracking whether this instance is active or not */
    private boolean active = true;

    /** Name of this feature display in /tab cpu */
    @Getter
    @NonNull
    private final String featureName;

    /**
     * Marks this instance as no longer active.
     */
    public void deactivate() {
        active = false;
    }

    /**
     * Throws {@link IllegalStateException} if this instance is no longer active.
     */
    public void ensureActive() {
        if (!active) throw new IllegalStateException("This instance got discarded because plugin was reloaded. Obtain a new instance.");
    }

    /**
     * Returns config file.
     *
     * @return  config file
     */
    @NotNull
    public ConfigurationFile config() {
        return TAB.getInstance().getConfiguration().getConfig();
    }
}