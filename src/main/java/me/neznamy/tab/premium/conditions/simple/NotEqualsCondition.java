package me.neznamy.tab.premium.conditions.simple;

import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * "leftSide!=rightSide" condition where both sides support placeholders
 */
public class NotEqualsCondition extends SimpleCondition {

	private String leftSide;
	private List<String> leftSidePlaceholders;
	private String rightSide;
	private List<String> rightSidePlaceholders;
	
	public NotEqualsCondition(String leftSide, String rightSide) {
		this.leftSide = leftSide;
		leftSidePlaceholders = Placeholders.detectAll(leftSide);
		this.rightSide = rightSide;
		rightSidePlaceholders = Placeholders.detectAll(rightSide);
	}
	
	@Override
	public boolean isMet(TabPlayer p) {
		String leftSide = this.leftSide;
		for (String identifier : leftSidePlaceholders) {
			Placeholder pl = Placeholders.getPlaceholder(identifier);
			if (pl != null) leftSide = pl.set(leftSide, p);
		}
		String rightSide = this.rightSide;
		for (String identifier : rightSidePlaceholders) {
			Placeholder pl = Placeholders.getPlaceholder(identifier);
			if (pl != null) rightSide = pl.set(rightSide, p);
		}
		return !leftSide.equals(rightSide);
	}
	
	public static NotEqualsCondition compile(String line) {
		if (line.contains("!=")) {
			String[] arr = line.split("!=");
			return new NotEqualsCondition(arr[0], arr[1]);
		} else {
			return null;
		}
	}
}