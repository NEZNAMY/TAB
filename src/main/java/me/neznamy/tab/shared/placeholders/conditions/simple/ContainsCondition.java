package me.neznamy.tab.shared.placeholders.conditions.simple;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

/**
 * Condition for "contains" type using "<-"
 */
public class ContainsCondition extends SimpleCondition {

	/**
	 * Constructs new instance with given condition line
	 * @param line - condition line
	 */
	public ContainsCondition(String line) {
		String[] arr = line.split("<-");
		setSides(arr.length < 1 ? "" : arr[0], arr.length < 2 ? "" : arr[1]);
	}

	@Override
	public boolean isMet(TabPlayer p) {
		return TAB.getInstance().getPlaceholderManager().color(parseLeftSide(p)).contains(TAB.getInstance().getPlaceholderManager().color(parseRightSide(p)));
	}
}