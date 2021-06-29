package me.neznamy.tab.shared.placeholders.conditions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.conditions.simple.SimpleCondition;

/**
 * The main condition class
 */
public abstract class Condition {
	
	//map of all defined conditions in premiumconfig
	private static Map<String, Condition> conditions = new HashMap<>();
	
	//name of this condition
	private String name;
	
	//list of subconditions
	protected List<SimpleCondition> subconditions = new ArrayList<>();
	
	//value to return if condition is met
	private String yes;
	
	//value to return if condition is not met
	private String no;

	/**
	 * Constructs new instance with given parameters
	 * @param name - name of condition
	 * @param conditions - list of condition lines
	 * @param yes - value to return if condition is met
	 * @param no - value to return if condition is not met
	 */
	protected Condition(String name, List<String> conditions, String yes, String no) {
		this.name = name;
		this.yes = yes;
		this.no = no;
		if (conditions == null) {
			TAB.getInstance().getErrorManager().startupWarn("Condition \"" + name + "\" is missing \"conditions\" section.");
		}
		for (String line : conditions) {
			SimpleCondition condition = SimpleCondition.compile(line);
			if (condition != null) {
				this.subconditions.add(condition);
			} else {
				TAB.getInstance().getErrorManager().startupWarn("\"" + line + "\" is not a defined condition nor a condition pattern");
			}
		}
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
	public abstract boolean isMet(TabPlayer p);

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
		} catch (Exception e) {
			type = ConditionType.AND;
			if (conditions.size() > 1) TAB.getInstance().getErrorManager().startupWarn("Invalid condition type: " + conditionType);
		}
		switch(type) {
		case AND:
			return new ConditionAND(name, conditions, yes, no);
		case OR:
			return new ConditionOR(name, conditions, yes, no);
		default: 
			return null;
		}
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
			return Condition.compile(null, Lists.newArrayList(string.split(";")), "AND", null, null);
		}
	}

	public static Map<String, Condition> getConditions() {
		return conditions;
	}

	public static void setConditions(Map<String, Condition> conditions) {
		Condition.conditions = conditions;
	}

	public String getYes() {
		return yes;
	}

	public String getNo() {
		return no;
	}
}
