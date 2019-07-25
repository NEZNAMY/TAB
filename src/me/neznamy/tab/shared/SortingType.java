package me.neznamy.tab.shared;

public enum SortingType {
	
	GROUPS, GROUP_PERMISSIONS, NICKNAME_ALPHABETICAL, TABPREFIX_ALPHABETICAL, PLACEHOLDER_LOW_TO_HIGH, PLACEHOLDER_HIGH_TO_LOW, PLACEHOLDER_ALPHABETICAL;
	
	public static String placeholder;
	
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
		case NICKNAME_ALPHABETICAL:
			teamName = p.getNickname();
			break;
		case TABPREFIX_ALPHABETICAL:
			teamName = p.getTabPrefix();
			break;
		case PLACEHOLDER_LOW_TO_HIGH:
			teamName = Placeholders.replace(placeholder, p);
			try {
				Integer.parseInt(teamName);
			} catch (Exception e) {
				Shared.error(teamName + " is not a number! Did you forget to download an expansion ?");
			}
			while (teamName.length() < 10) teamName = "0" + teamName;
			break;
		case PLACEHOLDER_HIGH_TO_LOW:
			teamName = Placeholders.replace(placeholder, p);
			try {
				int value = Integer.parseInt(teamName);
				teamName = (9999999999L-value)+"";
			} catch (Exception e) {
				Shared.error(teamName + " is not a number! Did you forget to download an expansion ?");
			}
			break;
		case PLACEHOLDER_ALPHABETICAL:
			teamName = Placeholders.replace(placeholder, p);
		}
		teamName += p.getName();
		teamName = Placeholders.replace(teamName, p);
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
				return name2;
			}
		}
		return teamName;
	}
}