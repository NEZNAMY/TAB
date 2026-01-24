package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Interface for objects in features that may require a condition to see.
 */
public interface Conditional {

    /**
     * Returns the condition required to see this object, or null if there is none
     *
     * @return  condition required to see this object, or null if there is none
     */
    @Nullable
    Condition getDisplayCondition();

    /**
     * Returns true if condition is null or is met, false otherwise
     *
     * @param   player
     *          player to check for
     * @return  true if condition is null or is met, false otherwise
     */
    default boolean isConditionMet(@NotNull TabPlayer player) {
        Condition condition = getDisplayCondition();
        return condition == null || condition.isMet(player);
    }

    /**
     * Dumps information about this object for a specific player
     *
     * @param   player
     *          player to dump for
     * @param   activeObject
     *          currently active object of this type for the player
     * @return  map of dumped data
     */
    @NotNull
    default Map<String, Object> dump(@NotNull TabPlayer player, @Nullable Object activeObject) {
        Condition displayCondition = getDisplayCondition();
        Map<String, Object> innerMap = new LinkedHashMap<>();
        innerMap.put("display-condition", displayCondition == null ? null : displayCondition.toShortFormat());
        innerMap.put("display-condition with placeholders parsed", displayCondition == null ? null :
                TAB.getInstance().getPlaceholderManager().parsePlaceholders(displayCondition.toShortFormat(), player));
        innerMap.put("display-condition is null or met", displayCondition == null || displayCondition.isMet(player));
        innerMap.put("element is displayed", activeObject == this);
        return innerMap;
    }
}
