package me.neznamy.tab.shared.features.sorting.types;

import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.features.sorting.Sorting;

import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * Sorting by a placeholder by values defined in list
 */
public class Placeholder extends SortingType {

    //map Value-Number where number is used in team name based on value
    private final LinkedHashMap<String, String> sortingMap;

    /**
     * Constructs new instance with given parameters
     *
     * @param   sorting
     *          sorting feature
     * @param   options
     *          options used by this sorting type
     */
    public Placeholder(Sorting sorting, String options) {
        super(sorting, getPlaceholder(options));
        String[] args = options.split(":");
        if (args.length > 1)
            sortingMap = convertSortingElements(args[args.length-1].split(","));
        else
            // Invalid configuration
            sortingMap = new LinkedHashMap<>();
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
    public String getChars(ITabPlayer p) {
        String output = EnumChatFormat.color(setPlaceholders(p));
        sorting.setTeamNameNote(p, sorting.getTeamNameNote(p) + sortingPlaceholder + " returned \"" + output + "\"");
        String sortingValue = sortingMap.get(output.toLowerCase(Locale.US));
        if (sortingValue == null) {
            sortingValue = String.valueOf(sortingMap.size()+1);
            sorting.setTeamNameNote(p, sorting.getTeamNameNote(p) + "&c (not in list)&r. ");
        } else {
            sorting.setTeamNameNote(p, sorting.getTeamNameNote(p) + "&r (#" + Integer.parseInt(sortingValue) + " in list). &r");
        }
        return sortingValue;
    }

    @Override
    public String toString() {
        return "PLACEHOLDER";
    }
}