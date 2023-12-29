package me.neznamy.tab.shared.features.sorting.types;

import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.sorting.Sorting;
import org.jetbrains.annotations.NotNull;

/**
 * Sorting by a placeholder alphabetically
 */
public class PlaceholderAtoZ extends SortingType {

    /**
     * Constructs new instance with given parameters.
     *
     * @param   sorting
     *          Sorting feature
     * @param   sortingPlaceholder
     *          Placeholder to sort by
     */
    public PlaceholderAtoZ(Sorting sorting, String sortingPlaceholder) {
        super(sorting, "PLACEHOLDER_A_TO_Z", sortingPlaceholder);
    }

    @Override
    public String getChars(@NotNull TabPlayer p) {
        String output = setPlaceholders(p);
        sorting.setTeamNameNote(p, sorting.getTeamNameNote(p) + "\n-> " + sortingPlaceholder + " returned \"&e" + output + "&r\". &r");
        return sorting.isCaseSensitiveSorting() ? output : output.toLowerCase();
    }
}