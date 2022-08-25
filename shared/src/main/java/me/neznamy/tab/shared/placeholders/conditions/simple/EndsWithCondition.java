package me.neznamy.tab.shared.placeholders.conditions.simple;

import me.neznamy.tab.api.TabPlayer;

/**
 * Condition for "endswith" type using "-|"
 */
public class EndsWithCondition extends SimpleCondition {

    /**
     * Constructs new instance with given condition line
     *
     * @param   line
     *          configured condition line
     */
    public EndsWithCondition(String line) {
        String[] arr = line.split("-\\|");
        setSides(arr.length < 1 ? "" : arr[0], arr.length < 2 ? "" : arr[1]);
    }

    @Override
    public boolean isMet(TabPlayer p) {
        return parseLeftSide(p).endsWith(parseRightSide(p));
    }
}