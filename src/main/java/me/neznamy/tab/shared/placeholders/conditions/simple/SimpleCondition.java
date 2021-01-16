package me.neznamy.tab.shared.placeholders.conditions.simple;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.Placeholder;

/**
 * An abstract class representing a simple condition
 */
public abstract class SimpleCondition {
	
	private static LinkedHashMap<String, Class<? extends SimpleCondition>> conditionTypes = new LinkedHashMap<String, Class<? extends SimpleCondition>>();
	
	private String leftSide;
	private List<String> leftSidePlaceholders;
	private String rightSide;
	private List<String> rightSidePlaceholders;
	
	static {
		conditionTypes.put("permission:", PermissionCondition.class);
		conditionTypes.put("!=", NotEqualsCondition.class);
		conditionTypes.put("=", EqualsCondition.class);
		conditionTypes.put("<-", ContainsCondition.class);
		conditionTypes.put(">=", MoreThanOrEqualsCondition.class);
		conditionTypes.put(">", MoreThanCondition.class);
		conditionTypes.put("<=", LessThanOrEqualsCondition.class);
		conditionTypes.put("<", LessThanCondition.class);
	}

	protected void setSides(String leftSide, String rightSide) {
		this.leftSide = leftSide;
		leftSidePlaceholders = TAB.getInstance().getPlaceholderManager().detectAll(leftSide);
		this.rightSide = rightSide;
		rightSidePlaceholders = TAB.getInstance().getPlaceholderManager().detectAll(rightSide);
	}
	
	public String parseLeftSide(TabPlayer p) {
		return parseSide(p, leftSide, leftSidePlaceholders);
	}
	
	public String parseRightSide(TabPlayer p) {
		return parseSide(p, rightSide, rightSidePlaceholders);
	}
	
	public String parseSide(TabPlayer p, String value, List<String> placeholders) {
		String result = value;
		for (String identifier : placeholders) {
			Placeholder pl = TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier);
			if (pl != null) result = pl.set(result, p);
		}
		return result;
	}
	
	public abstract boolean isMet(TabPlayer p);
	
	public static SimpleCondition compile(String line) {
		for (Entry<String, Class<? extends SimpleCondition>> entry : conditionTypes.entrySet()) {
			if (line.contains(entry.getKey())) {
				try {
					SimpleCondition c = (SimpleCondition) entry.getValue().getConstructor(String.class).newInstance(line);
					if (c != null) return c;
				} catch (Exception e) {
					//should never happen
					TAB.getInstance().getErrorManager().printError("Failed to create condition from line \"" + line + "\"", e);
				}
			}
		}
		return null;
	}
}