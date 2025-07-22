package me.neznamy.tab.shared.features.belowname;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Class for storing belowname configuration settings.
 */
@Getter
@AllArgsConstructor
public class BelowNameConfiguration {

    @NotNull private final String value;
    @NotNull private final String title;
    @NotNull private final String fancyValue;
    @NotNull private final String fancyValueDefault;
    @NotNull private final String disableCondition;

    /**
     * Returns instance of this class created from given configuration section. If there are
     * issues in the configuration, console warns are printed.
     *
     * @param   section
     *          Configuration section to load from
     * @return  Loaded instance from given configuration section
     */
    @NotNull
    public static BelowNameConfiguration fromSection(@NotNull ConfigurationSection section) {
        // Check keys
        section.checkForUnknownKey(Arrays.asList("enabled", "value", "title", "fancy-value-default", "fancy-value", "disable-condition"));

        // Check placeholders in title
        String title = section.getString("title", "Health");
        if (title.contains("%") && !title.contains("%animation") && !title.contains("%condition")) {
            section.startupWarn("Belowname title is set to \"" + title + "\", however, the feature cannot display different title on different players " +
                    "due to a minecraft limitation. Placeholders will be parsed for viewing player. To display per-player placeholders correctly, " +
                    "move them to fancy-value and only keep static text in title (this only works on 1.20.3+, on older versions you will need to " +
                    "use \"value\", which only supports numbers).");
        }

        // Check "value" for empty value
        String value = section.getObject("value", TabConstants.Placeholder.HEALTH).toString(); // Support both String and Integer
        if (value.isEmpty()) {
            section.startupWarn("Belowname value is set to be empty, but the configured value must evaluate to a number. Using 0.");
            value = "0";
        }

        // Check "value" for forced non-numeric input
        String strippedValue = value;
        for (String placeholder : PlaceholderManagerImpl.detectPlaceholders(strippedValue)) {
            strippedValue = strippedValue.replace(placeholder, "");
        }
        if (!strippedValue.isEmpty()) { // Empty value is fine; it means only a placeholder is used
            try {
                Integer.parseInt(strippedValue);
            } catch (NumberFormatException e) {
                section.startupWarn("\"value\" is set to \"" + value + "\", but this will never evaluate to a number. " +
                        "If you want text without limits, update to 1.20.3+ and use fancy-value. If you already did, set \"value\" to 0 as it is not displayed anyway.");
            }
        }

        return new BelowNameConfiguration(
                value,
                title,
                section.getString("fancy-value", "&c" + TabConstants.Placeholder.HEALTH),
                section.getString("fancy-value-default", "NPC"),
                section.getString("disable-condition", "%world%=disabledworld")
        );
    }
}
