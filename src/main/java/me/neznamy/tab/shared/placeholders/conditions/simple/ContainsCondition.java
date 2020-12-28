package me.neznamy.tab.shared.placeholders.conditions.simple;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.PlaceholderManager;

public class ContainsCondition extends SimpleCondition {

	public ContainsCondition(String line) {
		String[] arr = line.split("<-");
		String arg = "";
		if (arr.length >= 2) arg = arr[1];
		setSides(arr[0], arg);
	}

	@Override
	public boolean isMet(TabPlayer p) {
		return PlaceholderManager.color(parseLeftSide(p)).contains(PlaceholderManager.color(parseRightSide(p)));
	}
}