package me.neznamy.tab.shared.placeholders.conditions.simple;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
/**
 * "leftSide<=rightSide" condition where leftSide supports placeholders
 */
public class LessThanOrEqualsCondition extends SimpleCondition {

	public LessThanOrEqualsCondition(String line) {
		String[] arr = line.split("<=");
		setSides(arr[0], arr[1]);
	}
	
	@Override
	public boolean isMet(TabPlayer p) {
		return TAB.getInstance().getErrorManager().parseDouble(parseLeftSide(p).replace(",", ""), 0, "left side of <= condition") <= 
				TAB.getInstance().getErrorManager().parseDouble(parseRightSide(p), 0, "right side of <= condition");
	}
}