package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Class checking if disable-condition of each feature is met or not.
 */
public class DisableChecker extends RefreshableFeature {

    @NotNull
    private final TabFeature feature;

    @Nullable
    private final Condition disableCondition;

    @NotNull
    private final BiConsumer<TabPlayer, Boolean> action;

    @NotNull
    private final Function<TabPlayer, AtomicBoolean> field;

    /**
     * Constructs new instance with given parameters.
     *
     * @param   feature
     *          Feature this condition is for
     * @param   disableCondition
     *          Configured condition for disabling the feature
     * @param   action
     *          Action to take on condition change
     * @param   field
     *          Function that returns field storing disable status
     */
    public DisableChecker(@NotNull TabFeature feature, @Nullable Condition disableCondition,
                          @NotNull BiConsumer<TabPlayer, Boolean> action, @NotNull Function<TabPlayer, AtomicBoolean> field) {
        this.feature = feature;
        this.disableCondition = disableCondition;
        this.action = action;
        this.field = field;
        if (disableCondition != null) addUsedPlaceholder(TabConstants.Placeholder.condition(disableCondition.getName()));
    }

    @NotNull
    @Override
    public String getRefreshDisplayName() {
        return TabConstants.CpuUsageCategory.DISABLE_CONDITION_CHANGE;
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        if (disableCondition == null) return;
        Runnable r = () -> {
            boolean disabledNow = disableCondition.isMet(refreshed);
            AtomicBoolean value = field.apply(refreshed);
            if (disabledNow == value.get()) return; // Condition result did not change, only placeholders inside
            value.set(disabledNow);
            action.accept(refreshed, disabledNow);
        };
        if (feature instanceof CustomThreaded) {
            ((CustomThreaded) feature).getCustomThread().execute(new TimedCaughtTask(TAB.getInstance().getCpu(), r,
                    feature.getFeatureName(), TabConstants.CpuUsageCategory.DISABLE_CONDITION_CHANGE));
        } else {
            r.run();
        }
    }

    /**
     * Returns {@code true} if disable condition is {@code null} or met,
     * {@code false} if not met.
     *
     * @param   p
     *          Player to check condition for
     * @return  {@code true} if met, {@code false} if not
     */
    public boolean isDisableConditionMet(TabPlayer p) {
        return disableCondition != null && disableCondition.isMet(p);
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return feature.getFeatureName();
    }
}