package me.neznamy.tab.shared.placeholders.conditions;

import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.placeholders.PlaceholderReference;
import me.neznamy.tab.shared.placeholders.types.RelationalPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.types.TabPlaceholder;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The main condition class. It allows users to configure different
 * condition types that must be met in order to display specified
 * text or make a condition requirement for a visual to be displayed.
 */
@Getter
public class Condition {

    /** Name of this condition defined in configuration */
    @NotNull
    private final String name;

    /** All defined expressions inside this condition */
    @NotNull
    protected final List<ConditionalExpression> expressions;

    /** Condition type, {@code true} for AND type and {@code false} for OR type */
    private final boolean type;

    /** Text to display if condition passed */
    @NotNull
    private final String yes;

    /** Text to display if condition failed */
    @NotNull
    private final String no;

    /**
     * Refresh interval of placeholder created from this condition.
     * It is calculated based on nested placeholders used in sub-conditions.
     */
    private int refresh = -1;

    /**
     * Constructs new instance with given parameters and analyzes content
     * to determine refresh interval.
     *
     * @param   name
     *          Name of this condition defined in configuration
     * @param   expressions
     *          All defined expressions inside this condition
     * @param   type
     *          Condition type, {@code true} for AND type and {@code false} for OR type
     * @param   yes
     *          Text to display if condition passed
     * @param   no
     *          Text to display if condition failed
     * @param   primitive
     *          Whether this is a primitive condition (TrueCondition or FalseCondition with no nested placeholders)
     */
    public Condition(@NotNull String name, @NotNull List<ConditionalExpression> expressions, boolean type, @NotNull String yes, @NotNull String no, boolean primitive) {
        this.name = name;
        this.expressions = expressions;
        this.type = type;
        this.yes = yes;
        this.no = no;

        if (primitive) return;

        List<PlaceholderReference> placeholdersInConditions = new ArrayList<>();
        PlaceholderManagerImpl manager = TAB.getInstance().getPlaceholderManager();
        placeholdersInConditions.addAll(manager.detectPlaceholders(yes).stream().map(manager::getPlaceholderReference).collect(Collectors.toList()));
        placeholdersInConditions.addAll(manager.detectPlaceholders(no).stream().map(manager::getPlaceholderReference).collect(Collectors.toList()));
        for (ConditionalExpression expression : expressions) {
            if (expression instanceof Permission || expression instanceof NotPermission) {
                int permissionRefresh = TAB.getInstance().getConfiguration().getConfig().getPermissionRefreshInterval();
                if (refresh > permissionRefresh || refresh == -1) refresh = permissionRefresh;
            } else {
                ComparatorExpression comparator = (ComparatorExpression) expression;
                placeholdersInConditions.addAll(Arrays.stream(comparator.getLeftSide().getPlaceholders()).map(ConditionPlaceholder::getRealPlaceholder).collect(Collectors.toList()));
                placeholdersInConditions.addAll(Arrays.stream(comparator.getRightSide().getPlaceholders()).map(ConditionPlaceholder::getRealPlaceholder).collect(Collectors.toList()));
            }
        }
        manager.addUsedPlaceholderReferences(placeholdersInConditions);
        String identifier = getPlaceholderIdentifier();
        String relIdentifier = getRelationalPlaceholderIdentifier();
        TabPlaceholder placeholder;
        RelationalPlaceholderImpl relationalPlaceholder;
        if (hasRelationalContent()) {
            relationalPlaceholder = manager.registerRelationalPlaceholder(
                    relIdentifier,
                    refresh,
                    (viewer, target) -> getText((TabPlayer) viewer, (TabPlayer) target)
            );
            placeholder = manager.registerPlayerPlaceholder(
                    identifier,
                    -1,
                    p -> "<This is a relational condition, use " + relIdentifier.substring(1, relIdentifier.length()-1) + ">"
            );
        } else {
            relationalPlaceholder = manager.registerRelationalPlaceholder(
                    relIdentifier,
                    -1,
                    (viewer, target) -> "<This is not a relational condition, use " + identifier.substring(1, identifier.length()-1) + ">"
            );
            placeholder = manager.registerPlayerPlaceholder(
                    identifier,
                    refresh,
                    p -> getText((TabPlayer) p, (TabPlayer) p)
            );
        }
        for (PlaceholderReference reference : placeholdersInConditions) {
            reference.addParent(placeholder.getReference());
            if (hasRelationalContent()) {
                reference.addParent(relationalPlaceholder.getReference());
            }
        }
    }

    /**
     * Returns text for player based on if condition is met or not
     *
     * @param   viewer
     *          Viewer (relational conditions only)
     * @param   target
     *          Target player to check condition for
     * @return  yes or no value depending on if condition passed or not
     */
    @NotNull
    public String getText(@NotNull TabPlayer viewer, @NotNull TabPlayer target) {
        return isMet(viewer, target) ? yes : no;
    }

    /**
     * Returns {@code true} if condition is met for player, {@code false} if not
     *
     * @param   player
     *          Player to check condition for
     * @return  {@code true} if met, {@code false} if not
     */
    public boolean isMet(@NotNull TabPlayer player) {
        return isMet(player, player);
    }

    /**
     * Returns {@code true} if condition is met for player, {@code false} if not
     *
     * @param   viewer
     *          Viewer (relational conditions only)
     * @param   target
     *          Target player to check condition for
     * @return  {@code true} if met, {@code false} if not
     */
    public boolean isMet(@NotNull TabPlayer viewer, @NotNull TabPlayer target) {
        if (type) {
            for (ConditionalExpression condition : expressions) {
                if (!condition.isMet(viewer, target)) return false;
            }
            return true;
        } else {
            for (ConditionalExpression condition : expressions) {
                if (condition.isMet(viewer, target)) return true;
            }
            return false;
        }
    }

    /**
     * Inverts the condition by inverting each individual expression
     * and switching the overall condition type (AND to OR, OR to AND).
     *
     * @return A new Condition instance representing the inverted condition
     */
    @NotNull
    public Condition invert() {
        return new Condition(
                "inverted:" + name,
                expressions.stream().map(ConditionalExpression::invert).collect(Collectors.toList()),
                !type,
                yes,
                no,
                false
        );
    }

    /**
     * Returns a short format representation of the entire condition,
     * combining the short formats of all individual expressions
     * with the appropriate logical operator based on the condition type.
     *
     * @return A string representing the entire condition in short format
     */
    @NotNull
    public String toShortFormat() {
        return expressions.stream().map(ConditionalExpression::toShortFormat).collect(Collectors.joining(type ? ";" : "|"));
    }

    /**
     * Returns the placeholder identifier for this condition.
     *
     * @return The placeholder identifier in the format "%condition:name%"
     */
    @NotNull
    public String getPlaceholderIdentifier() {
        return "%condition:" + name + "%";
    }

    /**
     * Returns the relational placeholder identifier for this condition.
     *
     * @return The relational placeholder identifier in the format "%rel_condition:name%"
     */
    @NotNull
    public String getRelationalPlaceholderIdentifier() {
        return "%rel_condition:" + name + "%";
    }

    /**
     * Checks if this condition contains any relational content.
     *
     * @return {@code true} if the condition has relational content, {@code false} otherwise
     */
    public boolean hasRelationalContent() {
        for (ConditionalExpression expression : expressions) {
            if (expression.hasRelationalContent()) return true;
        }
        return false;
    }

    /**
     * Creates a new condition instance based on definition.
     *
     * @param   definition
     *          Condition definition from configuration
     * @return  A new Condition instance based on the provided definition
     */
    @NotNull
    public static Condition fromDefinition(@NotNull ConditionsSection.ConditionDefinition definition) {
        return new Condition(
                definition.getName(),
                definition.getConditions().stream().map(expressionString -> {
                    ConditionalExpression expression = ConditionalExpression.compile(expressionString.trim());
                    if (expression == null) {
                        TAB.getInstance().getConfigHelper().startup().startupWarn("Line \"" + expressionString + "\" is not a valid conditional expression.");
                    }
                    return expression;
                }).filter(Objects::nonNull).collect(Collectors.toList()),
                definition.isType(),
                definition.getYes(),
                definition.getNo(),
                false
        );
    }

    /**
     * Creates a new condition instance from a short format string.
     *
     * @param   shortFormat
     *          Condition in short format
     * @return  A new Condition instance based on the provided short format string
     */
    @NotNull
    public static Condition fromShortFormat(@NotNull String shortFormat) {
        boolean type;
        List<String> conditions;
        if (shortFormat.contains(";")) {
            type = true;
            conditions = Arrays.asList(shortFormat.split(";"));
        } else {
            type = false;
            conditions = splitString(shortFormat);
        }
        return new Condition(
                "AnonymousCondition[" + shortFormat + "]",
                conditions.stream().map(expressionString -> {
                    ConditionalExpression expression = ConditionalExpression.compile(expressionString.trim());
                    if (expression == null) {
                        TAB.getInstance().getConfigHelper().startup().startupWarn("Line \"" + expressionString + "\" is not a valid conditional expression.");
                    }
                    return expression;
                }).filter(Objects::nonNull).collect(Collectors.toList()),
                type,
                "true",
                "false",
                false
        );
    }
    /**
     * Splits string using `|` symbol except cases where it is used as |- or -|.
     *
     * @param   input
     *          String to split
     * @return  Split string
     */
    @NotNull
    private static List<String> splitString(@NotNull String input) {
        List<String> result = new ArrayList<>();

        // Define a regular expression pattern to match the desired delimiter
        Pattern pattern = Pattern.compile("(?<!-)[|](?!-)");

        // Use a Matcher to split the input string
        Matcher matcher = pattern.matcher(input);
        int start = 0;

        while (matcher.find()) {
            int end = matcher.start();
            result.add(input.substring(start, end));
            start = matcher.end();
        }

        // Add the remaining part of the string
        result.add(input.substring(start));

        return result;
    }

}
