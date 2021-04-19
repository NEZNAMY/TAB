package me.neznamy.tab.shared.placeholders.conditions.simple;

import me.neznamy.tab.api.TabPlayer;

/**
 * "leftSide=rightSide" condition
 */
public class EqualsCondition extends SimpleCondition {
	
	/**
	 * Constructs new instance with given condition line
	 * @param line - condition line
	 */
	public EqualsCondition(String line) {
		String[] arr = line.split("=");
		setSides(arr.length < 1 ? "" : arr[0], arr.length < 2 ? "" : arr[1]);
	}
	
	@Override
	public boolean isMet(TabPlayer p) {
		return parseLeftSide(p).equals(parseRightSide(p));
	}
}