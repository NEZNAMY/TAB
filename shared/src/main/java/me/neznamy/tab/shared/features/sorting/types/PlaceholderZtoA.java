package me.neznamy.tab.shared.features.sorting.types;

import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.sorting.Sorting;
import org.jetbrains.annotations.NotNull;

/**
 * Sorting by a placeholder from Z to A
 */
public class PlaceholderZtoA extends SortingType {

    /**
     * Constructs new instance with given parameter
     *
     * @param   sorting
     *          Sorting feature
     * @param   sortingPlaceholder
     *          Placeholder to sort by
     */
    public PlaceholderZtoA(Sorting sorting, String sortingPlaceholder) {
        super(sorting, "PLACEHOLDER_Z_TO_A", sortingPlaceholder);
    }

    @Override
    public String getChars(@NotNull TabPlayer p) {
        char[] chars = setPlaceholders(p).toCharArray();
        sorting.setTeamNameNote(p, sorting.getTeamNameNote(p) + "\n-> " + sortingPlaceholder + " returned \"&e" + new String(chars) + "&r\". &r");
        for (int i=0; i<chars.length; i++) {
            char c = chars[i];
            if (c >= 65 && c <= 90) {
                chars[i] = (char) (155 - c);
            }
            if (c >= 97 && c <= 122) {
                chars[i] = (char) (219 - c);
            }
        }
        String s = new String(chars);
        return sorting.isCaseSensitiveSorting() ? s : s.toLowerCase();
    }
}