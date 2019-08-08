package me.neznamy.tab.premium;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import me.neznamy.tab.shared.ConfigurationFile;
import me.neznamy.tab.shared.Shared;

public class Premium {

	public static ConfigurationFile premiumconfig;
	public static SortingType sortingType = null;
	public static String sortingPlaceholder;
	public static boolean caseSensitive;
	public static List<? extends Object> dynamicLines = Lists.newArrayList("belowname", "nametag", "abovename");
	public static Map<String, Double> staticLines = Maps.newConcurrentMap();
	
	public static boolean is() {
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public static void loadPremiumConfig() throws Exception {
		premiumconfig = new ConfigurationFile("premiumconfig.yml");
		String type = premiumconfig.getString("sorting-type", "GROUPS");
		try{
			sortingType = SortingType.valueOf(type.toUpperCase());
		} catch (Exception e) {
			Shared.startupWarn("\"§e" + type + "§c\" is not a valid type of sorting type. Valid options are: §eGROUPS, GROUP_PERMISSIONS, TABPREFIX_A_TO_Z, PLACEHOLDER_LOW_TO_HIGH, PLACEHOLDER_HIGH_TO_LOW, PLACEHOLDER_A_TO_Z.");
			sortingType = SortingType.GROUPS;
		}
		sortingPlaceholder = premiumconfig.getString("sorting-placeholder", "%some_level_maybe?%");
		caseSensitive = premiumconfig.getBoolean("case-sentitive-sorting", true);
		dynamicLines = premiumconfig.getList("unlimited-nametag-mode-dynamic-lines", Lists.newArrayList("abovename", "nametag", "belowname", "another"));
		Collections.reverse(dynamicLines);
		staticLines = (Map<String, Double>) premiumconfig.get("unlimited-nametag-mode-static-lines");
	}
}
