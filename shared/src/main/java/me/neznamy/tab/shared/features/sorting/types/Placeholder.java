package me.neznamy.tab.shared.features.sorting.types;

import me.neznamy.tab.shared.TAB;
import me.neznamy.chat.EnumChatFormat;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.sorting.Sorting;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * Sorting by a placeholder by values defined in list
 */
public class Placeholder extends SortingType {

    /** Map of priorities for each output */
    private final LinkedHashMap<String, Integer> sortingMap;

    /**
     * Constructs new instance with given parameters
     *
     * @param   sorting
     *          sorting feature
     * @param   options
     *          options used by this sorting type
     */
    public Placeholder(Sorting sorting, String options) {
        super(sorting, "PLACEHOLDER", getPlaceholder(options));
        String[] args = options.split(":");
        String elements = args[args.length-1];
        if (args.length > 1) {
            String[] array = elements.split(",");
            if (elements.endsWith(",")) {
                // Allow empty string as output
                array = Arrays.copyOf(array, array.length+1);
                array[array.length-1] = "";
            }
            sortingMap = convertSortingElements(array);
        } else {
            TAB.getInstance().getConfigHelper().startup().incompleteSortingLine("PLACEHOLDER:" + options);
            sortingMap = new LinkedHashMap<>();
        }
    }

    /**
     * Returns placeholder identifier used in provided options. This allows
     * support for placeholders that contain ":", such as conditions or animations.
     *
     * @param   options
     *          Configured sorting options in "%placeholder%:values" format
     * @return  Placeholder configured in options
     */
    private static String getPlaceholder(String options) {
        String[] args = options.split(":");
        if (args.length == 1) return args[0]; // Missing predefined values
        return options.substring(0, options.length()-args[args.length-1].length()-1);
    }

    @Override
    public String getChars(@NotNull TabPlayer p) {
        if (!valid) return "";
        return String.valueOf((char) (getPosition(p) + 47));
    }

    @Override
    public int getPosition(@NotNull TabPlayer p) {
        if (!valid) return 0;
        String output = EnumChatFormat.color(setPlaceholders(p));
        p.sortingData.teamNameNote += "\n-> " + sortingPlaceholder + " returned \"&e" + output + "&r\"";
        int position;
        String cleanOutput = output.trim().toLowerCase(Locale.US);
        if (!sortingMap.containsKey(cleanOutput)) {
            TAB.getInstance().getConfigHelper().runtime().valueNotInPredefinedValues(sortingPlaceholder, sortingMap.keySet(), cleanOutput, p);
            position = sortingMap.size()+1;
            p.sortingData.teamNameNote += "&c (not in list)&r. ";
        } else {
            position = sortingMap.get(cleanOutput);
            p.sortingData.teamNameNote += "&r &a(#" + position + " in list). &r";
        }
        return position;
    }
}