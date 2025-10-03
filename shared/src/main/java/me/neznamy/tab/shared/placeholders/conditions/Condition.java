package me.neznamy.tab.shared.placeholders.conditions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.placeholders.conditions.expression.ComparatorExpression;
import me.neznamy.tab.shared.placeholders.conditions.expression.ConditionalExpression;
import me.neznamy.tab.shared.placeholders.conditions.expression.NotPermission;
import me.neznamy.tab.shared.placeholders.conditions.expression.Permission;
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
@RequiredArgsConstructor
public class Condition {

    /** Name of this condition defined in configuration */
    @Getter
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
    @Getter
    private int refresh = -1;

    /** List of all placeholders used inside this condition */
    @NotNull
    private final List<String> placeholdersInConditions = new ArrayList<>();

    /**
     * Constructs new instance with given definition and registers
     * this condition to list as well as the placeholder.
     *
     * @param   definition
     *          Condition definition from configuration
     */
    public Condition(@NotNull ConditionsSection.ConditionDefinition definition) {
        type = definition.isType();
        name = definition.getName();
        expressions = new ArrayList<>(definition.getConditions());
        yes = definition.getYes();
        no = definition.getNo();
        analyzeContent();
    }

    /**
     * Constructs new instance from a condition string in short format.
     * This constructor is used for anonymous conditions only.
     *
     * @param   shortFormat
     *          Condition in short format
     */
    public Condition(@NotNull String shortFormat) {
        name = "AnonymousCondition[" + shortFormat + "]";
        yes = "true";
        no = "false";
        List<String> conditions;
        if (shortFormat.contains(";")) {
            type = true;
            conditions = Arrays.asList(shortFormat.split(";"));
        } else {
            type = false;
            conditions = splitString(shortFormat);
        }
        expressions = conditions.stream().map(expressionString -> {
            ConditionalExpression expression = ConditionalExpression.compile(expressionString.trim());
            if (expression == null) {
                TAB.getInstance().getConfigHelper().startup().startupWarn("Line \"" + expressionString + "\" is not a valid conditional expression.");
            }
            return expression;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        analyzeContent();
    }

    private void analyzeContent() {
        for (ConditionalExpression expression : expressions) {
            if (expression instanceof Permission || expression instanceof NotPermission) {
                int permissionRefresh = TAB.getInstance().getConfiguration().getConfig().getPermissionRefreshInterval();
                if (refresh > permissionRefresh || refresh == -1) refresh = permissionRefresh;
            } else {
                ComparatorExpression comparator = (ComparatorExpression) expression;
                placeholdersInConditions.addAll(Arrays.asList(comparator.getLeftSidePlaceholders()));
                placeholdersInConditions.addAll(Arrays.asList(comparator.getRightSidePlaceholders()));
            }
        }
    }

    /**
     * Splits string using `|` symbol except cases where it is used as |- or -|.
     * This method was 100% made by ChatGPT!
     *
     * @param   input
     *          String to split
     * @return  Split string
     */
    private List<String> splitString(@NotNull String input) {
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

    /**
     * Configures refresh interval and registers nested placeholders
     */
    public void finishSetup() {
        for (String placeholder : placeholdersInConditions) {
            TAB.getInstance().getPlaceholderManager().getPlaceholder(placeholder).addParent(TabConstants.Placeholder.condition(name));
            Placeholder pl = TAB.getInstance().getPlaceholderManager().getPlaceholder(placeholder);
            if (pl.getRefresh() < refresh && pl.getRefresh() != -1) {
                refresh = pl.getRefresh();
            }
        }
        TAB.getInstance().getPlaceholderManager().addUsedPlaceholders(placeholdersInConditions);
    }

    /**
     * Returns text for player based on if condition is met or not
     *
     * @param   p
     *          player to check condition for
     * @return  yes or no value depending on if condition passed or not
     */
    public String getText(TabPlayer p) {
        return isMet(p) ? yes : no;
    }

    /**
     * Returns {@code true} if condition is met for player, {@code false} if not
     *
     * @param   p
     *          player to check conditions for
     * @return  {@code true} if met, {@code false} if not
     */
    public boolean isMet(TabPlayer p) {
        if (type) {
            for (ConditionalExpression condition : expressions) {
                if (!condition.isMet(p)) return false;
            }
            return true;
        } else {
            for (ConditionalExpression condition : expressions) {
                if (condition.isMet(p)) return true;
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
        return new Condition(new ConditionsSection.ConditionDefinition(
                "inverted:" + name,
                expressions.stream().map(ConditionalExpression::invert).collect(Collectors.toList()),
                !type,
                yes,
                no
        ));
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
}
