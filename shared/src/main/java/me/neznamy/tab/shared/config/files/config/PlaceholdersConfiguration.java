package me.neznamy.tab.shared.config.files.config;

import lombok.NonNull;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.files.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;

public class PlaceholdersConfiguration extends ConfigurationSection {

    private final String SECTION = "placeholders";
    @NotNull public SimpleDateFormat dateFormat = parseDateFormat(getString(SECTION + ".date-format", "dd.MM.yyyy"), "dd.MM.yyyy");
    @NotNull public SimpleDateFormat timeFormat = parseDateFormat(getString(SECTION + ".time-format", "[HH:mm:ss / h:mm a]"), "[HH:mm:ss / h:mm a]");
    public double timeOffset = getNumber(SECTION + ".time-offset", 0).doubleValue();
    public boolean registerTabExpansion = getBoolean(SECTION + ".register-tab-expansion", false);

    public PlaceholdersConfiguration(@NotNull ConfigurationFile config) {
        super(config);
        checkForUnknownKey(SECTION, Arrays.asList("date-format", "time-format", "time-offset", "register-tab-expansion"));
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
    private SimpleDateFormat parseDateFormat(@NonNull String value, @NonNull String defaultValue) {
        try {
            return new SimpleDateFormat(value, Locale.ENGLISH);
        } catch (IllegalArgumentException e) {
            startupWarn("Format \"" + value + "\" is not a valid date/time format. Did you try to use color codes?");
            return new SimpleDateFormat(defaultValue);
        }
    }
}
