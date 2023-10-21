package me.neznamy.tab.shared.features.sorting.types;

import java.util.Collections;
import java.util.LinkedHashMap;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.sorting.Sorting;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public abstract class SortingType {

    protected final Sorting sorting;

    private final String displayName;

    //number to add to / subtract from to prevent incorrect sorting with negative values
    protected final int DEFAULT_NUMBER = Integer.MAX_VALUE / 2;
    
    //placeholder to sort by, if sorting type uses it
    protected String sortingPlaceholder;

    /**
     * Constructs new instance with given parameter
     *
     * @param   sortingPlaceholder
     *          placeholder to sort by
     */
    protected SortingType(Sorting sorting, String displayName, String sortingPlaceholder) {
        this.sorting = sorting;
        this.displayName = displayName;
        if (!sortingPlaceholder.startsWith("%") || !sortingPlaceholder.endsWith("%")) {
            TAB.getInstance().getMisconfigurationHelper().invalidSortingPlaceholder(sortingPlaceholder, this);
        } else {
            sorting.addUsedPlaceholders(Collections.singletonList(sortingPlaceholder));
            this.sortingPlaceholder = sortingPlaceholder;
        }
    }
    
    /**
     * Applies all placeholders for specified player
     *
     * @param   player
     *          player to set placeholders for
     * @return  text with replaced placeholders
     */
    protected String setPlaceholders(TabPlayer player) {
        if (sortingPlaceholder == null) return "";
        return TAB.getInstance().getPlaceholderManager().getPlaceholder(sortingPlaceholder).set(sortingPlaceholder, player);
    }
    
    protected LinkedHashMap<String, Integer> convertSortingElements(String[] elements) {
        LinkedHashMap<String, Integer> sortedGroups = new LinkedHashMap<>();
        int index = 1;
        for (String element : elements) {
            for (String element0 : element.split("\\|")) {
                sortedGroups.put(EnumChatFormat.color(element0.trim().toLowerCase()), index);
            }
            index++;
        }
        return sortedGroups;
    }

    /**
     * Compresses a number to ### format, where # is a character symbol representing
     * a number in a base of 65536. The first two represent the whole part, the third one decimal part.
     * The maximum number it will work properly with is {@link Integer#MAX_VALUE}.
     *
     * @param   number
     *          Number to convert
     * @return  3 characters long String of converted number with a base of 65536.
     */
    public String compressNumber(double number) {
        int wholePart = (int) number;
        char decimalChar = (char) ((number - wholePart) * Character.MAX_VALUE);
        StringBuilder sb = new StringBuilder();
        while (wholePart > 0) {
            char digit = (char) (wholePart % Character.MAX_VALUE);
            sb.append(digit);
            wholePart /= Character.MAX_VALUE;
        }
        sb.reverse();
        if (sb.length() == 1) sb.insert(0, (char) 0); // Avoid a single # if number is < 65535
        sb.append(decimalChar);
        return sb.toString();
    }

    /**
     * Parses double in given string and returns it.
     * Returns second argument if string is not valid and prints a console warn.
     *
     * @param   placeholder
     *          Raw placeholder, used in error message
     * @param   output
     *          string to parse
     * @param   defaultValue
     *          value to return if string is not valid
     * @param   player
     *          Player name used in error message
     * @return  parsed double or {@code defaultValue} if input is invalid
     */
    public double parseDouble(@NotNull String placeholder, @NotNull String output, double defaultValue, TabPlayer player) {
        try {
            return Double.parseDouble(output.replace(",", "."));
        } catch (NumberFormatException e) {
            TAB.getInstance().getMisconfigurationHelper().invalidInputForNumericSorting(this, placeholder, output, player);
            return defaultValue;
        }
    }

    @Override
    public final String toString() {
        return displayName;
    }
    
    /**
     * Returns current sorting characters of this sorting type for specified player
     *
     * @param   p
     *          player to get chars for
     * @return  an as-short-as-possible character sequence for unique sorting
     */
    public abstract String getChars(TabPlayer p);
}