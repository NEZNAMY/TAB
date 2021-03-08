package me.neznamy.tab.shared.placeholders.conditions;

import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.placeholders.conditions.simple.SimpleCondition;

/**
 * A condition consisting of multiple SimpleConditions with AND type
 */
public class ConditionAND extends Condition {

	/**
	 * Constructs new instance with given parameters
	 * @param name - name of condition
	 * @param conditions - list of condition lines
	 * @param yes - value to return if all conditions are met
	 * @param no - value to return if all condition are not met
	 */
	public ConditionAND(String name, List<String> conditions, String yes, String no) {
		super(name, conditions, yes, no);
	}

	@Override
	public boolean isMet(TabPlayer p) {
		for (SimpleCondition condition : subconditions) {
			if (!condition.isMet(p)) return false;
		}
		return true;
	}
}