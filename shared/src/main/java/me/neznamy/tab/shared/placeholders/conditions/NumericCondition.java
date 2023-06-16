package me.neznamy.tab.shared.placeholders.conditions;

import lombok.NonNull;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TAB;

import java.util.function.BiFunction;

/**
 * A class handling numeric conditions to avoid
 * repeated number parsing for static numbers and therefore
 * reduce memory allocations and improve performance.
 */
public class NumericCondition extends SimpleCondition {

    /** {@code true} if left side is a static number, {@code false} if it has placeholders */
    private boolean leftSideStatic;

    /** If left side is static, value is stored here */
    private float leftSideValue;

    /** {@code true} if right side is a static number, {@code false} if it has placeholders */
    private boolean rightSideStatic;

    /** If right side is static, value is stored here */
    private float rightSideValue;

    /** Function that determines whether condition is met or not */
    private final BiFunction<Double, Double, Boolean> function;

    public NumericCondition(@NonNull String[] arr, @NonNull BiFunction<Double, Double, Boolean> function) {
        super(arr);
        this.function = function;
        try {
            leftSideValue = Float.parseFloat(leftSide);
            leftSideStatic = true;
        } catch (NumberFormatException e) {
            //not a valid number
        }
        try {
            rightSideValue = Float.parseFloat(rightSide);
            rightSideStatic = true;
        } catch (NumberFormatException e) {
            //not a valid number
        }
    }

    /**
     * Returns left side of this condition. If it's a static number it is
     * returned, if not, placeholders are replaced and then text parsed and returned.
     *
     * @param   p
     *          player to get left side for
     * @return  parsed left side
     */
    public double getLeftSide(@NonNull TabPlayer p) {
        if (leftSideStatic) return leftSideValue;
        String value = parseLeftSide(p);
        if (value.contains(",")) value = value.replace(",", "");
        return TAB.getInstance().getErrorManager().parseDouble(value, 0);
    }

    /**
     * Returns right side of this condition. If it's a static number it is
     * returned, if not, placeholders are replaced and then text parsed and returned.
     *
     * @param   p
     *          player to get right side for
     * @return  parsed right side
     */
    public double getRightSide(@NonNull TabPlayer p) {
        if (rightSideStatic) return rightSideValue;
        String value = parseRightSide(p);
        if (value.contains(",")) value = value.replace(",", "");
        return TAB.getInstance().getErrorManager().parseDouble(value, 0);
    }

    @Override
    public boolean isMet(@NonNull TabPlayer p) {
        return function.apply(getLeftSide(p), getRightSide(p));
    }
}