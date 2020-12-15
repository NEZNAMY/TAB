package me.neznamy.tab.shared.features.sorting.types;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;

public class PlaceholderLowToHigh extends SortingType {

	public PlaceholderLowToHigh(String sortingPlaceholder) {
		super(sortingPlaceholder);
	}

	@Override
	public String getChars(TabPlayer p) {
		int intValue = Shared.errorManager.parseInteger(setPlaceholders(sortingPlaceholder, p), 0, "numeric sorting placeholder");
		return String.valueOf(DEFAULT_NUMBER + intValue);
	}
	
	@Override
	public String toString() {
		return "PLACEHOLDER_LOW_TO_HIGH";
	}
}