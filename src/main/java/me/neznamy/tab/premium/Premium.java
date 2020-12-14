package me.neznamy.tab.premium;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.ConfigurationFile;
import me.neznamy.tab.shared.config.YamlConfigurationFile;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

/**
 * Core of the premium version
 */
public class Premium {
	
	public static ConfigurationFile premiumconfig;
	public static boolean alignTabsuffix;
	public static Map<String, Condition> conditions = new HashMap<String, Condition>();

	public static boolean is() {
		return false;
	}

	public static void loadPremiumConfig() throws Exception {
		premiumconfig = new YamlConfigurationFile(Shared.platform.getDataFolder(), "premiumconfig.yml", null);
		alignTabsuffix = premiumconfig.getBoolean("align-tabsuffix-on-the-right", false);
		conditions = new HashMap<String, Condition>();
		for (Object condition : premiumconfig.getConfigurationSection("conditions").keySet()) {
			List<String> conditions = premiumconfig.getStringList("conditions." + condition + ".conditions"); //lol
			String type = premiumconfig.getString("conditions." + condition + ".type");
			String yes = premiumconfig.getString("conditions." + condition + ".true");
			String no = premiumconfig.getString("conditions." + condition + ".false");
			Premium.conditions.put(condition+"", Condition.compile(condition+"", conditions, type, yes, no));
		}
		PlaceholderManager.findAllUsed(premiumconfig.getValues());
	}
}