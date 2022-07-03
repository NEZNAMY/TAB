package me.neznamy.tab.api.util;

import me.neznamy.tab.api.TabPlayer;

public class Preconditions {

    private Preconditions(){}

    public static void checkNotNull(Object obj, String name){
        if (obj == null) throw new IllegalArgumentException(name + " cannot be null");
    }

    public static void checkMaxLength(String string, int maxLength, String name) {
        checkNotNull(string, name);
        if (string.length() > maxLength) throw new IllegalArgumentException(name + " is longer than " + maxLength + " characters (" + string.length() + ")");
    }

    public static void checkRange(Number number, Number min, Number max, String variable){
        if (number.doubleValue() < min.doubleValue() || number.doubleValue() > max.doubleValue())
            throw new IllegalArgumentException(variable + " index out of range (" + min + " - " + max + ")");
    }

    public static void checkLoaded(TabPlayer player) {
        if (!player.isLoaded()) throw new IllegalStateException("Player is not loaded yet. Try again later.");
    }
}
