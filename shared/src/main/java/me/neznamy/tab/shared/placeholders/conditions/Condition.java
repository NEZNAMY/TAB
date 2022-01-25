package me.neznamy.tab.shared.placeholders.conditions;

import java.util.*;
import java.util.function.Function;

import com.google.common.collect.Lists;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.placeholders.conditions.simple.*;

/**
 * The main condition class
 */
public class Condition {
	
	//map of all defined conditions in config
	private static Map<String, Condition> conditions = new HashMap<>();

	//all known condition types
	private static final Map<String, Function<String, SimpleCondition>> conditionTypes = new LinkedHashMap<>();
	
	//condition type
	private final ConditionType type;
	
	//name of this condition
	private final String name;
	
	//list of sub-conditions
	protected SimpleCondition[] subConditions;
	
	//value to return if condition is met
	private final String yes;
	
	//value to return if condition is not met
	private final String no;
	
	private int refresh = 10000;

	static {
		conditionTypes.put("permission:", PermissionCondition::new);
		conditionTypes.put("<-", ContainsCondition::new);
		conditionTypes.put(">=", MoreThanOrEqualsCondition::new);
		conditionTypes.put(">", MoreThanCondition::new);
		conditionTypes.put("<=", LessThanOrEqualsCondition::new);
		conditionTypes.put("<", LessThanCondition::new);
		conditionTypes.put("!=", NotEqualsCondition::new);
		conditionTypes.put("=", EqualsCondition::new);
	}
	
	/**
	 * Constructs new instance with given parameters
	 * @param name - name of condition
	 * @param conditions - list of condition lines
	 * @param yes - value to return if condition is met
	 * @param no - value to return if condition is not met
	 */
	protected Condition(ConditionType type, String name, List<String> conditions, String yes, String no) {
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
	}
	
	public int getRefresh() {
		return refresh;
	}
	
	/**
	 * Returns name of this condition
	 * @return name of this condition
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns text for player based on if condition is met or not
	 * @param p - player to check condition for
	 * @return yes or no value depending on if condition is met or not
	 */
	public String getText(TabPlayer p) {
		return isMet(p) ? getYes() : getNo();
	}

	/**
	 * Returns true if condition is met for player, false if not
	 * @param p - player to check
	 * @return true if met, false if not
	 */
	public boolean isMet(TabPlayer p) {
		if (type == ConditionType.AND) {
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
	 * Compiles condition from given parameters
	 * @param name - name of condition
	 * @param conditions - list of condition lines
	 * @param conditionType - type of condition AND/OR
	 * @param yes - value to return if condition is met
	 * @param no - value to return if condition is not met
	 * @return compiled condition
	 */
	public static Condition compile(String name, List<String> conditions, String conditionType, String yes, String no) {
		ConditionType type;
		try {
			type = ConditionType.valueOf(conditionType);
		} catch (IllegalArgumentException e) {
			type = ConditionType.AND;
			if (conditions.size() > 1) TAB.getInstance().getErrorManager().startupWarn("Invalid condition type: " + conditionType);
		}
		return new Condition(type, name, conditions, yes, no);
	}
	
	/**
	 * Returns condition from given string. If the string is name of a condition, that condition is returned.
	 * If it's a condition pattern, it is compiled and returned. If the string is null, null is returned
	 * @param string - condition name or pattern
	 * @return condition from string
	 */
	public static Condition getCondition(String string) {
		if (string == null) return null;
		if (getConditions().containsKey(string)) {
			return getConditions().get(string);
		} else {
			Condition c = Condition.compile("AnonymousCondition[" + string + "]", Lists.newArrayList(string.split(";")), "AND", "true", "false");
			TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%condition:" + c.getName() + "%", c.getRefresh(), c::getText);
			return c;
		}
	}

	public static Map<String, Condition> getConditions() {
		return conditions;
	}

	public static void setConditions(Map<String, Condition> conditions) {
		Condition.conditions = conditions;
	}
	
	public static Map<String, Function<String, SimpleCondition>> getConditionTypes() {
		return conditionTypes;
	}

	public String getYes() {
		return yes;
	}

	public String getNo() {
		return no;
	}
}
