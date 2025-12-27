package me.neznamy.tab.shared.features.layout.pattern;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

/**
 * Pattern for player groups. It contains: <p>
 * - Condition players must meet to be displayed in this group (optional)<p>
 * - Slots to display players in
 */
@Getter
@RequiredArgsConstructor
public class GroupPattern {

    /** Condition players must meet to be displayed in this group */
    @Nullable
    private final String condition;

    /** Slots to display players in */
    private final int[] slots;
}
