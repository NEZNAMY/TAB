package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.player.TabPlayer;

import java.util.Collection;

/**
 * Interface for features periodically refreshing visuals
 */
public interface Refreshable {

    /**
     * Called when a placeholder used by this feature changes value
     *
     * @param   refreshed
     *          Player which a placeholder changed value for
     * @param   force
     *          Whether refresh should be forced
     */
    void refresh(TabPlayer refreshed, boolean force);

    /**
     * Returns display name of {@link #refresh(TabPlayer, boolean)}
     * called for this feature in /tab cpu
     *
     * @return  Display name of refresh function in this feature
     */
    String getRefreshDisplayName();

    /**
     * Registers this feature as one using specified placeholders
     *
     * @param   placeholders
     *          placeholders to add as used in this feature
     */
    default void addUsedPlaceholders(Collection<String> placeholders) {
        if (placeholders.isEmpty()) return;
        placeholders.forEach(p -> TAB.getInstance().getPlaceholderManager().addUsedPlaceholder(p, this));
    }

    /**
     * Returns name of this feature displayed in /tab cpu
     *
     * @return  name of this feature display in /tab cpu
     */
    String getFeatureName();
}