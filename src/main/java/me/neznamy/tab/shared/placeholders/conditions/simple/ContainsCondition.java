package me.neznamy.tab.shared.placeholders.conditions.simple;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.PlaceholderManager;

public class ContainsCondition extends SimpleCondition {

    public ContainsCondition(String leftSide, String rightSide) {
        super(leftSide, rightSide);
    }

    @Override
    public boolean isMet(TabPlayer p) {
        return PlaceholderManager.color(parseLeftSide(p)).contains(PlaceholderManager.color(parseRightSide(p)));
    }

    public static ContainsCondition compile(String line) {
        if (line.contains("<-")) {
            String[] arr = line.split("<-");
            String arg = "";
            if (arr.length >= 2)
                arg = arr[1];
            return new ContainsCondition(arr[0], arg);
        } else {
            return null;
        }
    }
}