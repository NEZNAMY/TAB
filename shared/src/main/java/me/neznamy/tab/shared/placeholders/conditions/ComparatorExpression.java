package me.neznamy.tab.shared.placeholders.conditions;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * An abstract class representing a condition that compares 2 values.
 */
@Getter
public class ComparatorExpression extends ConditionalExpression {

    /** Left side of the condition */
    @NotNull
    private final ConditionSide leftSide;

    /** Right side of the condition */
    @NotNull
    private final ConditionSide rightSide;

    /** Operator of the condition */
    @NotNull
    private final Operator operator;

    /**
     * Constructs a new comparator expression with given parameters.
     *
     * @param   leftSide
     *          Left side of the condition
     * @param   rightSide
     *          Right side of the condition
     * @param   operator
     *          Operator of the condition
     */
    public ComparatorExpression(@NonNull String leftSide, @NonNull String rightSide, @NonNull Operator operator) {
        this.leftSide = new ConditionSide(leftSide);
        this.rightSide = new ConditionSide(rightSide);
        this.operator = operator;
    }

    @Override
    public boolean isMet(@NonNull TabPlayer viewer, @NonNull TabPlayer target) {
        return operator.evaluate(leftSide, rightSide, viewer, target);
    }

    @Override
    @NotNull
    public ConditionalExpression invert() {
        return new ComparatorExpression(leftSide.getValue(), rightSide.getValue(), Operator.valueOf(operator.getOpposite()));
    }

    @Override
    @NotNull
    public String toShortFormat() {
        return leftSide.getValue() + operator.getSymbol() + rightSide.getValue();
    }
}
