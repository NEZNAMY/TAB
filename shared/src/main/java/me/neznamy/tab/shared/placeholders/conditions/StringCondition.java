package me.neznamy.tab.shared.placeholders.conditions;

import lombok.NonNull;
import me.neznamy.tab.shared.platform.TabPlayer;

import java.util.function.BiFunction;

public class StringCondition extends SimpleCondition {

    @NonNull private final BiFunction<String, String, Boolean> function;

    public StringCondition(@NonNull String[] arr, @NonNull BiFunction<String, String, Boolean> function) {
        super(arr);
        this.function = function;
    }

    @Override
    public boolean isMet(@NonNull TabPlayer p) {
        return function.apply(parseLeftSide(p), parseRightSide(p));
    }
}
