package me.neznamy.tab.shared.features.sorting.types;

import java.util.LinkedHashMap;

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
		sortingMap = convertSortingElements(options.split(","));
	}

	@Override
	public String getChars(ITabPlayer p) {
		String output = setPlaceholders(p);
		p.setTeamNameNote(p.getTeamNameNote() + "Placeholder returned \"" + output + "\". ");
		if (output.contains("&")) output = output.replace('&', '\u00a7');
		String sortingValue = sortingMap.get(output);
		if (sortingValue == null) {
			sortingValue = String.valueOf(sortingMap.size()+1);
			TAB.getInstance().getErrorManager().oneTimeConsoleError("Sorting by predefined placeholder values is enabled, but output \"" + output + "\" is not listed.");
			p.setTeamNameNote(p.getTeamNameNote() + "&cPlayer's placeholder output is not in list. ");
		}
		return sortingValue;
	}
	
	@Override
	public String toString() {
		return "PLACEHOLDER_A_TO_Z";
	}
}