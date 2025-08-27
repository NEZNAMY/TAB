package me.neznamy.tab.shared.placeholders;

import lombok.Getter;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.Map.Entry;

/**
 * Placeholder replacement pattern class for placeholder output replacements
 * feature.
 */
public class PlaceholderReplacementPattern {

    /** Instance for empty map to only have a single instance */
    public static final PlaceholderReplacementPattern EMPTY = new PlaceholderReplacementPattern("", Collections.emptyMap());

    /**
     * Full replacement map with values colored and keys being duplicated,
     * once with and once without colors
     */
    private final Map<String, String> replacements = new HashMap<>();

    /**
     * Map of number intervals where key is a 2-dimensional array
     * with first value being minimum and second value maximum and value
     * being output to replace to.
     */
    private final Map<float[], String> numberIntervals = new HashMap<>();

    /** Set of all used placeholders in replacement values */
    @Getter private final Set<String> nestedPlaceholders = new HashSet<>();

    /** Flag tracking if this replacement map is empty */
    private final boolean empty;

    /**
     * Constructs new instance from given replacement map from config
     *
     * @param   identifier
     *          placeholder identifier which this pattern belongs to
     * @param   map
     *          replacement map from config
     */
    private PlaceholderReplacementPattern(@NotNull String identifier, @NotNull Map<Object, Object> map) {
        empty = map.isEmpty();
        for (Entry<Object, Object> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            String value = String.valueOf(entry.getValue()).replace(identifier, "%value%");
            replacements.put(EnumChatFormat.color(key), EnumChatFormat.color(value));
            replacements.put(key, value);
            nestedPlaceholders.addAll(PlaceholderManagerImpl.detectPlaceholders(value));
            nestedPlaceholders.remove("%value%"); //not a real placeholder
            //snakeyaml converts yes & no to booleans, making them not work when used without "
            if ("true".equals(key)) {
                replacements.put("yes", value);
                replacements.put("Yes", value);
            } else if ("false".equals(key)) {
                replacements.put("no", value);
                replacements.put("No", value);
            } else if (key.contains("-")) {
                try {
                    numberIntervals.put(new float[]{Float.parseFloat(key.split("-")[0]), Float.parseFloat(key.split("-")[1])}, value);
                } catch (NumberFormatException ignored) {}
                try {
                    numberIntervals.put(new float[]{Float.parseFloat(key.split("~")[0]), Float.parseFloat(key.split("~")[1])}, value);
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    /**
     * Finds replacement using provided output as well as applying
     * %value% placeholder for original output inside replacements.
     *
     * @param   output
     *          placeholder's output
     * @return  replacement or {@code output} if no pattern is matching
     */
    @NotNull
    public String findReplacement(@NotNull String output) {
        if (empty) return output;
        String replacement = findReplacement0(output);
        if (replacement.contains("%value%")) {
            replacement = replacement.replace("%value%", output);
        }
        return replacement;
    }

    /**
     * Internal method that returns value based on provided
     * placeholder output and configured replacements.
     *
     * @param   output
     *          placeholder's output
     * @return  replacement or {@code output} if no pattern is matching
     */
    @NotNull
    private String findReplacement0(@NotNull String output) {
        //exact output
        if (replacements.containsKey(output)) {
            return replacements.get(output);
        }
        
        //number interval
        if (!numberIntervals.isEmpty()) {  //not parsing number if no intervals are configured
            try {
                //supporting placeholders with fancy output using "," every 3 digits
                String cleanValue = output.contains(",") ? output.replace(",", "") : output;
                float value = Float.parseFloat(cleanValue);
                for (Entry<float[], String> entry : numberIntervals.entrySet()) {
                    if (entry.getKey()[0] <= value && value <= entry.getKey()[1]) return entry.getValue();
                }
            } catch (NumberFormatException e) {
                //placeholder output is not a number
            }
        }

        //else
        if (replacements.containsKey("else")) return replacements.get("else");
        
        //nothing was found
        return output;
    }

    /**
     * Returns new instance from given parameters.
     *
     * @param   identifier
     *          Placeholder identifier which this pattern belongs to
     * @param   map
     *          Replacement map from config
     * @return  New instance from given parameters
     */
    public static PlaceholderReplacementPattern create(@NotNull String identifier, @NotNull Map<Object, Object> map) {
        if (map.isEmpty()) return EMPTY;
        return new PlaceholderReplacementPattern(identifier, map);
    }
}