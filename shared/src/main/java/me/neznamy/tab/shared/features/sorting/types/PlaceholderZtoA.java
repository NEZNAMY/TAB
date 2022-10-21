package me.neznamy.tab.shared.features.sorting.types;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.features.sorting.Sorting;

/**
 * Sorting by a placeholder from Z to A
 */
public class PlaceholderZtoA extends SortingType {

    /**
     * Constructs new instance with given parameter
     *
     * @param   sortingPlaceholder
     *          placeholder to sort by
     */
    public PlaceholderZtoA(Sorting sorting, String sortingPlaceholder) {
        super(sorting, sortingPlaceholder);
    }

    @Override
    public String getChars(ITabPlayer p) {
        char[] chars = setPlaceholders(p).toCharArray();
        sorting.setTeamNameNote(p, sorting.getTeamNameNote(p) + sortingPlaceholder + " returned \"" + new String(chars) + "\". &r");
        for (int i=0; i<chars.length; i++) {
            char c = chars[i];
            if (c >= 65 && c <= 90) {
                chars[i] = (char) (155 - c);
            }
            if (c >= 97 && c <= 122) {
                chars[i] = (char) (219 - c);
            }
        }
        return new String(chars);
    }

    @Override
    public String toString() {
        return "PLACEHOLDER_Z_TO_A";
    }
}