package me.neznamy.tab.shared.placeholders.conditions;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * An abstract class representing a conditional expression.
 */
public abstract class ConditionalExpression {

    /** All supported expressions */
    @Getter
    @NotNull
    private static final Map<String, Function<String, ConditionalExpression>> conditionTypes = new LinkedHashMap<>();

    static {
        registerConditionType(">=", Operator.GREATER_THAN_OR_EQUAL);
        registerConditionType(">", Operator.GREATER_THAN);
        registerConditionType("<=", Operator.LESS_THAN_OR_EQUAL);
        registerConditionType("!<-", Operator.NOT_CONTAINS);
        registerConditionType("!|-", Operator.NOT_STARTS_WITH);
        registerConditionType("!-|", Operator.NOT_ENDS_WITH);
        registerConditionType("<-", Operator.CONTAINS);
        registerConditionType("<", Operator.LESS_THAN);
        registerConditionType("-|", Operator.ENDS_WITH);
        registerConditionType("|-", Operator.STARTS_WITH);
        registerConditionType("!=", Operator.NOT_EQUALS);
        registerConditionType("=", Operator.EQUALS);
        conditionTypes.put("!permission:", line -> {
            String node = splitAndTrim(line, ":")[1];
            return new NotPermission(node);
        });
        conditionTypes.put("permission:", line -> {
            String node = splitAndTrim(line, ":")[1];
            return new Permission(node);
        });
    }

    private static void registerConditionType(@NotNull String key, @NotNull Operator operator) {
        conditionTypes.put(key, line -> {
            String[] args = splitAndTrim(line, key);
            return new ComparatorExpression(args[0], args[1], operator);
        });
    }

    @NotNull
    private static String[] splitAndTrim(@NonNull String string, @NonNull String delimiter) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean insidePercentBlock = false;
        int i = 0;
        int len = string.length();
        int delimiterLength = delimiter.length();

        while (i < len) {
            // Check for '%' toggling
            if (string.charAt(i) == '%') {
                insidePercentBlock = !insidePercentBlock;
                current.append('%');
                i++;
                continue;
            }

            // Check for delimiter match
            if (!insidePercentBlock && i + delimiterLength <= len &&
                    string.regionMatches(i, delimiter, 0, delimiterLength)) {
                // Split here
                result.add(current.toString().trim());
                current.setLength(0);
                i += delimiterLength;
            } else {
                current.append(string.charAt(i));
                i++;
            }
        }

        // Add last part
        result.add(current.toString().trim());

        return result.toArray(new String[0]);
    }

    /**
     * Compiles expression from pattern. This includes detection
     * what kind of condition it is and creating it. If no valid
     * pattern is found, {@code null} is returned.
     *
     * @param   pattern
     *          condition pattern
     * @return  compiled condition or null if no valid pattern was found
     */
    @Nullable
    public static ConditionalExpression compile(@NonNull String pattern) {
        // Avoid wrong condition type detection if placeholder contains a symbol that is used in condition patterns (#1503)
        String noPlaceholders = pattern;
        for (String placeholder : PlaceholderManagerImpl.detectPlaceholders(pattern)) {
            noPlaceholders = noPlaceholders.replace(placeholder, "");
        }
        for (Map.Entry<String, Function<String, ConditionalExpression>> entry : conditionTypes.entrySet()) {
            if (noPlaceholders.contains(entry.getKey())) {
                return entry.getValue().apply(pattern);
            }
        }
        return null;
    }

    /**
     * Checks if the condition is met for the given player.
     *
     * @param   viewer
     *          Viewer player, for relational placeholders and if configured to parse for viewer instead of target
     * @param   target
     *          The player to check the condition for by default
     * @return  {@code true} if the condition is met, {@code false} otherwise
     */
    public abstract boolean isMet(@NonNull TabPlayer viewer, @NonNull TabPlayer target);

    /**
     * Inverts the conditional expression.
     *
     * @return  A new ConditionalExpression that represents the inverted condition
     */
    @NotNull
    public abstract ConditionalExpression invert();

    /**
     * Returns a short format representation of the conditional expression.
     *
     * @return  A string representing the conditional expression in short format
     */
    @NotNull
    public abstract String toShortFormat();

    /**
     * Returns {@code true} if this expression involves relational content,
     * meaning it depends on both viewer and target players.
     *
     * @return  {@code true} if relational content is involved, {@code false} otherwise
     */
    public abstract boolean hasRelationalContent();
}
