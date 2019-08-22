package me.neznamy.tab.premium;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Placeholders;
import me.neznamy.tab.shared.Shared;

public enum SortingType {
	
	GROUPS, GROUP_PERMISSIONS, TABPREFIX_A_TO_Z, PLACEHOLDER_LOW_TO_HIGH, PLACEHOLDER_HIGH_TO_LOW, PLACEHOLDER_A_TO_Z;
	
	public String getTeamName(ITabPlayer p) {
		String teamName = "";
		switch(this){
		case GROUPS:
			for (String group : Configs.sortedGroups.keySet()) {
				if (group.equalsIgnoreCase(p.getGroup())) {
					teamName = Configs.sortedGroups.get(group);
					break;
				}
			}
			break;
		case GROUP_PERMISSIONS:
			for (String group : Configs.sortedGroups.keySet()) {
				if (p.hasPermission("tab.sort." + group)) {
					teamName = Configs.sortedGroups.get(group);
					break;
				}
			}
			break;
		case TABPREFIX_A_TO_Z:
			teamName = p.getProperty("tabprefix").getRaw();
			break;
		case PLACEHOLDER_LOW_TO_HIGH:
			teamName = Placeholders.replaceAllPlaceholders(Premium.sortingPlaceholder, p);
			try {
				Long.parseLong(teamName);
			} catch (Throwable e) {
				Shared.error(teamName + " is not a number! Did you forget to download an expansion ?");
			}
			while (teamName.length() < 10) teamName = "0" + teamName;
			break;
		case PLACEHOLDER_HIGH_TO_LOW:
			teamName = Placeholders.replaceAllPlaceholders(Premium.sortingPlaceholder, p);
			try {
				long value = Long.parseLong(teamName);
				teamName = (9999999999L-value)+"";
			} catch (Throwable e) {
				Shared.error(teamName + " is not a number! Did you forget to download an expansion ?");
			}
			break;
		case PLACEHOLDER_A_TO_Z:
			teamName = Placeholders.replaceAllPlaceholders(Premium.sortingPlaceholder, p);
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