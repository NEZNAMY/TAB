package me.neznamy.tab.shared.placeholders.conditions;

import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.placeholders.conditions.simple.SimpleCondition;

/**
 * A condition consisting of multiple SimpleConditions with OR type
 */
public class ConditionOR extends Condition {

	public ConditionOR(String name, List<String> conditions, String yes, String no) {
		super(name, conditions, yes, no);
	}

	@Override
	public boolean isMet(TabPlayer p) {
		for (SimpleCondition condition : subconditions) {
			if (condition.isMet(p)) return true;
		}
		return false;
	}
}