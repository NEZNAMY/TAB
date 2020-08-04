package me.neznamy.tab.premium;

import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.ConfigurationFile;
import me.neznamy.tab.shared.config.YamlConfigurationFile;

public class Premium {
	
	public static ConfigurationFile premiumconfig;
	public static boolean alignTabsuffix;

	public static boolean is() {
		return false;
	}

	public static void loadPremiumConfig() throws Exception {
		premiumconfig = new YamlConfigurationFile(Shared.platform.getDataFolder(), "premiumconfig.yml", null);
		alignTabsuffix = premiumconfig.getBoolean("allign-tabsuffix-on-the-right", false);
	}
}