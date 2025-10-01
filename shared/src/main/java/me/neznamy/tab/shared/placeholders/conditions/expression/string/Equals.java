package me.neznamy.tab.shared.placeholders.conditions.expression.string;

import lombok.NonNull;
import me.neznamy.tab.shared.placeholders.conditions.expression.ComparatorExpression;
import me.neznamy.tab.shared.placeholders.conditions.expression.ConditionalExpression;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * An expression that checks if the left is equal to the right side.
 */
public class Equals extends ComparatorExpression {

    /**
     * Constructs a new Equals expression with the specified left and right sides.
     *
     * @param   sides
     *          Array with first value being left side, second value being right side
     */
    public Equals(@NotNull String[] sides) {
        super(sides[0], sides[1]);
    }

    @Override
    public boolean isMet(@NonNull TabPlayer p) {
        return parseLeftSide(p).equals(parseRightSide(p));
    }

    @Override
    @NotNull
    public ConditionalExpression invert() {
        return new NotEquals(new String[] {leftSide, rightSide});
    }

    @Override
    @NotNull
    public String toShortFormat() {
        return leftSide + "=" + rightSide;
    }
}
