package me.neznamy.tab.shared.placeholders.conditions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.placeholders.conditions.simple.SimpleCondition;

/**
 * The main condition class
 */
public class Condition {
	
	//map of all defined conditions in premiumconfig
	private static Map<String, Condition> conditions = new HashMap<>();
	
	//condition type
	private ConditionType type;
	
	//name of this condition
	private String name;
	
	//list of subconditions
	protected SimpleCondition[] subconditions;
	
	//value to return if condition is met
	private String yes;
	
	//value to return if condition is not met
	private String no;

	/**
	 * Constructs new instance with given parameters
	 * @param name - name of condition
	 * @param conditions - list of condition lines
	 * @param yes - value to return if condition is met
	 * @param no - value to return if condition is not met
	 */
	protected Condition(ConditionType type, String name, List<String> conditions, String yes, String no) {
		this.type = type;
		this.name = name;
		this.yes = yes;
		this.no = no;
		if (conditions == null) {
			TAB.getInstance().getErrorManager().startupWarn("Condition \"" + name + "\" is missing \"conditions\" section.");
			return;
		}
		List<SimpleCondition> list = new ArrayList<>();
		for (String line : conditions) {
			SimpleCondition condition = SimpleCondition.compile(line);
			if (condition != null) {
				list.add(condition);
			} else {
				TAB.getInstance().getErrorManager().startupWarn("\"" + line + "\" is not a defined condition nor a condition pattern");
			}
		}
		subconditions = list.toArray(new SimpleCondition[0]);
		//adding placeholders in conditions to the map so they are actually refreshed if not used anywhere else
		PlaceholderManagerImpl pm = TAB.getInstance().getPlaceholderManager();
		List<String> placeholdersInConditions = new ArrayList<>();
		for (String subcondition : conditions) {
			placeholdersInConditions.addAll(pm.detectPlaceholders(subcondition));
		}
		placeholdersInConditions.addAll(pm.detectPlaceholders(yes));
		placeholdersInConditions.addAll(pm.detectPlaceholders(no));
		pm.addUsedPlaceholders(placeholdersInConditions);
	}
	
	/**
	 * Returns name of this condition
	 * @return name of this condition
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns text for player based on if condition is met or not
	 * @param p - player to check condition for
	 * @return yes or no value depending on if condition is met or not
	 */
	public String getText(TabPlayer p) {
		return isMet(p) ? getYes() : getNo();
	}

	/**
	 * Returns true if condition is met for player, false if not
	 * @param p - player to check
	 * @return true if met, false if not
	 */
	public boolean isMet(TabPlayer p) {
		if (type == ConditionType.AND) {
			for (SimpleCondition condition : subconditions) {
				if (!condition.isMet(p)) return false;
			}
			return true;
		} else {
			for (SimpleCondition condition : subconditions) {
				if (condition.isMet(p)) return true;
			}
			return false;
		}
	}

	/**
	 * Compiles condition from given parameters
	 * @param name - name of condition
	 * @param conditions - list of condition lines
	 * @param conditionType - type of condition AND/OR
	 * @param yes - value to return if condition is met
	 * @param no - value to return if condition is not met
	 * @return compiled condition
	 */
	public static Condition compile(String name, List<String> conditions, String conditionType, String yes, String no) {
		ConditionType type;
		try {
			type = ConditionType.valueOf(conditionType);
		} catch (Exception e) {
			type = ConditionType.AND;
			if (conditions.size() > 1) TAB.getInstance().getErrorManager().startupWarn("Invalid condition type: " + conditionType);
		}
		return new Condition(type, name, conditions, yes, no);
	}
	
	/**
	 * Returns condition from given string. If the string is name of a condition, that condition is returned.
	 * If it's a condition pattern, it is compiled and returned. If the string is null, null is returned
	 * @param string - condition name or pattern
	 * @return condition from string
	 */
	public static Condition getCondition(String string) {
		if (string == null) return null;
		if (getConditions().containsKey(string)) {
			return getConditions().get(string);
		} else {
			Condition c = Condition.compile(UUID.randomUUID().toString(), Lists.newArrayList(string.split(";")), "AND", "yes", "no");
			String identifier = "%condition:" + c.getName() + "%";
			int refresh = TAB.getInstance().getConfiguration().getConfig().getInt("placeholderapi-refresh-intervals.default-refresh-interval", 100);
			if (TAB.getInstance().getPlaceholderManager().getPlayerPlaceholderRefreshIntervals().containsKey(identifier)) {
				refresh = TAB.getInstance().getPlaceholderManager().getPlayerPlaceholderRefreshIntervals().get(identifier);
			}
			TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(new PlayerPlaceholder(identifier, refresh) {

				@Override
				public Object get(TabPlayer p) {
					return c.getText(p);
				}
			});
			return c;
			
		}
	}

	public static Map<String, Condition> getConditions() {
		return conditions;
	}

	public static void setConditions(Map<String, Condition> conditions) {
		Condition.conditions = conditions;
	}

	public String getYes() {
		return yes;
	}

	public String getNo() {
		return no;
	}
}
