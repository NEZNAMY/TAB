package me.neznamy.tab.premium;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.shared.ConfigurationFile;

public class Premium {
	
	public static ConfigurationFile premiumconfig;
	public static List<String> dynamicLines = Arrays.asList("belowname", "nametag", "abovename");
	public static Map<String, Object> staticLines = new HashMap<String, Object>();
	public static boolean alignTabsuffix;

	public static boolean is() {
		return false;
	}

	@SuppressWarnings("unchecked")
	public static void loadPremiumConfig() throws Exception {
		premiumconfig = new ConfigurationFile("premiumconfig.yml", null);
		List<String> realList = premiumconfig.getStringList("unlimited-nametag-mode-dynamic-lines", Arrays.asList("abovename", "nametag", "belowname", "another"));
		Premium.dynamicLines = new ArrayList<String>();
		Premium.dynamicLines.addAll(realList);
		Collections.reverse(Premium.dynamicLines);
		staticLines = premiumconfig.getConfigurationSection("unlimited-nametag-mode-static-lines");
		alignTabsuffix = premiumconfig.getBoolean("allign-tabsuffix-on-the-right", false);
	}
}