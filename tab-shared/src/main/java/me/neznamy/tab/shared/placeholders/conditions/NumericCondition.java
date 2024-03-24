package me.neznamy.tab.shared.placeholders.conditions;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

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

    /**
     * Constructs new instance with given parameters.
     *
     * @param   arr
     *          Array with first value being left side, second value being right side
     * @param   function
     *          Condition function
     */
    public NumericCondition(@NotNull String[] arr, @NotNull BiFunction<Double, Double, Boolean> function) {
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
    public double getLeftSide(@NotNull TabPlayer p) {
        if (leftSideStatic) return leftSideValue;
        String value = parseLeftSide(p);
        if (value.contains(",")) value = value.replace(",", "");
        return parseDouble(leftSide, value, 0, p);
    }

    /**
     * Returns right side of this condition. If it's a static number it is
     * returned, if not, placeholders are replaced and then text parsed and returned.
     *
     * @param   p
     *          player to get right side for
     * @return  parsed right side
     */
    public double getRightSide(@NotNull TabPlayer p) {
        if (rightSideStatic) return rightSideValue;
        String value = parseRightSide(p);
        if (value.contains(",")) value = value.replace(",", "");
        return parseDouble(rightSide, value, 0, p);
    }

    /**
     * Parses double in given string and returns it.
     * Returns second argument if string is not valid and prints a console warn.
     *
     * @param   placeholder
     *          Raw placeholder, used in error message
     * @param   output
     *          string to parse
     * @param   defaultValue
     *          value to return if string is not valid
     * @param   player
     *          Player name used in error message
     * @return  parsed double or {@code defaultValue} if input is invalid
     */
    public double parseDouble(@NotNull String placeholder, @NotNull String output, double defaultValue, TabPlayer player) {
        try {
            return Double.parseDouble(output);
        } catch (NumberFormatException e) {
            TAB.getInstance().getConfigHelper().runtime().invalidNumberForCondition(placeholder, output, player);
            return defaultValue;
        }
    }

    @Override
    public boolean isMet(@NotNull TabPlayer p) {
        return function.apply(getLeftSide(p), getRightSide(p));
    }
}