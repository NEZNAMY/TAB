package me.neznamy.tab.shared.util;

import org.jetbrains.annotations.NotNull;

import java.util.stream.IntStream;

/**
 * Class storing methods related to improving performance of functions.
 */
public class PerformanceUtil {

    /** Integer to string array for numbers from 0 to 999 for fast access */
    private static final String[] intToString = IntStream.range(0, 1000).mapToObj(Integer::toString).toArray(String[]::new);

    /**
     * Returns string representation of the number.
     *
     * @param   i
     *          Number to convert to string
     * @return  Converted string
     */
    @NotNull
    public static String toString(int i) {
        if (i >= 0 && i < intToString.length) return intToString[i];
        return Integer.toString(i);
    }
}
