package me.neznamy.tab.shared.placeholders;

import org.jetbrains.annotations.NotNull;

/**
 * Utility class for working with placeholder identifiers in both
 * {@code %placeholder%} and MiniPlaceholders {@code <placeholder>} syntax.
 */
public final class PlaceholderIdentifier {

    private PlaceholderIdentifier() {
    }

    /**
     * Checks if the given string is a valid placeholder identifier.
     *
     * @param   identifier
     *          identifier to check
     * @return  {@code true} if valid, {@code false} otherwise
     */
    public static boolean isValid(@NotNull String identifier) {
        return (identifier.startsWith("%") && identifier.endsWith("%")) ||
                (identifier.startsWith("<") && identifier.endsWith(">"));
    }

    /**
     * Checks if the given identifier is a relational placeholder.
     *
     * @param   identifier
     *          identifier to check
     * @return  {@code true} if relational, {@code false} otherwise
     */
    public static boolean isRelational(@NotNull String identifier) {
        return identifier.startsWith("%rel_") || identifier.startsWith("<rel_");
    }
}
