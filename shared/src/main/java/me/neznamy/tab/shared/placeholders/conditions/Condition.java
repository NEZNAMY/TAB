package me.neznamy.tab.shared.placeholders.conditions;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The main condition class. It allows users to configure different
 * condition types that must be met in order to display specified
 * text or make a condition requirement for a visual to be displayed.
 */
public class Condition {

    /** All conditions defined in configuration including anonymous conditions */
    private static Map<String, Condition> registeredConditions = new HashMap<>();

    /** All supported sub-condition types */
    @Getter
    private static final Map<String, Function<String, Function<TabPlayer, Boolean>>> conditionTypes = new LinkedHashMap<>();

    /** Name of this condition defined in configuration */
    @Getter
    private final String name;

    /** All defined sub-conditions inside this conditions */
    protected final List<Function<TabPlayer, Boolean>> subConditions = new ArrayList<>();

    /** Condition type, {@code true} for AND type and {@code false} for OR type */
    private final boolean type;

    /** Text to display if condition passed */
    private final String yes;

    /** Text to display if condition failed */
    private final String no;

    /**
     * Refresh interval of placeholder created from this condition.
     * It is calculated based on nested placeholders used in sub-conditions.
     */
    @Getter private int refresh = -1;

    /** List of all placeholders used inside this condition */
    private final List<String> placeholdersInConditions = new ArrayList<>();

    static {
        conditionTypes.put(">=", line -> new NumericCondition(splitAndTrim(line, ">="), (left, right) -> left >= right)::isMet);
        conditionTypes.put(">", line -> new NumericCondition(splitAndTrim(line, ">"), (left, right) -> left > right)::isMet);
        conditionTypes.put("<=", line -> new NumericCondition(splitAndTrim(line, "<="), (left, right) -> left <= right)::isMet);
        conditionTypes.put("<-", line -> new StringCondition(splitAndTrim(line, "<-"), String::contains)::isMet);
        conditionTypes.put("<", line -> new NumericCondition(splitAndTrim(line, "<"), (left, right) -> left < right)::isMet);
        conditionTypes.put("|-", line -> new StringCondition(splitAndTrim(line, "\\|-"), String::startsWith)::isMet);
        conditionTypes.put("-|", line -> new StringCondition(splitAndTrim(line, "-\\|"), String::endsWith)::isMet);
        conditionTypes.put("!=", line -> new StringCondition(splitAndTrim(line, "!="), (left, right) -> !left.equals(right))::isMet);
        conditionTypes.put("=", line -> new StringCondition(splitAndTrim(line, "="), String::equals)::isMet);
        conditionTypes.put("permission:", line -> {
            String node = splitAndTrim(line, ":")[1];
            return p -> p.hasPermission(node);
        });
    }
    
    @NotNull
    private static String[] splitAndTrim(@NotNull String string, @NonNull String delimiter) {
        return Arrays.stream(string.split(delimiter)).map(String::trim).toArray(String[]::new);
    }

    /**
     * Constructs new instance with given parameters and registers
     * this condition to list as well as the placeholder.
     *
     * @param   type
     *          type of condition, {@code true} for AND type and {@code false} for OR type
     * @param   name
     *          name of condition
     * @param   conditions
     *          list of condition lines
     * @param   yes
     *          value to return if condition is met
     * @param   no
     *          value to return if condition is not met
     */
    public Condition(boolean type, @NonNull String name, @NonNull List<String> conditions, @Nullable String yes, @Nullable String no) {
        this.type = type;
        this.name = name;
        this.yes = yes;
        this.no = no;
        for (String line : conditions) {
            Function<TabPlayer, Boolean> condition = compile(line);
            if (condition != null) {
                subConditions.add(condition);
            } else {
                TAB.getInstance().getConfigHelper().startup().invalidConditionPattern(name, line);
            }
        }
        for (String subCondition : conditions) {
            if (subCondition.startsWith("permission:")) {
                int permissionRefresh = TAB.getInstance().getConfiguration().getConfig().getPermissionRefreshInterval();
                if (refresh > permissionRefresh || refresh == -1) refresh = permissionRefresh;
            } else {
                placeholdersInConditions.addAll(PlaceholderManagerImpl.detectPlaceholders(subCondition));
            }
        }
        if (yes != null) placeholdersInConditions.addAll(PlaceholderManagerImpl.detectPlaceholders(yes));
        if (no != null) placeholdersInConditions.addAll(PlaceholderManagerImpl.detectPlaceholders(no));
        registeredConditions.put(name, this);
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
            for (Function<TabPlayer, Boolean> condition : subConditions) {
                if (!condition.apply(p)) return false;
            }
            return true;
        } else {
            for (Function<TabPlayer, Boolean> condition : subConditions) {
                if (condition.apply(p)) return true;
            }
            return false;
        }
    }

    /**
     * Returns condition from given string. If the string is name of a condition,
     * that condition is returned. If it's a condition pattern, it is compiled and
     * returned. If the string is null, null is returned.
     *
     * @param   string
     *          condition name or pattern
     * @return  condition from string
     */
    public static Condition getCondition(String string) {
        if (string == null || string.isEmpty()) return null;
        if (string.equals("true")) return TrueCondition.INSTANCE;
        if (string.equals("false")) return FalseCondition.INSTANCE;
        String anonVersion = "AnonymousCondition[" + string + "]";
        if (registeredConditions.containsKey(string)) {
            return registeredConditions.get(string);
        } else if (registeredConditions.containsKey(anonVersion)) {
            return registeredConditions.get(anonVersion);
        } else {
            boolean type;
            List<String> conditions;
            if (string.contains(";")) {
                type = true;
                conditions = Arrays.asList(string.split(";"));
            } else {
                type = false;
                conditions = splitString(string);
            }
            conditions = conditions.stream().map(String::trim).collect(Collectors.toList());
            Condition c = new Condition(type, anonVersion, conditions, "true", "false");
            c.finishSetup();
            TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(TabConstants.Placeholder.condition(c.name), c.refresh,
                    p -> c.getText((TabPlayer) p));
            return c;
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

    /**
     * Clears registered condition map on plugin reload
     */
    public static void clearConditions() {
        registeredConditions = new HashMap<>();
    }

    /**
     * Marks all placeholders used in the condition as used and registers them.
     * Using a separate method to avoid premature registration of nested conditional placeholders
     * before they are registered properly.
     */
    public static void finishSetups() {
        for (Condition c : registeredConditions.values()) {
            c.finishSetup();
        }
    }

    /**
     * Compiles condition from condition line. This includes detection
     * what kind of condition it is and creating it.
     *
     * @param   line
     *          condition line
     * @return  compiled condition or null if no valid pattern was found
     */
    private static Function<TabPlayer, Boolean> compile(String line) {
        for (Map.Entry<String, Function<String, Function<TabPlayer, Boolean>>> entry : conditionTypes.entrySet()) {
            if (line.contains(entry.getKey())) {
                return entry.getValue().apply(line);
            }
        }
        return null;
    }
}
