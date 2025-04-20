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
        p.sortingData.teamNameNote += "\n-> " + sortingPlaceholder + " returned \"&e" + new String(chars) + "&r\". &r";
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
        return sorting.getConfiguration().isCaseSensitiveSorting() ? s : s.toLowerCase();
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
                value = 'Z' - c + 1; // Z = 1, ..., A = 26
            } else if (Character.isLowerCase(c)) {
                value = 'z' - c + 27; // z = 27, ..., a = 52
            } else {
                value = 0; // other chars are ignored
            }

            factor /= 53.0;
            weight += value * factor;
        }

        return (int) weight * 10000000;
    }
}