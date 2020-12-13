package me.neznamy.tab.premium.conditions;

import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.premium.conditions.simple.SimpleCondition;

/**
 * A condition consisting of multiple SimpleConditions with AND type
 */
public class ConditionAND extends Condition {

	public ConditionAND(String name, List<String> conditions, String yes, String no) {
		super(name, conditions, yes, no);
	}

	@Override
	public boolean isMet(TabPlayer p) {
		for (SimpleCondition condition : conditions) {
			if (!condition.isMet(p)) return false;
		}
		return true;
	}
}