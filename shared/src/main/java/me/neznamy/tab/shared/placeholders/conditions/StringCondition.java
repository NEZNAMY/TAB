package me.neznamy.tab.shared.placeholders.conditions;

import me.neznamy.tab.shared.platform.TabPlayer;

import java.util.function.BiFunction;

public class StringCondition extends SimpleCondition {

    private final BiFunction<String, String, Boolean> function;

    public StringCondition(String[] arr, BiFunction<String, String, Boolean> function) {
        super(arr);
        this.function = function;
    }

    @Override
    public boolean isMet(TabPlayer p) {
        return function.apply(parseLeftSide(p), parseRightSide(p));
    }
}
