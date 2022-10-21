package me.neznamy.tab.shared.features.sorting.types;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.sorting.Sorting;

/**
 * Sorting by a numeric placeholder from highest to lowest
 */
public class PlaceholderHighToLow extends SortingType {

    /**
     * Constructs new instance with given parameter
     *
     * @param   sortingPlaceholder
     *          placeholder to sort by
     */
    public PlaceholderHighToLow(Sorting sorting, String sortingPlaceholder) {
        super(sorting, sortingPlaceholder);
    }

    @Override
    public String getChars(ITabPlayer p) {
        String output = setPlaceholders(p);
        sorting.setTeamNameNote(p, sorting.getTeamNameNote(p) + sortingPlaceholder + " returned \"" + output + "\". &r");
        double doubleValue = TAB.getInstance().getErrorManager().parseDouble(output, 0);
        String string = String.valueOf(DEFAULT_NUMBER - doubleValue);
        return string.length() > 10 ? string.substring(0, 10) : string;
    }

    @Override
    public String toString() {
        return "PLACEHOLDER_HIGH_TO_LOW";
    }
}