package me.neznamy.tab.api.util;

/**
 * Utility class storing methods working with reflection
 */
public class ReflectionUtils {

    /**
     * Returns {@code true} if class with given full name exists,
     * {@code false} if not.
     *
     * @param   path
     *          Full class path and name
     * @return  {@code true} if exists, {@code false} if not
     */
    public static boolean classExists(String path) {
        try {
            Class.forName(path);
            return true;
        } catch (ClassNotFoundException | NullPointerException e) {
            return false;
        }
    }
}
