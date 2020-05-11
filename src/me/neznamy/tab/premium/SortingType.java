package me.neznamy.tab.premium;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.Placeholders;

public enum SortingType {
	
	GROUPS, GROUP_PERMISSIONS, 
	TABPREFIX_A_TO_Z, 
	PLACEHOLDER_LOW_TO_HIGH, PLACEHOLDER_HIGH_TO_LOW, PLACEHOLDER_A_TO_Z,
	GROUPS_THEN_PLACEHOLDER_HIGH_TO_LOW, GROUPS_THEN_PLACEHOLDER_LOW_TO_HIGH, 
	GROUPS_THEN_PLACEHOLDER_A_TO_Z,
	GROUP_PERMISSIONS_THEN_PLACEHOLDER_HIGH_TO_LOW, GROUP_PERMISSIONS_THEN_PLACEHOLDER_LOW_TO_HIGH, 
	GROUP_PERMISSIONS_THEN_PLACEHOLDER_A_TO_Z;
	
	private static final int DEFAULT_NUMBER = 5000000;
	
	public String getTeamName(ITabPlayer p) {
		String teamName = null;
		int intValue;
		switch(this){
		case GROUPS:
			teamName = getGroupChars(p.getGroup());
			break;
		case GROUP_PERMISSIONS:
			teamName = getGroupPermissionChars(p);
			break;
		case TABPREFIX_A_TO_Z:
			teamName = p.properties.get("tabprefix").get();
			break;
		case PLACEHOLDER_LOW_TO_HIGH:
			intValue = Shared.errorManager.parseInteger(setPlaceholders(Premium.sortingPlaceholder, p), 0, "numeric sorting placeholder");
			teamName = String.valueOf(DEFAULT_NUMBER + intValue);
			break;
		case PLACEHOLDER_HIGH_TO_LOW:
			intValue = Shared.errorManager.parseInteger(setPlaceholders(Premium.sortingPlaceholder, p), 0, "numeric sorting placeholder");
			teamName = String.valueOf(DEFAULT_NUMBER - intValue);
			break;
		case PLACEHOLDER_A_TO_Z:
			teamName = setPlaceholders(Premium.sortingPlaceholder, p);
			break;
		case GROUPS_THEN_PLACEHOLDER_LOW_TO_HIGH:
			intValue = Shared.errorManager.parseInteger(setPlaceholders(Premium.sortingPlaceholder, p), 0, "numeric sorting placeholder");
			teamName = getGroupChars(p.getGroup()) + String.valueOf(DEFAULT_NUMBER + intValue);
			break;
		case GROUPS_THEN_PLACEHOLDER_HIGH_TO_LOW:
			intValue = Shared.errorManager.parseInteger(setPlaceholders(Premium.sortingPlaceholder, p), 0, "numeric sorting placeholder");
			teamName = getGroupChars(p.getGroup()) + String.valueOf(DEFAULT_NUMBER - intValue);
			break;
		case GROUPS_THEN_PLACEHOLDER_A_TO_Z:
			teamName = getGroupChars(p.getGroup()) + setPlaceholders(Premium.sortingPlaceholder, p);
			break;
		case GROUP_PERMISSIONS_THEN_PLACEHOLDER_LOW_TO_HIGH:
			intValue = Shared.errorManager.parseInteger(setPlaceholders(Premium.sortingPlaceholder, p), 0, "numeric sorting placeholder");
			teamName = getGroupPermissionChars(p) + String.valueOf(DEFAULT_NUMBER + intValue);
			break;
		case GROUP_PERMISSIONS_THEN_PLACEHOLDER_HIGH_TO_LOW:
			intValue = Shared.errorManager.parseInteger(setPlaceholders(Premium.sortingPlaceholder, p), 0, "numeric sorting placeholder");
			teamName = getGroupPermissionChars(p) + String.valueOf(DEFAULT_NUMBER - intValue);
			break;
		case GROUP_PERMISSIONS_THEN_PLACEHOLDER_A_TO_Z:
			teamName = getGroupPermissionChars(p) + setPlaceholders(Premium.sortingPlaceholder, p);
			break;
		}
		teamName += p.getName();
		if (teamName.length() > 15) {
			teamName = teamName.substring(0, 15);
		}
		main:
		for (int i = 65; i <= 255; i++) {
			String potentialTeamName = teamName;
			if (!Premium.caseSensitive) potentialTeamName = potentialTeamName.toLowerCase() + (char)i;
			for (ITabPlayer all : Shared.getPlayers()) {
				if (all == p) continue;
				if (all.getTeamName().equals(potentialTeamName)) {
					continue main;
				}
			}
			return potentialTeamName;
		}
		return "InvalidTeam";
	}
	public static String getGroupChars(String group) {
		String chars = Configs.sortedGroups.get(group.toLowerCase()); // 4 chars
		if (chars == null) {
			chars = "";
			if (!group.equals("null")) Shared.errorManager.oneTimeConsoleError("Group \"&e" + group + "&c\" is not defined in sorting list! This will result in players in that group not being sorted correctly. To fix this, add group \"&e" + group + "&c\" into &egroup-sorting-priority-list in config.yml&c.");
		}
		return chars;
	}
	public static String getGroupPermissionChars(ITabPlayer p) {
		String chars = null;
		for (String localgroup : Configs.sortedGroups.keySet()) {
			if (p.hasPermission("tab.sort." + localgroup)) {
				chars = getGroupChars(localgroup);
				break;
			}
		}
		if (chars == null) {
			chars = "";
			Shared.errorManager.oneTimeConsoleError("Sorting by permissions is enabled but player " + p.getName() + " does not have any sorting permission. Configure sorting permissions or disable sorting by permissions like it is by default.");
		}
		return chars;
	}
	private String setPlaceholders(String string, ITabPlayer player) {
		if (string.contains("%")) {
			for (Placeholder pl : Placeholders.getAllUsed()) {
				if (string.contains(pl.getIdentifier())) {
					string = pl.set(string, player);
				}
			}
		}
		return string;
	}
}