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
	public static SortingType sortingType;
	public static String sortingPlaceholder;
	public static boolean caseSensitive;
	public static List<String> dynamicLines = Arrays.asList("belowname", "nametag", "abovename");
	public static Map<String, Double> staticLines = new HashMap<String, Double>();

	public static boolean is() {
		return false;
	}

	@SuppressWarnings("unchecked")
	public static void loadPremiumConfig() throws Exception {
		premiumconfig = new ConfigurationFile("premiumconfig.yml", null);
		String type = premiumconfig.getString("sorting-type", "GROUPS");
		try {
			sortingType = SortingType.valueOf(type.toUpperCase());
		} catch (Throwable e) {
			Shared.errorManager.startupWarn("\"&e" + type + "&c\" is not a valid type of sorting type. Valid options are: &eGROUPS, GROUP_PERMISSIONS, TABPREFIX_A_TO_Z, PLACEHOLDER_LOW_TO_HIGH, PLACEHOLDER_HIGH_TO_LOW, PLACEHOLDER_A_TO_Z, GROUPS_THEN_PLACEHOLDER_HIGH_TO_LOW and GROUPS_THEN_PLACEHOLDER_LOW_TO_HIGH. &bUsing GROUPS");
			sortingType = SortingType.GROUPS;
		}
		sortingPlaceholder = premiumconfig.getString("sorting-placeholder", "%some_level_maybe?%");
		caseSensitive = premiumconfig.getBoolean("case-sentitive-sorting", true);
		List<String> realList = premiumconfig.getStringList("unlimited-nametag-mode-dynamic-lines", Arrays.asList("abovename", "nametag", "belowname", "another"));
		Premium.dynamicLines = new ArrayList<String>();
		Premium.dynamicLines.addAll(realList);
		Collections.reverse(Premium.dynamicLines);
		staticLines = (Map<String, Double>) premiumconfig.get("unlimited-nametag-mode-static-lines");
	}
}
