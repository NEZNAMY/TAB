package me.neznamy.tab.shared.features.sorting.types;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

/**
 * Sorting by a numeric placeholder from lowest to highest
 */
public class PlaceholderLowToHigh extends SortingType {

	/**
	 * Constructs new instance with given parameter
	 * @param sortingPlaceholder - placeholder to sort by
	 */
	public PlaceholderLowToHigh(String sortingPlaceholder) {
		super(sortingPlaceholder);
	}

	@Override
	public String getChars(TabPlayer p) {
		int intValue = TAB.getInstance().getErrorManager().parseInteger(setPlaceholders(p), 0, "numeric sorting placeholder");
		return String.valueOf(DEFAULT_NUMBER + intValue);
	}
	
	@Override
	public String toString() {
		return "PLACEHOLDER_LOW_TO_HIGH";
	}
}