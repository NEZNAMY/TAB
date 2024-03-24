package me.neznamy.tab.shared.placeholders.conditions;

import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

/**
 * Condition class for conditions that use String operations.
 */
public class StringCondition extends SimpleCondition {

    /** Condition function */
    @NotNull
    private final BiFunction<String, String, Boolean> function;

    /**
     * Constructs new instance with given parameters.
     *
     * @param   arr
     *          Array with first value being left side, second value being right side
     * @param   function
     *          Condition function
     */
    public StringCondition(@NotNull String[] arr, @NotNull BiFunction<String, String, Boolean> function) {
        super(arr);
        this.function = function;
    }

    @Override
    public boolean isMet(@NotNull TabPlayer p) {
        return function.apply(parseLeftSide(p), parseRightSide(p));
    }
}
