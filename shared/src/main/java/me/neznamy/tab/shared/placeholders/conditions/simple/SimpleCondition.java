package me.neznamy.tab.shared.placeholders.conditions.simple;

import java.util.Map.Entry;
import java.util.function.Function;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

/**
 * An abstract class representing a simple condition
 */
public abstract class SimpleCondition {

    /** Text on the left side of condition */
    private String leftSide;
    
    /** Placeholders used on the left side */
    private String[] leftSidePlaceholders;

    /** Text on the right side of condition */
    private String rightSide;

    /** Placeholders used on the right side */
    private String[] rightSidePlaceholders;

    /**
     * Sets raw values of sides and finds used placeholders
     *
     * @param   leftSide
     *          left side of condition
     * @param   rightSide
     *          right side of condition
     */
    protected void setSides(String leftSide, String rightSide) {
        this.leftSide = leftSide;
        leftSidePlaceholders = TAB.getInstance().getPlaceholderManager().detectPlaceholders(leftSide).toArray(new String[0]);
        this.rightSide = rightSide;
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
    
    /**
     * Compiles condition from condition line. This includes detection
     * what kind of condition it is and creating it.
     *
     * @param   line
     *          condition line
     * @return  compiled condition or null if no valid pattern was found
     */
    public static SimpleCondition compile(String line) {
        for (Entry<String, Function<String, SimpleCondition>> entry : Condition.getConditionTypes().entrySet()) {
            if (line.contains(entry.getKey())) {
                return entry.getValue().apply(line);
            }
        }
        return null;
    }
}