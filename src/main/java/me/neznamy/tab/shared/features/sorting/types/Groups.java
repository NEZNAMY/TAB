package me.neznamy.tab.shared.features.sorting.types;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.features.sorting.Sorting;

public class Groups extends SortingType {

	private Sorting feature;
	
	public Groups(Sorting feature, String sortingPlaceholder) {
		super(sortingPlaceholder);
		this.feature = feature;
	}

	@Override
	public String getChars(TabPlayer p) {
		String group = p.getGroup();
		String chars = feature.sortedGroups.get(group.toLowerCase());
		if (chars == null) {
			chars = "999";
			if (!group.equals("<null>")) Shared.errorManager.oneTimeConsoleError("Group \"&e" + group + "&c\" is not defined in sorting list! This will result in players in that group not being sorted correctly. To fix this, add group \"&e" + group + "&c\" into &egroup-sorting-priority-list in config.yml&c.");
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