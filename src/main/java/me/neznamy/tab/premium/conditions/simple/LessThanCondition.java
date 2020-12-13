package me.neznamy.tab.premium.conditions.simple;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;

/**
 * "leftSide<rightSide" condition where leftSide supports placeholders
 */
public class LessThanCondition extends SimpleCondition {

	public LessThanCondition(String leftSide, String rightSide) {
		super(leftSide, rightSide);
	}
	
	@Override
	public boolean isMet(TabPlayer p) {
		return Shared.errorManager.parseDouble(parseLeftSide(p).replace(",", ""), 0, "left side of < condition") < 
				Shared.errorManager.parseDouble(parseRightSide(p), 0, "right side of < condition");
	}
	
	public static LessThanCondition compile(String line) {
		if (line.contains("<")) {
			String[] arr = line.split("<");
			return new LessThanCondition(arr[0], arr[1]);
		} else {
			return null;
		}
	}
}
