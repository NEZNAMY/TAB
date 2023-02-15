package me.neznamy.tab.shared.placeholders.conditions;

import java.util.*;
import java.util.function.Function;

import lombok.Getter;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.placeholders.conditions.simple.*;

/**
 * The main condition class. It allows users to configure different
 * condition types that must be met in order to display specified
 * text or make a condition requirement for a visual to be displayed.
 */
public class Condition {

    /** All conditions defined in configuration including anonymous conditions */
    private static Map<String, Condition> registeredConditions = new HashMap<>();

    /** All supported sub-condition types */
    @Getter private static final Map<String, Function<String, SimpleCondition>> conditionTypes =
            new LinkedHashMap<String, Function<String, SimpleCondition>>() {{
        put("permission:", PermissionCondition::new);
        put("<-", ContainsCondition::new);
        put("|-", StartsWithCondition::new);
        put("-|", EndsWithCondition::new);
        put(">=", MoreThanOrEqualsCondition::new);
        put(">", MoreThanCondition::new);
        put("<=", LessThanOrEqualsCondition::new);
        put("<", LessThanCondition::new);
        put("!=", NotEqualsCondition::new);
        put("=", EqualsCondition::new);
    }};

    /** Name of this condition defined in configuration */
    @Getter private final String name;

    /** All defined sub-conditions inside this conditions */
    protected SimpleCondition[] subConditions;

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
    public Condition(boolean type, String name, List<String> conditions, String yes, String no) {
        this.type = type;
        this.name = name;
        this.yes = yes;
        this.no = no;
        if (conditions == null) {
            TAB.getInstance().getErrorManager().startupWarn("Condition \"" + name + "\" is missing \"conditions\" section.");
            return;
        }
        List<SimpleCondition> list = new ArrayList<>();
        for (String line : conditions) {
            SimpleCondition condition = SimpleCondition.compile(line);
            if (condition != null) {
                list.add(condition);
            } else {
                TAB.getInstance().getErrorManager().startupWarn("\"" + line + "\" is not a defined condition nor a condition pattern");
            }
        }
        subConditions = list.toArray(new SimpleCondition[0]);
        PlaceholderManagerImpl pm = TAB.getInstance().getPlaceholderManager();
        for (String subCondition : conditions) {
            if (subCondition.startsWith("permission:")) {
                if (refresh > 1000 || refresh == -1) refresh = 1000; //permission refreshing will be done every second
            } else {
                placeholdersInConditions.addAll(pm.detectPlaceholders(subCondition));
            }
        }
        placeholdersInConditions.addAll(pm.detectPlaceholders(yes));
        placeholdersInConditions.addAll(pm.detectPlaceholders(no));
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
            for (SimpleCondition condition : subConditions) {
                if (!condition.isMet(p)) return false;
            }
            return true;
        } else {
            for (SimpleCondition condition : subConditions) {
                if (condition.isMet(p)) return true;
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
        if (string == null) return null;
        if (registeredConditions.containsKey(string)) {
            return registeredConditions.get(string);
        } else {
            boolean type;
            List<String> conditions;
            if (string.contains(";")) {
                type = true;
                conditions = Arrays.asList(string.split(";"));
            } else {
                type = false;
                conditions = Arrays.asList(string.split("\\|"));
            }
            Condition c = new Condition(type, "AnonymousCondition[" + string + "]", conditions, "true", "false");
            c.finishSetup();
            TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(TabConstants.Placeholder.condition(c.getName()), c.getRefresh(), c::getText);
            return c;
        }
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
        registeredConditions.values().forEach(Condition::finishSetup);
    }
}
