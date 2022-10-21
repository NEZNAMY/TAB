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
        super(sorting, sortingPlaceholder);
    }

    @Override
    public String getChars(ITabPlayer p) {
        String output = setPlaceholders(p);
        sorting.setTeamNameNote(p, sorting.getTeamNameNote(p) + sortingPlaceholder + " returned \"" + output + "\". &r");
        return output;
    }

    @Override
    public String toString() {
        return "PLACEHOLDER_A_TO_Z";
    }
}