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
	GROUPS_THEN_PLACEHOLDER_A_TO_Z;
	
	public String getTeamName(ITabPlayer p) {
		String teamName = "";
		String number;
		int value;
		switch(this){
		case GROUPS:
			teamName = Configs.sortedGroups.get(p.getGroup().toLowerCase());
			if (teamName == null) teamName = "";
			break;
		case GROUP_PERMISSIONS:
			for (String localgroup : Configs.sortedGroups.keySet()) {
				if (p.hasPermission("tab.sort." + localgroup)) {
					teamName = Configs.sortedGroups.get(localgroup);
					break;
				}
			}
			break;
		case TABPREFIX_A_TO_Z:
			teamName = p.properties.get("tabprefix").getCurrentRawValue();
			break;
		case PLACEHOLDER_LOW_TO_HIGH:
			teamName = setPlaceholders(Premium.sortingPlaceholder, p);
			Shared.errorManager.parseInteger(teamName, 0, "numeric sorting placeholder");
			while (teamName.length() < 10) teamName = "0" + teamName;
			break;
		case PLACEHOLDER_HIGH_TO_LOW:
			teamName = setPlaceholders(Premium.sortingPlaceholder, p);
			value = Shared.errorManager.parseInteger(teamName, 0, "numeric sorting placeholder");
			teamName = (999999999-value)+"";
			break;
		case PLACEHOLDER_A_TO_Z:
			teamName = setPlaceholders(Premium.sortingPlaceholder, p);
			break;
		case GROUPS_THEN_PLACEHOLDER_HIGH_TO_LOW:
			number = setPlaceholders(Premium.sortingPlaceholder, p);
			value = Shared.errorManager.parseInteger(number, 0, "numeric sorting placeholder");
			number = (999999999-value)+"";
			teamName = getGroupChars(p) + number;
			break;
		case GROUPS_THEN_PLACEHOLDER_LOW_TO_HIGH:
			number = setPlaceholders(Premium.sortingPlaceholder, p);
			value = Shared.errorManager.parseInteger(number, 0, "numeric sorting placeholder");
			number = value+"";
			while (number.length() < 8) number = "0" + number;
			teamName = getGroupChars(p) + number;
			break;
		case GROUPS_THEN_PLACEHOLDER_A_TO_Z:
			teamName = getGroupChars(p) + setPlaceholders(Premium.sortingPlaceholder, p);
			break;
		}
		teamName += p.getName();
		teamName = setPlaceholders(teamName, p);
		if (teamName.length() > 15) {
			teamName = teamName.substring(0, 15);
		}
		for (int i = 1; i <= 255; ++i) {
			String name2 = teamName + (char)i;
			boolean nameUsed = false;
			for (ITabPlayer d : Shared.getPlayers()) {
				if (d.getTeamName() != null && d.getTeamName().equals(name2) && !d.getName().equals(p.getName())) {
					nameUsed = true;
				}
			}
			if (!nameUsed) {
				if (!Premium.caseSensitive) name2 = name2.toLowerCase();
				return name2;
			}
		}
		return "InvalidTeam";
	}
	private String getGroupChars(ITabPlayer p) {
		String group = Configs.sortedGroups.get(p.getGroup().toLowerCase()); // 4 chars
		if (group == null) group = "";
		return group;
	}
	private String setPlaceholders(String s, ITabPlayer p) {
		if (s.contains("%")) {
			for (Placeholder pl : Placeholders.getAllUsed()) {
				if (s.contains(pl.getIdentifier())) s = s.replace(pl.getIdentifier(), pl.getValue(p));
			}
		}
		return s;
	}
}