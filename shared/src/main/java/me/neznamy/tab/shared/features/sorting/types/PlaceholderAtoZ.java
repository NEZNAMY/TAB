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
        p.sortingData.teamNameNote += "\n-> " + sortingPlaceholder + " returned \"&e" + output + "&r\". &r";
        return sorting.getConfiguration().isCaseSensitiveSorting() ? output : output.toLowerCase();
    }

    @Override
    public int getPosition(@NotNull TabPlayer p) {
        String s = getChars(p);

        double weight = 0.0;
        double factor = 1.0;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int value;

            if (Character.isUpperCase(c)) {
                value = c - 'A' + 1; // A = 1, B = 2, ..., Z = 26
            } else if (Character.isLowerCase(c)) {
                value = c - 'a' + 27; // a = 27, b = 28, ..., z = 52
            } else {
                // Ignore other chars
                value = 0;
            }

            factor /= 53.0; // base 53 (26 uppercase + 26 lowercase + 1 to avoid 0)
            weight += value * factor;
        }

        double ratio = 1.0 - weight; // always between 1 and 0

        return (int) ratio * 10000000;
    }
}