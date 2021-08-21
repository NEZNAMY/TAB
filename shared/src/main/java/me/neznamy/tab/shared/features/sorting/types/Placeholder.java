package me.neznamy.tab.shared.features.sorting.types;

import java.util.LinkedHashMap;

import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;

/**
 * Sorting by a placeholder by values defined in list
 */
public class Placeholder extends SortingType {

	//map Value-Number where number is used in team name based on value
	private LinkedHashMap<String, String> sortingMap;
	
	/**
	 * Constructs new instance with given parameter
	 * @param sortingPlaceholder - placeholder to sort by
	 */
	public Placeholder(String options) {
		super(options.split(":")[0]);
		sortingMap = convertSortingElements(options.substring(options.indexOf(":")+1).split(","));
	}

	@Override
	public String getChars(ITabPlayer p) {
		String output = EnumChatFormat.color(setPlaceholders(p));
		p.setTeamNameNote(p.getTeamNameNote() + sortingPlaceholder + " returned \"" + output + "\"");
		String sortingValue = sortingMap.get(output);
		if (sortingValue == null) {
			sortingValue = String.valueOf(sortingMap.size()+1);
			TAB.getInstance().getErrorManager().oneTimeConsoleError("Sorting by predefined placeholder values is enabled, but output \"" + output + "\" is not listed. List: " + sortingMap.keySet());
			p.setTeamNameNote(p.getTeamNameNote() + "&c (not in list). &r");
		} else {
			p.setTeamNameNote(p.getTeamNameNote() + " (#" + Integer.parseInt(sortingMap.get(output)) + " in list). &r");
		}
		return sortingValue;
	}
	
	@Override
	public String toString() {
		return "PLACEHOLDER";
	}
}