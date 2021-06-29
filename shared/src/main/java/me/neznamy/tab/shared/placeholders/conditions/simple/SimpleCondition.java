package me.neznamy.tab.shared.placeholders.conditions.simple;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.Placeholder;

/**
 * An abstract class representing a simple condition
 */
public abstract class SimpleCondition {
	
	//all known condition types
	private static Map<String, Class<? extends SimpleCondition>> conditionTypes = new LinkedHashMap<>();
	
	//left side of condition
	private String leftSide;
	
	//placeholders used in left side of condition
	private List<String> leftSidePlaceholders;
	
	//ride side of condition
	private String rightSide;
	
	//placeholders used in right side of condition
	private List<String> rightSidePlaceholders;
	
	/**
	 * Registering all condition types
	 */
	static {
		getConditionTypes().put("permission:", PermissionCondition.class);
		getConditionTypes().put("<-", ContainsCondition.class);
		getConditionTypes().put(">=", MoreThanOrEqualsCondition.class);
		getConditionTypes().put(">", MoreThanCondition.class);
		getConditionTypes().put("<=", LessThanOrEqualsCondition.class);
		getConditionTypes().put("<", LessThanCondition.class);
		getConditionTypes().put("!=", NotEqualsCondition.class);
		getConditionTypes().put("=", EqualsCondition.class);
	}

	/**
	 * Sets raw values and finds used placeholders
	 * @param leftSide - left side of condition
	 * @param rightSide - right side of condition
	 */
	protected void setSides(String leftSide, String rightSide) {
		this.leftSide = leftSide;
		leftSidePlaceholders = TAB.getInstance().getPlaceholderManager().detectAll(leftSide);
		this.rightSide = rightSide;
		rightSidePlaceholders = TAB.getInstance().getPlaceholderManager().detectAll(rightSide);
	}
	
	/**
	 * Replaces placeholders on the left side and return result
	 * @param p - player to replace placeholders for
	 * @return replaced left side
	 */
	public String parseLeftSide(TabPlayer p) {
		return parseSide(p, leftSide, leftSidePlaceholders);
	}
	
	/**
	 * Replaces placeholders on the right side and return result
	 * @param p - player to replace placeholders for
	 * @return replaced right side
	 */
	public String parseRightSide(TabPlayer p) {
		return parseSide(p, rightSide, rightSidePlaceholders);
	}
	
	/**
	 * Replaces placeholders in provided value
	 * @param p - player to replace placeholders for
	 * @param value - string to replaceplaceholders in
	 * @param placeholders - used placeholders
	 * @return replaced string
	 */
	public String parseSide(TabPlayer p, String value, List<String> placeholders) {
		String result = value;
		for (String identifier : placeholders) {
			Placeholder pl = TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier);
			if (pl != null) result = pl.set(result, p);
		}
		if (result.contains("&")) result = result.replace('&', '\u00a7');
		return result;
	}
	
	/**
	 * Returns true if condition is met for player, false if not
	 * @param p - player to check condition for
	 * @return true if met, false if not
	 */
	public abstract boolean isMet(TabPlayer p);
	
	/**
	 * Compiles condition from condition line
	 * @param line - condition line
	 * @return compiled condition
	 */
	public static SimpleCondition compile(String line) {
		for (Entry<String, Class<? extends SimpleCondition>> entry : getConditionTypes().entrySet()) {
			if (line.contains(entry.getKey())) {
				try {
					SimpleCondition c = entry.getValue().getConstructor(String.class).newInstance(line);
					if (c != null) return c;
				} catch (Exception e) {
					//should never happen
					TAB.getInstance().getErrorManager().printError("Failed to create condition from line \"" + line + "\"", e);
				}
			}
		}
		return null;
	}

	public static Map<String, Class<? extends SimpleCondition>> getConditionTypes() {
		return conditionTypes;
	}
}