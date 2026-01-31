package me.neznamy.tab.shared.features.sorting.types;

import lombok.AllArgsConstructor;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     *          Sorting feature
     * @param   result
     *          Result of splitting the options string containing the placeholder and its values
     */
    public Placeholder(Sorting sorting, PlaceholderSplitResult result) {
        super(sorting, "PLACEHOLDER:" + result.placeholder, result.placeholder);
        sortingMap = convertSortingElements(result.values);
    }

    /**
     * Splits the given options string into a placeholder and its values.
     *
     * @param   options
     *          Options string in the format "%placeholder%:value1,value2,...,valueN"
     * @return  Result containing the placeholder and an array of values
     */
    @Nullable
    public static PlaceholderSplitResult splitValue(@NotNull String options) {
        Pattern pattern = Pattern.compile("(%[^%]+%):(.+)");
        Matcher matcher = pattern.matcher(options);

        if (!matcher.matches()) {
            TAB.getInstance().getConfigHelper().startup().invalidSortingLine("PLACEHOLDER:" + options, "Invalid format. Expected \"%placeholder%:value1,value2,...,valueN\".");
            return null;
        }

        String placeholder = matcher.group(1);
        String[] values = matcher.group(2).split(",", -1);
        return new PlaceholderSplitResult(placeholder, values);
    }

    @Override
    public String getChars(@NotNull TabPlayer p) {
        if (!valid) return "";
        String output = EnumChatFormat.color(setPlaceholders(p));
        int position;
        String cleanOutput = output.trim().toLowerCase(Locale.US);
        if (!sortingMap.containsKey(cleanOutput)) {
            TAB.getInstance().getConfigHelper().runtime().valueNotInPredefinedValues(sortingPlaceholder.getIdentifier(), sortingMap.keySet(), cleanOutput, p);
            position = sortingMap.size()+1;
        } else {
            position = sortingMap.get(cleanOutput);
        }
        return String.valueOf((char) (position + 47));
    }

    @Override
    @NotNull
    public String getReturnedValue(@NotNull TabPlayer p) {
        if (!valid) return "<INVALID>";
        String output = EnumChatFormat.color(setPlaceholders(p));
        String cleanOutput = output.trim().toLowerCase(Locale.US);
        if (!sortingMap.containsKey(cleanOutput)) {
            return output + " (not in list)";
        } else {
            return output + " (#" + sortingMap.get(cleanOutput) + " in list)";
        }
    }

    @AllArgsConstructor
    public static class PlaceholderSplitResult {

        @NotNull private final String placeholder;
        @NotNull private final String[] values;
    }
}