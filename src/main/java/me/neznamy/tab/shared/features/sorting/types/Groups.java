package me.neznamy.tab.shared.features.sorting.types;

import java.util.LinkedHashMap;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

public class Groups extends SortingType {

	private LinkedHashMap<String, String> sortedGroups;
	
	public Groups(String sortingPlaceholder) {
		super(sortingPlaceholder);
		sortedGroups = loadSortingList();
	}

	@Override
	public String getChars(TabPlayer p) {
		String group = p.getGroup();
		String chars = sortedGroups.get(group.toLowerCase());
		if (chars == null) {
			chars = "9";
			if (!group.equals("<null>")) TAB.getInstance().getErrorManager().oneTimeConsoleError("Group \"&e" + group + "&c\" is not defined in sorting list! This will result in players in that group not being sorted correctly. To fix this, add group \"&e" + group + "&c\" into &egroup-sorting-priority-list in config.yml&c. Your current list: " + sortedGroups.keySet());
			p.setTeamNameNote("&cPlayer's primary group is not in sorting list");
		} else {
			p.setTeamNameNote("Primary group is #" + Integer.parseInt(chars) + " in sorting list");
		}
		return chars;
	}
	
	@Override
	public String toString() {
		return "GROUPS";
	}
}