package me.neznamy.tab.premium.conditions.simple;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.PlaceholderManager;

/**
 * "leftSide=rightSide" condition where both sides support placeholders
 */
public class EqualsCondition extends SimpleCondition {
	
	public EqualsCondition(String leftSide, String rightSide) {
		super(leftSide, rightSide);
	}
	
	@Override
	public boolean isMet(TabPlayer p) {
		return PlaceholderManager.color(parseLeftSide(p)).equals(PlaceholderManager.color(parseRightSide(p)));
	}
	
	public static EqualsCondition compile(String line) {
		if (line.contains("=")) {
			String[] arr = line.split("=");
			String arg = "";
			if (arr.length >= 2)
				arg = arr[1];
			return new EqualsCondition(arr[0], arg);
		} else {
			return null;
		}
	}
}