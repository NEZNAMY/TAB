package me.neznamy.tab.shared.features.types;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Interface for features periodically refreshing visuals
 */
@Getter
public abstract class RefreshableFeature extends TabFeature {

    /** Display name of {@link #refresh(TabPlayer, boolean)} called for this feature in /tab cpu */
    @NonNull
    private final String refreshDisplayName;

    /**
     * Constructs new instance with given parameters.
     *
     * @param   featureName
     *          Name of this feature display in /tab cpu
     * @param   refreshDisplayName
     *          Display name of {@link #refresh(TabPlayer, boolean)} called for this feature in /tab cpu
     */
    protected RefreshableFeature(@NonNull String featureName, @NonNull String refreshDisplayName) {
        super(featureName);
        this.refreshDisplayName = refreshDisplayName;
    }

    /**
     * Called when a placeholder used by this feature changes value
     *
     * @param   refreshed
     *          Player which a placeholder changed value for
     * @param   force
     *          Whether refresh should be forced
     */
    public abstract void refresh(@NotNull TabPlayer refreshed, boolean force);

    /**
     * Registers this feature as one using specified placeholders
     *
     * @param   placeholders
     *          placeholders to add as used in this feature
     */
    public void addUsedPlaceholders(@NotNull Collection<String> placeholders) {
        if (placeholders.isEmpty()) return;
        placeholders.forEach(p -> TAB.getInstance().getPlaceholderManager().addUsedPlaceholder(p, this));
    }

    /**
     * Registers this feature as one using specified placeholder.
     *
     * @param   placeholder
     *          placeholder to add as used in this feature
     */
    public void addUsedPlaceholder(@NotNull String placeholder) {
        TAB.getInstance().getPlaceholderManager().addUsedPlaceholder(placeholder, this);
    }
}
