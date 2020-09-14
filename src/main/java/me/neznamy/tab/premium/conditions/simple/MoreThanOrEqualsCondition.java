package me.neznamy.tab.premium.conditions.simple;

import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * "leftSide>=rightSide" condition where leftSide supports placeholders
 */
public class MoreThanOrEqualsCondition extends SimpleCondition {

	private String leftSide;
	private List<String> leftSidePlaceholders;
	private double rightValue;
	
	public MoreThanOrEqualsCondition(String leftSide, String rightSide) {
		this.leftSide = leftSide;
		leftSidePlaceholders = Placeholders.detectAll(leftSide);
		this.rightValue = Shared.errorManager.parseDouble(rightSide, 0, "right side of MoreThanOrEquals condition");
	}
	
	@Override
	public boolean isMet(TabPlayer p) {
		String leftSide = this.leftSide;
		for (String identifier : leftSidePlaceholders) {
			Placeholder pl = Placeholders.getPlaceholder(identifier);
			if (pl != null) leftSide = pl.set(leftSide, p);
		}
		double leftValue = Shared.errorManager.parseDouble(leftSide, 0, "left side of MoreThanOrEquals condition");
		return leftValue >= rightValue;
	}
	
	public static MoreThanOrEqualsCondition compile(String line) {
		if (line.contains(">=")) {
			String[] arr = line.split(">=");
			return new MoreThanOrEqualsCondition(arr[0], arr[1]);
		} else {
			return null;
		}
	}
}
