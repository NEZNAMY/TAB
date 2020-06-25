package me.neznamy.tab.premium;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.shared.ConfigurationFile;
import me.neznamy.tab.shared.Shared;

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
		String type = premiumconfig.getString("sorting-type", "GROUPS");
		try {
			SortingType.INSTANCE = SortingType.valueOf(type.toUpperCase());
		} catch (Throwable e) {
			Shared.errorManager.startupWarn("\"&e" + type + "&c\" is not a valid type of sorting type. Valid options are: &e" + Arrays.deepToString(SortingType.values()) + ". &bUsing GROUPS");
			SortingType.INSTANCE = SortingType.GROUPS;
		}
		SortingType.sortingPlaceholder = premiumconfig.getString("sorting-placeholder", "%some_level_maybe?%");
		SortingType.caseSensitiveSorting = premiumconfig.getBoolean("case-sentitive-sorting", true);
		List<String> realList = premiumconfig.getStringList("unlimited-nametag-mode-dynamic-lines", Arrays.asList("abovename", "nametag", "belowname", "another"));
		Premium.dynamicLines = new ArrayList<String>();
		Premium.dynamicLines.addAll(realList);
		Collections.reverse(Premium.dynamicLines);
		staticLines = premiumconfig.getConfigurationSection("unlimited-nametag-mode-static-lines");
		alignTabsuffix = premiumconfig.getBoolean("allign-tabsuffix-on-the-right", false);
	}
}