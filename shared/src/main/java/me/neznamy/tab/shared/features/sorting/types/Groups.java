package me.neznamy.tab.shared.features.sorting.types;

import java.util.LinkedHashMap;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.GroupRefresher;

/**
 * Sorting by primary permission groups
 */
public class Groups extends SortingType {

	//map of sorted groups in config
	private LinkedHashMap<String, String> sortedGroups;
	
	/**
	 * Constructs new instance
	 */
	public Groups(String options) {
		sortedGroups = convertSortingElements(options.split(","));
	}

	@Override
	public String getChars(ITabPlayer p) {
		String group = p.getGroup();
		String chars = sortedGroups.get(group.toLowerCase());
		if (chars == null) {
			chars = String.valueOf(sortedGroups.size()+1);
			if (!group.equals(GroupRefresher.DEFAULT_GROUP)) {
				TAB.getInstance().getErrorManager().oneTimeConsoleError(String.format("Group \"%s\" is not defined in sorting list! This will result in players in that group not being sorted correctly. To fix this, add group \"%s\" into group-sorting-priority-list in config.yml. Your current list: %s", group, group, sortedGroups.keySet()));
			}
			p.setTeamNameNote(p.getTeamNameNote() + "&cPlayer's primary group is not in sorting list. &r");
		} else {
			p.setTeamNameNote(p.getTeamNameNote() + String.format("Primary group is #%s in sorting list", Integer.parseInt(chars)) + ". &r");
		}
		return chars;
	}
	
	@Override
	public String toString() {
		return "GROUPS";
	}
}