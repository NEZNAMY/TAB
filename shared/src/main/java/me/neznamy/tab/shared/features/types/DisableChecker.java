package me.neznamy.tab.shared.features.types;

import lombok.Getter;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;

public class DisableChecker extends TabFeature implements Refreshable {

    @Getter private final String featureName;
    @Getter private final String refreshDisplayName = "Refreshing disable condition";
    private final @Nullable Condition disableCondition;
    private final BiConsumer<TabPlayer, Boolean> action;

    /** Players with the feature currently disabled */
    private final Set<TabPlayer> disabledPlayers = Collections.newSetFromMap(new WeakHashMap<>());

    public DisableChecker(@NotNull String featureName, @Nullable Condition disableCondition, @NotNull BiConsumer<TabPlayer, Boolean> action) {
        this.featureName = featureName;
        this.disableCondition = disableCondition;
        this.action = action;
        if (disableCondition != null) addUsedPlaceholders(Collections.singletonList(TabConstants.Placeholder.condition(disableCondition.getName())));
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        if (disableCondition == null) return;
        boolean disabledNow = disableCondition.isMet(refreshed);
        if (disabledNow == disabledPlayers.contains(refreshed)) return; // Condition result did not change, only placeholders inside
        if (disabledNow) {
            disabledPlayers.add(refreshed);
        } else {
            disabledPlayers.remove(refreshed);
        }
        action.accept(refreshed, disabledNow);
    }

    /**
     * Returns {@code true} if the feature is disabled for specified player, {@code false} if not
     *
     * @param   p
     *          Player to check
     * @return  {@code true} if player is disabled,
     *          {@code false} if not
     */
    public boolean isDisabledPlayer(@NotNull TabPlayer p) {
        return disabledPlayers.contains(p);
    }

    /**
     * Adds specified player into set of disabled players
     *
     * @param   p
     *          Player to add
     */
    public void addDisabledPlayer(@NotNull TabPlayer p) {
        disabledPlayers.add(p);
    }

    public boolean isDisableConditionMet(TabPlayer p) {
        return disableCondition != null && disableCondition.isMet(p);
    }
}