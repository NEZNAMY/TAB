package me.neznamy.tab.api.util;

public class Preconditions {

    public static void checkNotNull(Object obj, String exceptionMessage){
        if (obj == null) throw new IllegalArgumentException(exceptionMessage);
    }

    public static void checkRange(Number number, Number min, Number max, String variable){
        if (number.doubleValue() < min.doubleValue() || number.doubleValue() > max.doubleValue())
            throw new IllegalArgumentException("Number index out of range (" + min + " - " + max + ") for " + variable);
    }
}
