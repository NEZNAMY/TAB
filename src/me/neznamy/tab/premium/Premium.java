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
	public static SortingType sortingType;
	public static String sortingPlaceholder;
	public static boolean caseSensitive;
	public static List<? extends Object> dynamicLines = Lists.newArrayList("belowname", "nametag", "abovename");
	public static Map<String, Double> staticLines = Maps.newConcurrentMap();

	public static boolean is() {
		return true;
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

		ScoreboardManager.enabled = premiumconfig.getBoolean("scoreboard.enabled", false);
		ScoreboardManager.toggleCommand = premiumconfig.getString("scoreboard.toggle-command", "/sb");
		ScoreboardManager.disabledWorlds = premiumconfig.getList("scoreboard.disable-in-worlds", Lists.newArrayList("disabledworld"));
		ScoreboardManager.defaultScoreboard = premiumconfig.getString("scoreboard.default-scoreboard", "MyDefaultScoreboard");
		ScoreboardManager.refresh = premiumconfig.getInt("scoreboard.refresh-interval-ticks", 1);
		ScoreboardManager.perWorld = (Map<String, String>) premiumconfig.get("scoreboard.per-world");
		ScoreboardManager.scoreboard_on = premiumconfig.getString("scoreboard-on", "&2Scorebord enabled").replace("&", "§");
		ScoreboardManager.scoreboard_off = premiumconfig.getString("scoreboard-off", "&7Scoreboard disabled").replace("&", "§");
		if (premiumconfig.get("scoreboards")!= null) 
			for (String scoreboard : ((Map<String, Object>)premiumconfig.get("scoreboards")).keySet()) {
				String title = premiumconfig.getString("scoreboards." + scoreboard + ".title");
				List<String> lines = premiumconfig.getStringList("scoreboards." + scoreboard + ".lines");
				ScoreboardManager.scoreboards.put(scoreboard, new Scoreboard(title, lines));
			}
	}
}
