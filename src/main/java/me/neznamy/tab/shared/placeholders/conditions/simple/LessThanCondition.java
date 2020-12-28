package me.neznamy.tab.shared.placeholders.conditions.simple;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;

/**
 * "leftSide<rightSide" condition where leftSide supports placeholders
 */
public class LessThanCondition extends SimpleCondition {

	public LessThanCondition(String line) {
		String[] arr = line.split("<");
		setSides(arr[0], arr[1]);
	}
	
	@Override
	public boolean isMet(TabPlayer p) {
		return Shared.errorManager.parseDouble(parseLeftSide(p).replace(",", ""), 0, "left side of < condition") < 
				Shared.errorManager.parseDouble(parseRightSide(p), 0, "right side of < condition");
	}
}