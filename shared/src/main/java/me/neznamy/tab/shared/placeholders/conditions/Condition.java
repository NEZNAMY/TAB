package me.neznamy.tab.shared.placeholders.conditions;

import java.util.*;
import java.util.function.Function;

import com.google.common.collect.Lists;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
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
    private static final Map<String, Function<String, SimpleCondition>> conditionTypes =
            new LinkedHashMap<String, Function<String, SimpleCondition>>(){{
        put("permission:", PermissionCondition::new);
        put("<-", ContainsCondition::new);
        put(">=", MoreThanOrEqualsCondition::new);
        put(">", MoreThanCondition::new);
        put("<=", LessThanOrEqualsCondition::new);
        put("<", LessThanCondition::new);
        put("!=", NotEqualsCondition::new);
        put("=", EqualsCondition::new);
    }};

    /** Name of this condition defined in configuration */
    private final String name;

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
    private int refresh = 10000;

    /**
     * Constructs new instance with given parameters and registers
     * this condition to list as well as the placeholder.
     *
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
        //adding placeholders in conditions to the map, so they are actually refreshed if not used anywhere else
        PlaceholderManagerImpl pm = TAB.getInstance().getPlaceholderManager();
        List<String> placeholdersInConditions = new ArrayList<>();
        for (String subCondition : conditions) {
            if (subCondition.startsWith("permission:")) {
                if (refresh > 1000) refresh = 1000; //permission refreshing will be done every second
            } else {
                placeholdersInConditions.addAll(pm.detectPlaceholders(subCondition));
            }
        }
        placeholdersInConditions.addAll(pm.detectPlaceholders(yes));
        placeholdersInConditions.addAll(pm.detectPlaceholders(no));
        for (String placeholder : placeholdersInConditions) {
            Placeholder pl = TAB.getInstance().getPlaceholderManager().getPlaceholder(placeholder);
            if (pl.getRefresh() < refresh) {
                if (pl.getRefresh() == -1) {
                    if (refresh > 500) refresh = 500;
                } else {
                    refresh = pl.getRefresh();
                }
            }
        }
        pm.addUsedPlaceholders(placeholdersInConditions);
        registeredConditions.put(name, this);
    }

    /**
     * Returns refresh interval of placeholder made from this condition
     *
     * @return  refresh interval of placeholder made from this condition
     */
    public int getRefresh() {
        return refresh;
    }

    /**
     * Returns name of this condition
     *
     * @return  name of this condition
     */
    public String getName() {
        return name;
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
            Condition c = new Condition(true, "AnonymousCondition[" + string + "]", Lists.newArrayList(string.split(";")), "true", "false");
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
     * Returns map of all registered condition types
     *
     * @return  all registered condition types
     */
    public static Map<String, Function<String, SimpleCondition>> getConditionTypes() {
        return conditionTypes;
    }
}
