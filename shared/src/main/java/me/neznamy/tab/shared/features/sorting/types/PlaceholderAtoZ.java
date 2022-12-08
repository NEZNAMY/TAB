package me.neznamy.tab.shared.features.sorting.types;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.features.sorting.Sorting;

/**
 * Sorting by a placeholder alphabetically
 */
public class PlaceholderAtoZ extends SortingType {

    /**
     * Constructs new instance with given parameter
     *
     * @param   sortingPlaceholder
     *          placeholder to sort by
     */
    public PlaceholderAtoZ(Sorting sorting, String sortingPlaceholder) {
        super(sorting, "PLACEHOLDER_A_TO_Z", sortingPlaceholder);
    }

    @Override
    public String getChars(ITabPlayer p) {
        String output = setPlaceholders(p);
        sorting.setTeamNameNote(p, sorting.getTeamNameNote(p) + "\n-> " + sortingPlaceholder + " returned \"&e" + output + "&r\". &r");
        return sorting.isCaseSensitiveSorting() ? output : output.toLowerCase();
    }
}