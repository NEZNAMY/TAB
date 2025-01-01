package me.neznamy.tab.shared.features.belowname;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
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
                    "due to a minecraft limitation. Placeholders will be parsed for viewing player.");
        }

        // Check for empty value
        String value = section.getString("value", TabConstants.Placeholder.HEALTH);
        if (value.isEmpty()) {
            section.startupWarn("Belowname value is set to be empty, but the configured value must evaluate to a number. Using 0.");
            value = "0";
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
