package me.neznamy.tab.shared.features.sorting.types;

import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.sorting.Sorting;
import org.jetbrains.annotations.NotNull;

/**
 * Sorting by a numeric placeholder from lowest to highest
 */
public class PlaceholderLowToHigh extends SortingType {

    /**
     * Constructs new instance with given parameters.
     *
     * @param   sorting
     *          Sorting feature
     * @param   sortingPlaceholder
     *          Placeholder to sort by
     */
    public PlaceholderLowToHigh(Sorting sorting, String sortingPlaceholder) {
        super(sorting, "PLACEHOLDER_LOW_TO_HIGH", sortingPlaceholder);
    }

    @Override
    public String getChars(@NotNull TabPlayer p) {
        String output = setPlaceholders(p);
        sorting.setTeamNameNote(p, sorting.getTeamNameNote(p) + "\n-> " + sortingPlaceholder.getIdentifier() +
                " returned \"&e" + output + "&r\". &r");
        return compressNumber(DEFAULT_NUMBER + parseDouble(sortingPlaceholder.getIdentifier(), output, 0, p));
    }
}