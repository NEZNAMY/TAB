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
		String arg = "";
		if (arr.length >= 2) arg = arr[1];
		setSides(arr[0], arg);
	}

	@Override
	public boolean isMet(TabPlayer p) {
		return TAB.getInstance().getPlaceholderManager().color(parseLeftSide(p)).contains(TAB.getInstance().getPlaceholderManager().color(parseRightSide(p)));
	}
}