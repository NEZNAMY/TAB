package me.neznamy.tab.shared.features.sorting.types;

import java.util.LinkedHashMap;
import java.util.Locale;

import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.features.sorting.Sorting;

/**
 * Sorting by a placeholder by values defined in list
 */
public class Placeholder extends SortingType {

	//map Value-Number where number is used in team name based on value
	private final LinkedHashMap<String, String> sortingMap;
	
	/**
	 * Constructs new instance with given parameters
	 * @param	sorting
	 * 			sorting feature
	 * @param 	options
	 * 			options used by this sorting type
	 */
	public Placeholder(Sorting sorting, String options) {
		super(sorting, options.split(":")[0]);
		sortingMap = convertSortingElements(options.substring(options.indexOf(":")+1).split(","));
	}

	@Override
	public String getChars(ITabPlayer p) {
		String output = EnumChatFormat.color(setPlaceholders(p));
		p.setTeamNameNote(p.getTeamNameNote() + sortingPlaceholder + " returned \"" + output + "\"");
		String sortingValue = sortingMap.get(output.toLowerCase(Locale.US));
		if (sortingValue == null) {
			sortingValue = String.valueOf(sortingMap.size()+1);
			p.setTeamNameNote(p.getTeamNameNote() + "&c (not in list)&r. ");
		} else {
			p.setTeamNameNote(p.getTeamNameNote() + "&r (#" + Integer.parseInt(sortingMap.get(output)) + " in list). &r");
		}
		return sortingValue;
	}
	
	@Override
	public String toString() {
		return "PLACEHOLDER";
	}
}