package me.neznamy.tab.shared.config.files.config;

import me.neznamy.tab.shared.TabConstants.Placeholder;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.files.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlaceholderRefreshConfiguration extends ConfigurationSection {

    private final String SECTION = "placeholderapi-refresh-intervals";
    public final int defaultInterval = getInt(SECTION + ".default-refresh-interval", 500);
    @NotNull public final Map<String, Integer> refreshIntervals = new HashMap<>();

    public PlaceholderRefreshConfiguration(@NotNull ConfigurationFile config) {
        super(config);
        for (Map.Entry<Object, Object> entry : getMap(SECTION, Collections.emptyMap()).entrySet()) {
            String identifier = entry.getKey().toString();
            if (identifier.equals("default-refresh-interval")) continue;
            if (!identifier.startsWith("%") || !identifier.endsWith("%")) {
                startupWarn("PlaceholderAPI refresh intervals have a value for \"" + identifier + "\", which is not " +
                        "a valid placeholder pattern (placeholders must start and end with %)");
                continue;
            }
            refreshIntervals.put(entry.getKey().toString(), fixInterval(entry));
        }
    }

    private int fixInterval(@NotNull Map.Entry<Object, Object> entry) {
        if (entry.getValue() == null) {
            startupWarn("Refresh interval of " + entry.getKey() +
                    " is set to null. Define a valid value or remove it if you don't want to override default value.");
            return Placeholder.MINIMUM_REFRESH_INTERVAL;
        }
        if (!(entry.getValue() instanceof Integer)) {
            startupWarn("Refresh interval configured for \"" + entry.getKey() + "\" is not a valid number (" + entry.getValue().getClass().getSimpleName() + ").");
            return 500;
        }
        int interval = (int) entry.getValue();
        if (interval == defaultInterval) {
            hint("Refresh interval of " + entry.getKey() + " is same as default interval, therefore there is no need to override it.");
            return (Integer) entry.getValue();
        }
        if (interval == -1) {
            return (Integer) entry.getValue();
        }
        if (interval <= 0) {
            startupWarn("Invalid refresh interval configured for " + entry.getKey() +
                    " (" + interval + "). Value cannot be zero or negative (except -1).");
            return defaultInterval;
        } else if (interval % Placeholder.MINIMUM_REFRESH_INTERVAL != 0) {
            startupWarn("Invalid refresh interval configured for " + entry.getKey() +
                    " (" + interval + "). Value must be divisible by " + Placeholder.MINIMUM_REFRESH_INTERVAL + ".");
            return defaultInterval;
        }
        return (int) entry.getValue();
    }
}
