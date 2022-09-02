package me.neznamy.tab.api.util;

import me.neznamy.tab.api.TabPlayer;

/**
 * Class with utility methods used in the plugin
 */
public class Preconditions {

    /**
     * Private constructor preventing the class from being instantiated
     */
    private Preconditions() {}

    /**
     * Checks object and throws {@code IllegalArgumentException} if it is {@code null}.
     *
     * @param   obj
     *          Object to check
     * @param   name
     *          Name of the variable to use in exception message
     * @throws  IllegalArgumentException
     *          if {@code obj} is {@code null}
     */
    public static void checkNotNull(Object obj, String name) {
        if (obj == null) throw new IllegalArgumentException(name + " cannot be null");
    }

    /**
     * Checks length of String and throws {@code IllegalArgumentException} if it is
     * longer than limit.
     *
     * @param   string
     *          String to check
     * @param   maxLength
     *          Maximum length of the string
     * @param   name
     *          Name of the variable to use in exception message
     * @throws  IllegalArgumentException
     *          if {@code string} is {@code null} or longer than {@code maxLength} characters
     */
    public static void checkMaxLength(String string, int maxLength, String name) {
        checkNotNull(string, name);
        if (string.length() > maxLength) throw new IllegalArgumentException(name + " is longer than " + maxLength + " characters (" + string.length() + ")");
    }

    /**
     * Checks number range and throws {@code IllegalArgumentException} if number
     * is out of range.
     *
     * @param   number
     *          Number to check
     * @param   min
     *          Minimum allowed value
     * @param   max
     *          Maximum allowed value
     * @param   variable
     *          Variable name to use in exception message
     * @throws  IllegalArgumentException
     *          if {@code number} is out of range ({@code number < min || number > max})
     */
    public static void checkRange(Number number, Number min, Number max, String variable) {
        if (number.doubleValue() < min.doubleValue() || number.doubleValue() > max.doubleValue())
            throw new IllegalArgumentException(variable + " index out of range (" + min + " - " + max + "), was " + number);
    }

    /**
     * Checks if player is loaded and throws {@code IllegalStateException} if not.
     *
     * @param   player
     *          Player to check
     * @throws  IllegalStateException
     *          If player is not loaded
     */
    public static void checkLoaded(TabPlayer player) {
        if (!player.isLoaded()) throw new IllegalStateException("Player is not loaded yet. Try again later.");
    }
}
