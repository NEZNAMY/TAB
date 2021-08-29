package me.neznamy.tab.shared.features.sorting.types;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.sorting.Sorting;

/**
 * Sorting by a numeric placeholder from lowest to highest
 */
public class PlaceholderLowToHigh extends SortingType {

	/**
	 * Constructs new instance with given parameter
	 * @param sortingPlaceholder - placeholder to sort by
	 */
	public PlaceholderLowToHigh(Sorting sorting, String sortingPlaceholder) {
		super(sorting, sortingPlaceholder);
	}

	@Override
	public String getChars(ITabPlayer p) {
		String output = setPlaceholders(p);
		p.setTeamNameNote(p.getTeamNameNote() + sortingPlaceholder + " returned \"" + output + "\". &r");
		int intValue = TAB.getInstance().getErrorManager().parseInteger(output, 0, "numeric sorting placeholder");
		return String.valueOf(DEFAULT_NUMBER + intValue);
	}
	
	@Override
	public String toString() {
		return "PLACEHOLDER_LOW_TO_HIGH";
	}
}