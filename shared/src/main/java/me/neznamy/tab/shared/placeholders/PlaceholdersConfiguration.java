package me.neznamy.tab.shared.placeholders;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;

/**
 * This class represents "placeholders" configuration section.
 */
@Getter
@RequiredArgsConstructor
public class PlaceholdersConfiguration {

    @NotNull private final SimpleDateFormat dateFormat;
    @NotNull private final SimpleDateFormat timeFormat;
    private final double timeOffset;
    private final boolean registerTabExpansion;

    /**
     * Returns instance of this class created from given configuration section. If there are
     * issues in the configuration, console warns are printed.
     *
     * @param   section
     *          Configuration section to load from
     * @return  Loaded instance from given configuration section
     */
    @NotNull
    public static PlaceholdersConfiguration fromSection(@NotNull ConfigurationSection section) {
        // Check keys
        section.checkForUnknownKey(Arrays.asList("date-format", "time-format", "time-offset", "register-tab-expansion"));

        return new PlaceholdersConfiguration(
                parseDateFormat(section.getString("date-format", "dd.MM.yyyy"), "dd.MM.yyyy"),
                parseDateFormat(section.getString("time-format", "[HH:mm:ss / h:mm a]"), "[HH:mm:ss / h:mm a]"),
                section.getNumber("time-offset", 0).doubleValue(),
                section.getBoolean("register-tab-expansion", false)
        );
    }

    /**
     * Evaluates inserted date format. If it's not valid, a message is printed into console
     * and format with {@code defaultValue} is returned.
     *
     * @param   value
     *          date format to evaluate
     * @param   defaultValue
     *          value to use if entered format is not valid
     * @return  evaluated date format
     */
    private static SimpleDateFormat parseDateFormat(@NonNull String value, @NonNull String defaultValue) {
        try {
            return new SimpleDateFormat(value, Locale.ENGLISH);
        } catch (IllegalArgumentException e) {
            TAB.getInstance().getConfigHelper().startup().startupWarn("Format \"" + value + "\" is not a valid date/time format. Did you try to use color codes?");
            return new SimpleDateFormat(defaultValue);
        }
    }
}
