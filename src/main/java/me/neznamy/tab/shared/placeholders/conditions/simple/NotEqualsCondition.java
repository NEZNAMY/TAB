package me.neznamy.tab.shared.placeholders.conditions.simple;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.PlaceholderManager;

/**
 * "leftSide!=rightSide" condition where both sides support placeholders
 */
public class NotEqualsCondition extends SimpleCondition {

	public NotEqualsCondition(String leftSide, String rightSide) {
		super(leftSide, rightSide);
	}
	
	@Override
	public boolean isMet(TabPlayer p) {
		return !PlaceholderManager.color(parseLeftSide(p)).equals(PlaceholderManager.color(parseRightSide(p)));
	}
	
	public static NotEqualsCondition compile(String line) {
		if (line.contains("!=")) {
			String[] arr = line.split("!=");
			String arg = "";
			if (arr.length >= 2)
				arg = arr[1];
			return new NotEqualsCondition(arr[0], arg);
		} else {
			return null;
		}
	}
}