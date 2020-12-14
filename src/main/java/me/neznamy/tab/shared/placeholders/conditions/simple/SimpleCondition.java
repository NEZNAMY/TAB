package me.neznamy.tab.shared.placeholders.conditions.simple;

import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.placeholders.Placeholder;

/**
 * An abstract class representing a simple condition
 */
public abstract class SimpleCondition {
	
	private String leftSide;
	private List<String> leftSidePlaceholders;
	private String rightSide;
	private List<String> rightSidePlaceholders;
	
	public SimpleCondition() {
	}
	
	public SimpleCondition(String leftSide, String rightSide) {
		this.leftSide = leftSide;
		leftSidePlaceholders = PlaceholderManager.detectAll(leftSide);
		this.rightSide = rightSide;
		rightSidePlaceholders = PlaceholderManager.detectAll(rightSide);
	}
	
	public String parseLeftSide(TabPlayer p) {
		return parseSide(p, leftSide, leftSidePlaceholders);
	}
	
	public String parseRightSide(TabPlayer p) {
		return parseSide(p, rightSide, rightSidePlaceholders);
	}
	
	public static String parseSide(TabPlayer p, String value, List<String> placeholders) {
		String result = value;
		for (String identifier : placeholders) {
			Placeholder pl = ((PlaceholderManager) Shared.featureManager.getFeature("placeholders")).getPlaceholder(identifier);
			if (pl != null) result = pl.set(result, p);
		}
		return result;
	}
	
	public abstract boolean isMet(TabPlayer p);
	
	public static SimpleCondition compile(String line) {
		SimpleCondition c = null;
		
		//no idea how to make this more efficient
		if ((c = PermissionCondition.compile(line)) != null) return c;
		if ((c = NotEqualsCondition.compile(line)) != null) return c;
		if ((c = EqualsCondition.compile(line)) != null) return c;
		if ((c = MoreThanOrEqualsCondition.compile(line)) != null) return c;
		if ((c = MoreThanCondition.compile(line)) != null) return c;
		if ((c = LessThanOrEqualsCondition.compile(line)) != null) return c;
		if ((c = LessThanCondition.compile(line)) != null) return c;

		return c;
	}
}