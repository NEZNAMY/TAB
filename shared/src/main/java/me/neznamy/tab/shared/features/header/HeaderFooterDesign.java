package me.neznamy.tab.shared.features.header;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * A sub-feature for each individual design.
 */
@RequiredArgsConstructor
@Getter
public class HeaderFooterDesign extends RefreshableFeature {

    private final HeaderFooter feature;
    private final String name;
    private final HeaderFooterConfiguration.HeaderFooterDesignDefinition definition;
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
            feature.addUsedPlaceholder(TabConstants.Placeholder.condition(displayCondition.getName()));
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

    /**
     * Returns true if condition is null or is met, false otherwise
     *
     * @param   p
     *          player to check
     * @return  true if condition is null or is met, false otherwise
     */
    public boolean isConditionMet(@NonNull TabPlayer p) {
        return displayCondition == null || displayCondition.isMet(p);
    }
}
