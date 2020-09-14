package me.neznamy.tab.premium.conditions;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.premium.conditions.simple.SimpleCondition;
import me.neznamy.tab.shared.Shared;

/**
 * The main condition class
 */
public abstract class Condition {

	private String name;
	protected List<SimpleCondition> conditions = new ArrayList<SimpleCondition>();
	public String yes;
	public String no;

	public Condition(String name, List<String> conditions, String yes, String no) {
		this.name = name;
		for (String line : conditions) {
			SimpleCondition condition = SimpleCondition.compile(line);
			if (condition != null) {
				this.conditions.add(condition);
			} else {
				Shared.errorManager.startupWarn("Invalid condition line: " + line);
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
			if (conditions.size() > 1) Shared.errorManager.startupWarn("Invalid condition type: " + conditionType);
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
}
