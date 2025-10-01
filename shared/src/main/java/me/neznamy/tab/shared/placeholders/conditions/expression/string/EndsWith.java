package me.neznamy.tab.shared.placeholders.conditions.expression.string;

import lombok.NonNull;
import me.neznamy.tab.shared.placeholders.conditions.expression.ComparatorExpression;
import me.neznamy.tab.shared.placeholders.conditions.expression.ConditionalExpression;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * An expression that checks if the left side ends with the right side.
 */
public class EndsWith extends ComparatorExpression {

    /**
     * Constructs new instance with the specified left and right sides.
     *
     * @param   sides
     *          Array with first value being left side, second value being right side
     */
    public EndsWith(@NotNull String[] sides) {
        super(sides[0], sides[1]);
    }

    @Override
    public boolean isMet(@NonNull TabPlayer p) {
        return parseLeftSide(p).endsWith(parseRightSide(p));
    }

    @Override
    @NotNull
    public ConditionalExpression invert() {
        return new NotEndsWith(new String[] {leftSide, rightSide});
    }

    @Override
    @NotNull
    public String toShortFormat() {
        return leftSide + "-|" + rightSide;
    }
}
