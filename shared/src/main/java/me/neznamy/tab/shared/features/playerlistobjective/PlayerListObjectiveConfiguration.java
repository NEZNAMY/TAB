package me.neznamy.tab.shared.features.playerlistobjective;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TabConstants.Placeholder;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import me.neznamy.tab.shared.platform.Scoreboard.HealthDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Class storing playerlist objective configuration section.
 */
@Getter
@RequiredArgsConstructor
public class PlayerListObjectiveConfiguration {

    @NotNull private final String value;
    @NotNull private final String fancyValue;
    @NotNull private final String title;
    @NotNull private final String disableCondition;
    @NotNull private final HealthDisplay healthDisplay;

    /**
     * Returns instance of this class created from given configuration section. If there are
     * issues in the configuration, console warns are printed.
     *
     * @param   section
     *          Configuration section to load from
     * @return  Loaded instance from given configuration section
     */
    @NotNull
    public static PlayerListObjectiveConfiguration fromSection(@NotNull ConfigurationSection section) {
        // Check keys
        section.checkForUnknownKey(Arrays.asList("enabled", "value", "fancy-value", "title", "render-type", "disable-condition"));

        // Check for empty value
        String value = section.getString("value", Placeholder.PING);
        if (value.isEmpty()) {
            section.startupWarn("Playerlist objective value is set to be empty, but the configured value must evaluate to a number. Using 0.");
            value = "0";
        }

        // Check the render type
        String renderTypeString = section.getString("render-type", "INTEGER");
        HealthDisplay healthDisplay;
        try {
            healthDisplay = HealthDisplay.valueOf(renderTypeString);
        } catch (IllegalArgumentException e) {
            section.startupWarn("\"" + renderTypeString + "\" is not a valid render type. Valid options are: " +
                    Arrays.deepToString(HealthDisplay.values()) + ". Using INTEGER");
            healthDisplay = HealthDisplay.INTEGER;
        }

        return new PlayerListObjectiveConfiguration(
                value,
                section.getString("fancy-value", "&7Ping: " + Placeholder.PING),
                section.getString("title", "Java Edition is better"),
                section.getString("disable-condition", "%world%=disabledworld"),
                healthDisplay
        );
    }
}
