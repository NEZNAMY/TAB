package me.neznamy.tab.premium.conditions.simple;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * "leftSide=rightSide" condition where both sides support placeholders
 */
public class EqualsCondition extends SimpleCondition {
	
	public EqualsCondition(String leftSide, String rightSide) {
		super(leftSide, rightSide);
	}
	
	@Override
	public boolean isMet(TabPlayer p) {
		return Placeholders.color(parseLeftSide(p)).equals(Placeholders.color(parseRightSide(p)));
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