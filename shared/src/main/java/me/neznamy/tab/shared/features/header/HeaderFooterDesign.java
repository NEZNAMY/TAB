package me.neznamy.tab.shared.features.header;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.types.Conditional;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A sub-feature for each individual design.
 */
@RequiredArgsConstructor
@Getter
public class HeaderFooterDesign extends RefreshableFeature implements CustomThreaded, Conditional {

    @NotNull
    private final HeaderFooter feature;

    @NotNull
    private final String name;

    @NotNull
    private final HeaderFooterConfiguration.HeaderFooterDesignDefinition definition;

    @Nullable
    private final Condition displayCondition;

    /**
     * Constructs new instance with given parameters
     *
     * @param   feature
     *          parent feature
     * @param   name
     *          design name
     * @param   definition
     *          design definition
     */
    public HeaderFooterDesign(@NonNull HeaderFooter feature, @NonNull String name, @NonNull HeaderFooterConfiguration.HeaderFooterDesignDefinition definition) {
        this.feature = feature;
        this.name = name;
        this.definition = definition;
        displayCondition = TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(definition.getDisplayCondition());
        if (displayCondition != null) {
            feature.addUsedPlaceholder(displayCondition.getPlaceholderIdentifier());
        }
    }

    @Override
    @NotNull
    public String getRefreshDisplayName() {
        return "Updating header/footer";
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        if (refreshed.headerFooterData.activeDesign == this) {
            feature.sendHeaderFooter(refreshed);
        }
    }

    @Override
    @NotNull
    public String getFeatureName() {
        return feature.getFeatureName();
    }

    @Override
    @NotNull
    public ThreadExecutor getCustomThread() {
        return feature.getCustomThread();
    }
}
