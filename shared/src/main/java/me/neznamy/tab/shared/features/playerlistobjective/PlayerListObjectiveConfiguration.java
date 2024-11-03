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
        section.checkForUnknownKey(Arrays.asList("enabled", "value", "fancy-value", "disable-condition"));

        // Check for empty value
        String value = section.getString("value", Placeholder.PING);
        if (value.isEmpty()) {
            section.startupWarn("Playerlist objective value is set to be empty, but the configured value must evaluate to a number. Using 0.");
            value = "0";
        }

        return new PlayerListObjectiveConfiguration(
                value,
                section.getString("fancy-value", "&7Ping: " + Placeholder.PING),
                section.getString("disable-condition", "%world%=disabledworld"),
                Arrays.asList(Placeholder.HEALTH, "%player_health%", "%player_health_rounded%").contains(value) ? HealthDisplay.HEARTS : HealthDisplay.INTEGER
        );
    }
}
