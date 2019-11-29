package me.neznamy.tab.premium;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.placeholders.Placeholders;

public enum SortingType {
	
	GROUPS, GROUP_PERMISSIONS, TABPREFIX_A_TO_Z, PLACEHOLDER_LOW_TO_HIGH, PLACEHOLDER_HIGH_TO_LOW, PLACEHOLDER_A_TO_Z,
	GROUPS_THEN_PLACEHOLDER_HIGH_TO_LOW, GROUPS_THEN_PLACEHOLDER_LOW_TO_HIGH;
	
	public String getTeamName(ITabPlayer p) {
		String teamName = "";
		String group;
		String number;
		switch(this){
		case GROUPS:
			teamName = Configs.sortedGroups.get(p.getGroup());
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
			teamName = Placeholders.replaceAllPlaceholders(Premium.sortingPlaceholder, p);
			try {
				Long.parseLong(teamName);
			} catch (Throwable e) {
				Shared.error(null, teamName + " is not a number! Did you forget to download an expansion ?");
			}
			while (teamName.length() < 10) teamName = "0" + teamName;
			break;
		case PLACEHOLDER_HIGH_TO_LOW:
			teamName = Placeholders.replaceAllPlaceholders(Premium.sortingPlaceholder, p);
			try {
				long value = Long.parseLong(teamName);
				teamName = (9999999999L-value)+"";
			} catch (Throwable e) {
				Shared.error(null, teamName + " is not a number! Did you forget to download an expansion ?");
			}
			break;
		case PLACEHOLDER_A_TO_Z:
			teamName = Placeholders.replaceAllPlaceholders(Premium.sortingPlaceholder, p);
			break;
		case GROUPS_THEN_PLACEHOLDER_HIGH_TO_LOW:
			group = Configs.sortedGroups.get(p.getGroup()); // 4 chars
			if (group == null) group = "";
			number = Placeholders.replaceAllPlaceholders(Premium.sortingPlaceholder, p);
			try {
				long value = Long.parseLong(number);
				number = (99999999L-value)+""; // 8 chars
			} catch (Throwable e) {
				Shared.error(null, number + " is not a number! Did you forget to download an expansion ?");
			}
			teamName = group + number;
			break;
		case GROUPS_THEN_PLACEHOLDER_LOW_TO_HIGH:
			group = Configs.sortedGroups.get(p.getGroup()); // 4 chars
			if (group == null) group = "";
			number = Placeholders.replaceAllPlaceholders(Premium.sortingPlaceholder, p);
			try {
				Long.parseLong(number);
			} catch (Throwable e) {
				Shared.error(null, number + " is not a number! Did you forget to download an expansion ?");
			}
			while (number.length() < 8) number = "0" + number;
			teamName = group + number;
			break;
		}
		teamName += p.getName();
		teamName = Placeholders.replaceAllPlaceholders(teamName, p);
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
}