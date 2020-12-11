package me.neznamy.tab.premium.conditions.simple;

import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * "leftSide=rightSide" condition where both sides support placeholders
 */
public class EqualsCondition extends SimpleCondition {

	private String leftSide;
	private List<String> leftSidePlaceholders;
	private String rightSide;
	private List<String> rightSidePlaceholders;
	
	public EqualsCondition(String leftSide, String rightSide) {
		this.leftSide = leftSide;
		leftSidePlaceholders = PlaceholderManager.detectAll(leftSide);
		this.rightSide = rightSide;
		rightSidePlaceholders = PlaceholderManager.detectAll(rightSide);
	}
	
	@Override
	public boolean isMet(TabPlayer p) {
		String leftSide = this.leftSide;
		for (String identifier : leftSidePlaceholders) {
			Placeholder pl = ((PlaceholderManager) Shared.featureManager.getFeature("placeholders")).getPlaceholder(identifier);
			if (pl != null) leftSide = pl.set(leftSide, p);
		}
		String rightSide = this.rightSide;
		for (String identifier : rightSidePlaceholders) {
			Placeholder pl = ((PlaceholderManager) Shared.featureManager.getFeature("placeholders")).getPlaceholder(identifier);
			if (pl != null) rightSide = pl.set(rightSide, p);
		}
		return Placeholders.color(leftSide).equals(Placeholders.color(rightSide));
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