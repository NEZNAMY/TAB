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

    /**
     * Checks if the given identifier is a server placeholder.
     *
     * @param   identifier
     *          identifier to check
     * @return  {@code true} if server placeholder, {@code false} otherwise
     */
    public static boolean isServer(@NotNull String identifier) {
        return identifier.startsWith("%server_") || identifier.startsWith("<server_");
    }

    /**
     * Converts the identifier into {@code %placeholder%} syntax.
     *
     * @param   identifier
     *          identifier to convert
     * @return  identifier in percent syntax
     */
    @NotNull
    public static String toPercentSyntax(@NotNull String identifier) {
        if (identifier.startsWith("<") && identifier.endsWith(">")) {
            return "%" + identifier.substring(1, identifier.length() - 1) + "%";
        }
        return identifier;
    }

    /**
     * Converts the identifier into MiniPlaceholders {@code <placeholder>} syntax.
     *
     * @param   identifier
     *          identifier to convert
     * @return  identifier in angle bracket syntax
     */
    @NotNull
    public static String toAngleBracketSyntax(@NotNull String identifier) {
        if (identifier.startsWith("%") && identifier.endsWith("%")) {
            return "<" + identifier.substring(1, identifier.length() - 1) + ">";
        }
        return identifier;
    }
}
