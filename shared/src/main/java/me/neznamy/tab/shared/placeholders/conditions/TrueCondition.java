package me.neznamy.tab.shared.placeholders.conditions;

import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * Condition that always returns true.
 */
public class TrueCondition extends Condition {

    /** Instance of the class */
    public static final TrueCondition INSTANCE = new TrueCondition();

    private TrueCondition() {
        super(false, "TrueCondition", Collections.emptyList(), null, null);
    }

    @Override
    public boolean isMet(@NotNull TabPlayer player) {
        return true;
    }
}
