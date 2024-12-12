package me.neznamy.tab.shared.features.sorting.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.placeholders.types.TabPlaceholder;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;

/**
 * Abstract class for sorting types.
 */
@RequiredArgsConstructor
public abstract class SortingType {

    /** Sorting feature */
    protected final Sorting sorting;

    /** Display name of this sorting type */
    @Getter
    private final String displayName;

    /** Number to add to / subtract from to prevent incorrect sorting with negative values */
    protected final int DEFAULT_NUMBER = Integer.MAX_VALUE / 2;
    
    /** Placeholder to sort by, if sorting type uses it */
    protected String sortingPlaceholder;

    /** Flag tracking if this sorting type is valid or not. If not, it is disabled. */
    protected final boolean valid;

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
            TAB.getInstance().getConfigHelper().startup().invalidSortingPlaceholder(sortingPlaceholder, this);
            valid = false;
        } else {
            sorting.addUsedPlaceholder(sortingPlaceholder);
            this.sortingPlaceholder = sortingPlaceholder;
            valid = true;
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
        TabPlaceholder placeholder = TAB.getInstance().getPlaceholderManager().getPlaceholder(sortingPlaceholder);
        return placeholder.set(placeholder.getIdentifier(), player);
    }

    /**
     * Converts array of elements into map with priorities where 1 is highest.
     *
     * @param   elements
     *          Configured sorting values
     * @return  Converted map with priorities
     */
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
     * a number in a base of 65534. The first two represent the whole part, the third one decimal part.
     * The maximum number it will work properly with is {@link Integer#MAX_VALUE}.
     *
     * @param   number
     *          Number to convert
     * @return  3 characters long String of converted number with a base of 65534.
     */
    public String compressNumber(double number) {
        int wholePart = (int) number;
        int base = Character.MAX_VALUE - 1;
        char decimalChar = (char) ((number - wholePart) * base);
        // The \ symbol breaks json syntax, skip it (and reduce range) (why is it not being escaped by json writer?)
        if (decimalChar >= '\\') decimalChar++;
        StringBuilder sb = new StringBuilder();
        while (wholePart > 0) {
            char digit = (char) (wholePart % base);
            if (digit >= '\\') digit++;
            sb.insert(0, digit);
            wholePart /= base;
        }
        while (sb.length() < 2) {
            sb.insert(0, (char) 0); // Avoid a single # if number is < base
        }
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
            TAB.getInstance().getConfigHelper().runtime().invalidInputForNumericSorting(this, placeholder, output, player);
            return defaultValue;
        }
    }

    /**
     * Returns current sorting characters of this sorting type for specified player
     *
     * @param   p
     *          player to get chars for
     * @return  an as-short-as-possible character sequence for unique sorting
     */
    public abstract String getChars(@NotNull TabPlayer p);
}