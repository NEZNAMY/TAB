package me.neznamy.tab.premium;

import me.neznamy.tab.shared.ConfigurationFile;

public class Premium {
	
	public static ConfigurationFile premiumconfig;
	public static boolean alignTabsuffix;

	public static boolean is() {
		return false;
	}

	public static void loadPremiumConfig() throws Exception {
		premiumconfig = new ConfigurationFile("premiumconfig.yml", null);
		alignTabsuffix = premiumconfig.getBoolean("allign-tabsuffix-on-the-right", false);
	}
}