package me.neznamy.tab.shared.placeholders.conditions.expression;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * An abstract class representing a condition that compares 2 values.
 */
@Getter
public abstract class ComparatorExpression extends ConditionalExpression {

    /** Text on the left side of condition */
    @NotNull protected final String leftSide;

    /** Text on the right side of condition */
    @NotNull protected final String rightSide;

    /** Placeholders used on the left side */
    @NotNull private final String[] leftSidePlaceholders;

    /** Placeholders used on the right side */
    @NotNull private final String[] rightSidePlaceholders;

    protected ComparatorExpression(@NonNull String leftSide, @NonNull String rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
        leftSidePlaceholders = PlaceholderManagerImpl.detectPlaceholders(leftSide).toArray(new String[0]);
        rightSidePlaceholders = PlaceholderManagerImpl.detectPlaceholders(rightSide).toArray(new String[0]);
    }

    /**
     * Replaces placeholders on the left side and return result
     *
     * @param   p
     *          player to replace placeholders for
     * @return  replaced left side
     */
    @NotNull
    public String parseLeftSide(@NotNull TabPlayer p) {
        return parseSide(p, leftSide, leftSidePlaceholders);
    }

    /**
     * Replaces placeholders on the right side and return result
     *
     * @param    p
     *           player to replace placeholders for
     * @return   replaced right side
     */
    @NotNull
    public String parseRightSide(@NotNull TabPlayer p) {
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
    @NotNull
    public String parseSide(@NotNull TabPlayer p, @NotNull String value, @NotNull String[] placeholders) {
        String result = value;
        for (String identifier : placeholders) {
            result = TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier).set(result, p);
        }
        return EnumChatFormat.color(result);
    }
}
