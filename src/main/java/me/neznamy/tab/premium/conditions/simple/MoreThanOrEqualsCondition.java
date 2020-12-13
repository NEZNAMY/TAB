package me.neznamy.tab.premium.conditions.simple;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;

/**
 * "leftSide>=rightSide" condition where leftSide supports placeholders
 */
public class MoreThanOrEqualsCondition extends SimpleCondition {

	public MoreThanOrEqualsCondition(String leftSide, String rightSide) {
		super(leftSide, rightSide);
	}
	
	@Override
	public boolean isMet(TabPlayer p) {
		return Shared.errorManager.parseDouble(parseLeftSide(p).replace(",", ""), 0, "left side of >= condition") >= 
		Shared.errorManager.parseDouble(parseRightSide(p), 0, "right side of >= condition");	
	}
	
	public static MoreThanOrEqualsCondition compile(String line) {
		if (line.contains(">=")) {
			String[] arr = line.split(">=");
			return new MoreThanOrEqualsCondition(arr[0], arr[1]);
		} else {
			return null;
		}
	}
}
