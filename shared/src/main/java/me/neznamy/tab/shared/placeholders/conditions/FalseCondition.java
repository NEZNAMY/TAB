package me.neznamy.tab.shared.placeholders.conditions;

import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * Condition that always returns false.
 */
public class FalseCondition extends Condition {

    /** Instance of the class */
    public static final FalseCondition INSTANCE = new FalseCondition();

    private FalseCondition() {
        super("false", Collections.emptyList(), true, "true", "false");
    }

    @Override
    public boolean isMet(@NotNull TabPlayer player) {
        return false;
    }

    @NotNull
    public Condition invert() {
        return TrueCondition.INSTANCE;
    }

    @NotNull
    @Override
    public String toShortFormat() {
        return "false";
    }
}
