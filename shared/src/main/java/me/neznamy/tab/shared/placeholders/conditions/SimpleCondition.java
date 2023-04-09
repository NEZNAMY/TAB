package me.neznamy.tab.shared.placeholders.conditions;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.platform.TabPlayer;

/**
 * An abstract class representing a simple condition
 */
public abstract class SimpleCondition {

    /** Text on the left side of condition */
    protected String leftSide;
    
    /** Placeholders used on the left side */
    private final String[] leftSidePlaceholders;

    /** Text on the right side of condition */
    protected String rightSide;

    /** Placeholders used on the right side */
    private final String[] rightSidePlaceholders;

    public SimpleCondition(String[] arr) {
        leftSide = arr.length < 1 ? "" : arr[0];
        leftSidePlaceholders = TAB.getInstance().getPlaceholderManager().detectPlaceholders(leftSide).toArray(new String[0]);
        rightSide = arr.length < 2 ? "" : arr[1];
        rightSidePlaceholders = TAB.getInstance().getPlaceholderManager().detectPlaceholders(rightSide).toArray(new String[0]);
    }

    /**
     * Replaces placeholders on the left side and return result
     *
     * @param   p
     *          player to replace placeholders for
     * @return  replaced left side
     */
    public String parseLeftSide(TabPlayer p) {
        return parseSide(p, leftSide, leftSidePlaceholders);
    }
    
    /**
     * Replaces placeholders on the right side and return result
     *
     * @param    p
     *           player to replace placeholders for
     * @return   replaced right side
     */
    public String parseRightSide(TabPlayer p) {
        return parseSide(p, rightSide, rightSidePlaceholders);
    }
    
    /**
     * Replaces placeholders in provided value
     *
     * @param   p
     *          player to replace placeholders for
     * @param   value
     *          string to replace placeholders in
     * @param   placeholders
     *          used placeholders
     * @return  replaced string
     */
    public String parseSide(TabPlayer p, String value, String[] placeholders) {
        String result = value;
        for (String identifier : placeholders) {
            result = TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier).set(result, p);
        }
        return result == null ? "null" : EnumChatFormat.color(result);
    }
    
    /**
     * Returns {@code true} if condition is met for player, {@code false} if not
     *
     * @param   p
     *          player to check condition for
     * @return  {@code true} if met, {@code false} if not
     */
    public abstract boolean isMet(TabPlayer p);
}