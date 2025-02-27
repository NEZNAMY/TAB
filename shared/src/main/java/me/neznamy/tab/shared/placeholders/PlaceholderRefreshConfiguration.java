package me.neznamy.tab.shared.placeholders;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TabConstants.Placeholder;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Class storing configuration of placeholder refresh intervals.
 */
@Getter
@RequiredArgsConstructor
public class PlaceholderRefreshConfiguration {

    private final int defaultInterval;
    @NotNull private final Map<String, Integer> refreshIntervals;

    /**
     * Returns refresh interval for specified placeholder.
     * If not defined, {@link #defaultInterval} is returned.
     *
     * @param   identifier
     *          Placeholder identifier
     * @return  Refresh interval for given placeholder
     */
    public int getRefreshInterval(@NotNull String identifier) {
        return refreshIntervals.getOrDefault(identifier, defaultInterval);
    }

    /**
     * Returns refresh interval for specified placeholder.
     * If not defined, {@code defaultInterval} is returned.
     *
     * @param   identifier
     *          Placeholder identifier
     * @param   defaultInterval
     *          Interval to use if not defined in config
     * @return  Refresh interval for given placeholder
     */
    public int getRefreshInterval(@NotNull String identifier, int defaultInterval) {
        return refreshIntervals.getOrDefault(identifier, defaultInterval);
    }

    /**
     * Returns instance of this class created from the given configuration section.
     * If there are issues in the configuration, console warns are printed.
     *
     * @param   section
     *          Configuration section to load from
     * @return  Loaded instance from the given configuration section
     */
    @NotNull
    public static PlaceholderRefreshConfiguration fromSection(@NotNull ConfigurationSection section) {
        int defaultInterval = section.getInt("default-refresh-interval", 500);
        Map<String, Integer> refreshIntervals = new HashMap<>();
        for (Object placeholder : section.getKeys()) {
            String identifier = placeholder.toString();
            if (identifier.equals("default-refresh-interval")) continue;
            if (!identifier.startsWith("%") || !identifier.endsWith("%")) {
                section.startupWarn("PlaceholderAPI refresh intervals have a value for \"" + identifier + "\", which is not " +
                        "a valid placeholder pattern (placeholders must start and end with %)");
                continue;
            }
            refreshIntervals.put(identifier, fixInterval(section, identifier, defaultInterval));
        }
        
        return new PlaceholderRefreshConfiguration(defaultInterval, refreshIntervals);
    }
    
    private static int fixInterval(@NotNull ConfigurationSection section, @NotNull String identifier, int defaultInterval) {
        Object value = section.getObject(identifier);
        if (value == null) {
            section.startupWarn("Refresh interval of " + identifier +
                    " is set to null. Define a valid value or remove it if you don't want to override default value.");
            return Placeholder.MINIMUM_REFRESH_INTERVAL;
        }
        if (!(value instanceof Integer)) {
            section.startupWarn("Refresh interval configured for \"" + identifier + "\" is not a valid number (" + value.getClass().getSimpleName() + ").");
            return 500;
        }
        int interval = (int) value;
        if (interval == defaultInterval) {
            section.hint("Refresh interval of " + identifier + " is same as default interval, therefore there is no need to override it.");
            return (Integer) value;
        }
        if (interval == -1) {
            return (Integer) value;
        }
        if (interval <= 0) {
            section.startupWarn("Invalid refresh interval configured for " + identifier +
                    " (" + interval + "). Value cannot be zero or negative (except -1).");
            return defaultInterval;
        } else if (interval % Placeholder.MINIMUM_REFRESH_INTERVAL != 0) {
            section.startupWarn("Invalid refresh interval configured for " + identifier +
                    " (" + interval + "). Value must be divisible by " + Placeholder.MINIMUM_REFRESH_INTERVAL + ".");
            return defaultInterval;
        }
        return (int) value;
    }
}
