package me.neznamy.tab.shared.features.sorting.types;

import java.util.LinkedHashMap;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;

/**
 * Sorting by permission nodes tab.sort.<name>, where names are defined in sorting list
 */
public class GroupPermission extends SortingType {

	//map of sorted groups in config
	private LinkedHashMap<String, String> sortedGroups;
	
	/**
	 * Constructs new instance
	 */
	public GroupPermission() {
		sortedGroups = loadSortingList();
	}
	
	@Override
	public String getChars(ITabPlayer p) {
		String chars = null;
		for (String localgroup : sortedGroups.keySet()) {
			if (p.hasPermission("tab.sort." + localgroup)) {
				chars = sortedGroups.get(localgroup.toLowerCase());
				p.setTeamNameNote("Highest sorting permission: &etab.sort." + localgroup + " &a(#" + Integer.parseInt(chars) + " in sorting list)");
				if (p.hasPermission("random.permission")) {
					p.setTeamNameNote(p.getTeamNameNote() + "&cThis user appears to have all permissions. Is he OP? &r");
				}
				break;
			}
		}
		if (chars == null) {
			chars = "9";
			TAB.getInstance().getErrorManager().oneTimeConsoleError("Sorting by permissions is enabled but player " + p.getName() + " does not have any sorting permission. Configure sorting permissions or disable sorting by permissions like it is by default.");
			p.setTeamNameNote(p.getTeamNameNote() + "&cPlayer does not have sorting permission for any group in sorting list. ");
		}
		return chars;
	}
	
	@Override
	public String toString() {
		return "GROUP_PERMISSIONS";
	}
}