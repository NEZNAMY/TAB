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
	
	public static Map<String, Condition> conditions = new HashMap<String, Condition>();
	
	private String name;
	protected List<SimpleCondition> subconditions = new ArrayList<SimpleCondition>();
	public String yes;
	public String no;

	public Condition(String name, List<String> conditions, String yes, String no) {
		this.name = name;
		for (String line : conditions) {
			SimpleCondition condition = SimpleCondition.compile(line);
			if (condition != null) {
				this.subconditions.add(condition);
			} else {
				TAB.getInstance().getErrorManager().startupWarn("\"" + line + "\" is not a defined condition nor a condition pattern");
			}
		}
		this.yes = yes;
		this.no = no;
	}
	
	public String getName() {
		return name;
	}

	public String getText(TabPlayer p) {
		return isMet(p) ? yes : no;
	}

	public abstract boolean isMet(TabPlayer p);

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
	
	public static Condition getCondition(String string) {
		if (string == null) return null;
		if (conditions.containsKey(string)) {
			return conditions.get(string);
		} else {
			return Condition.compile(null, Lists.newArrayList(string.split(";")), "AND", null, null);
		}
	}
}
