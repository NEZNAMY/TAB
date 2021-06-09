package me.neznamy.tab.shared.features.sorting.types;

import me.neznamy.tab.shared.ITabPlayer;

/**
 * Sorting by a placeholder alphabetically
 */
public class PlaceholderAtoZ extends SortingType {

	/**
	 * Constructs new instance with given parameter
	 * @param sortingPlaceholder - placeholder to sort by
	 */
	public PlaceholderAtoZ(String sortingPlaceholder) {
		super(sortingPlaceholder);
	}

	@Override
	public String getChars(ITabPlayer p) {
		String output = setPlaceholders(p);
		p.setTeamNameNote(p.getTeamNameNote() + "Placeholder returned \"" + output + "\". ");
		return output;
	}
	
	@Override
	public String toString() {
		return "PLACEHOLDER_A_TO_Z";
	}
}