package me.neznamy.tab.shared.features.sorting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.sorting.types.GroupPermission;
import me.neznamy.tab.shared.features.sorting.types.Groups;
import me.neznamy.tab.shared.features.sorting.types.PlaceholderAtoZ;
import me.neznamy.tab.shared.features.sorting.types.PlaceholderHighToLow;
import me.neznamy.tab.shared.features.sorting.types.PlaceholderLowToHigh;
import me.neznamy.tab.shared.features.sorting.types.PlaceholderZtoA;
import me.neznamy.tab.shared.features.sorting.types.SortingType;

public class Sorting {

	private Map<String, SortingType> types = new HashMap<String, SortingType>();
	public String sortingPlaceholder;
	private boolean caseSensitiveSorting = true;
	public List<SortingType> sorting;
	
	public Sorting() {
		types.put("GROUPS", new Groups(sortingPlaceholder));
		types.put("GROUP_PERMISSIONS", new GroupPermission(sortingPlaceholder));
		if (Configs.premiumconfig != null) {
			sortingPlaceholder = Configs.premiumconfig.getString("sorting-placeholder", "%some_level_maybe?%");
			caseSensitiveSorting = Configs.premiumconfig.getBoolean("case-sentitive-sorting", true);
			types.put("PLACEHOLDER_A_TO_Z", new PlaceholderAtoZ(sortingPlaceholder));
			types.put("PLACEHOLDER_Z_TO_A", new PlaceholderZtoA(sortingPlaceholder));
			types.put("PLACEHOLDER_LOW_TO_HIGH", new PlaceholderLowToHigh(sortingPlaceholder));
			types.put("PLACEHOLDER_HIGH_TO_LOW", new PlaceholderHighToLow(sortingPlaceholder));
			sorting = compile(Configs.premiumconfig.getString("sorting-type", "GROUPS"));
		} else {
			sorting = new ArrayList<SortingType>();
			if (Configs.config.getBoolean("sort-players-by-permissions", false)) {
				sorting.add(types.get("GROUP_PERMISSIONS"));
			} else {
				sorting.add(types.get("GROUPS"));
			}
		}
		
		Shared.cpu.startRepeatingMeasuredTask(1000, "refreshing team names", TabFeature.NAMETAGS, UsageType.REFRESHING_TEAM_NAME, new Runnable() {

			@Override
			public void run() {
				for (TabPlayer p : Shared.getPlayers()) {
					if (!p.isLoaded()) continue;
					String newName = getTeamName(p);
					if (!p.getTeamName().equals(newName)) {
						p.unregisterTeam();
						p.setTeamName(newName);
						p.registerTeam();
					}
				}
			}
		});
	}
	
	private List<SortingType> compile(String string){
		List<SortingType> list = new ArrayList<SortingType>();
		for (String element : string.split("_THEN_")) {
			SortingType type = types.get(element.toUpperCase());
			if (type == null) {
				Shared.errorManager.startupWarn("\"&e" + type + "&c\" is not a valid sorting type element. Valid options are: &e" + types.keySet() + ". &bUsing GROUPS");
			} else {
				list.add(type);
			}
		}
		if (list.isEmpty()) list.add(types.get("GROUPS"));
		return list;
	}
	
	public String getTeamName(TabPlayer p) {
		String teamName = "";
		for (SortingType type : sorting) {
			teamName += type.getChars(p);
		}
		if (teamName.length() > 12) {
			teamName = teamName.substring(0, 12);
		}
		teamName += p.getName();
		if (teamName.length() > 15) {
			teamName = teamName.substring(0, 15);
		}
		return checkTeamName(p, teamName, 65);
	}
	
	private String checkTeamName(TabPlayer p, String currentName, int id) {
		String potentialTeamName = currentName;
		if (!caseSensitiveSorting) potentialTeamName = potentialTeamName.toLowerCase();
		potentialTeamName += (char)id;
		for (TabPlayer all : Shared.getPlayers()) {
			if (all == p) continue;
			if (all.getTeamName() != null && all.getTeamName().equals(potentialTeamName)) {
				return checkTeamName(p, currentName, id+1);
			}
		}
		return potentialTeamName;
	}
	
	public String typesToString() {
		String[] elements = new String[sorting.size()];
		for (int i=0; i<sorting.size(); i++) {
			elements[i] = sorting.get(i).toString();
		}
		return String.join(" then ", elements);
	}
	
	public static LinkedHashMap<String, String> loadSortingList() {
		LinkedHashMap<String, String> sortedGroups = new LinkedHashMap<String, String>();
		int index = 1;
		List<String> configList = Configs.config.getStringList("group-sorting-priority-list", Arrays.asList("Owner", "Admin", "Mod", "Helper", "Builder", "Premium", "Player", "default"));
		int charCount = String.valueOf(configList.size()).length(); //1 char for <10 groups, 2 chars for <100 etc
		for (Object group : configList){
			String sort = index+"";
			while (sort.length() < charCount) { 
				sort = "0" + sort;
			}
			for (String group0 : String.valueOf(group).toLowerCase().split(" ")) {
				sortedGroups.put(group0, sort);
			}
			index++;
		}
		return sortedGroups;
	}
}