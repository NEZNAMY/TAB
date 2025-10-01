package me.neznamy.tab.shared.placeholders.conditions.expression.numbers;

import lombok.NonNull;
import me.neznamy.tab.shared.placeholders.conditions.expression.ConditionalExpression;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * An expression that checks if the left side is greater than or equal to the right side.
 */
public class GreaterThanOrEqual extends NumericExpression {

    /**
     * Constructs a new instance with the specified left and right sides.
     *
     * @param   sides
     *          Array with first value being left side, second value being right side
     */
    public GreaterThanOrEqual(@NotNull String[] sides) {
        super(sides[0], sides[1]);
    }

    @Override
    public boolean isMet(@NonNull TabPlayer p) {
        return getLeftSide(p) >= getRightSide(p);
    }

    @Override
    @NotNull
    public ConditionalExpression invert() {
        return new LessThan(new String[] {leftSide, rightSide});
    }

    @Override
    @NotNull
    public String toShortFormat() {
        return leftSide + ">=" + rightSide;
    }
}
