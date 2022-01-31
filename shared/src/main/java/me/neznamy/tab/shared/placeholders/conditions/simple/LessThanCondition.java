package me.neznamy.tab.shared.placeholders.conditions.simple;

import me.neznamy.tab.api.TabPlayer;

/**
 * "leftSide&lt;rightSide" condition
 */
public class LessThanCondition extends NumericCondition {

	/**
	 * Constructs new instance with given condition line
	 *
	 * @param	line
	 * 			configured condition line
	 */
	public LessThanCondition(String line) {
		String[] arr = line.split("<");
		setSides(arr.length < 1 ? "" : arr[0], arr.length < 2 ? "" : arr[1]);
	}
	
	@Override
	public boolean isMet(TabPlayer p) {
		return getLeftSide(p) < getRightSide(p);
	}
}