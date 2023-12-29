package me.neznamy.tab.shared.features.sorting.types;

import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.sorting.Sorting;
import org.jetbrains.annotations.NotNull;

/**
 * Sorting by a numeric placeholder from highest to lowest
 */
public class PlaceholderHighToLow extends SortingType {

    /**
     * Constructs new instance with given parameters.
     *
     * @param   sorting
     *          Sorting feature
     * @param   sortingPlaceholder
     *          Placeholder to sort by
     */
    public PlaceholderHighToLow(Sorting sorting, String sortingPlaceholder) {
        super(sorting, "PLACEHOLDER_HIGH_TO_LOW", sortingPlaceholder);
    }

    @Override
    public String getChars(@NotNull TabPlayer p) {
        String output = setPlaceholders(p);
        sorting.setTeamNameNote(p, sorting.getTeamNameNote(p) + "\n-> " + sortingPlaceholder + " returned \"&e" + output + "&r\". &r");
        return compressNumber(DEFAULT_NUMBER - parseDouble(sortingPlaceholder, output, 0, p));
    }
}