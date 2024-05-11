package me.neznamy.tab.shared.features.layout;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Layout pattern for player groups displaying players if they meet a condition
 */
@RequiredArgsConstructor
@Getter
public class GroupPattern {

    /** Name of this pattern */
    @NotNull private final String name;

    /** Condition players must meet to be displayed in this group */
    @Nullable private final Condition condition;

    /** Slots to display players in */
    private final int[] slots;
}
