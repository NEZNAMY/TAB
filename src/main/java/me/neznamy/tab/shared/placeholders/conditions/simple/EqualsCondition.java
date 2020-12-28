package me.neznamy.tab.shared.placeholders.conditions.simple;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.PlaceholderManager;

/**
 * "leftSide=rightSide" condition where both sides support placeholders
 */
public class EqualsCondition extends SimpleCondition {
	
	public EqualsCondition(String line) {
		String[] arr = line.split("=");
		String arg = "";
		if (arr.length >= 2) arg = arr[1];
		setSides(arr[0], arg);
	}
	
	@Override
	public boolean isMet(TabPlayer p) {
		return PlaceholderManager.color(parseLeftSide(p)).equals(PlaceholderManager.color(parseRightSide(p)));
	}
}