package me.neznamy.tab.shared.features.playerlist;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Class storing configuration for tablist name formatting.
 */
@Getter
@RequiredArgsConstructor
public class TablistFormattingConfiguration {

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
    public static TablistFormattingConfiguration fromSection(@NotNull ConfigurationSection section) {
        // Check keys
        section.checkForUnknownKey(Arrays.asList("enabled", "disable-condition"));

        return new TablistFormattingConfiguration(section.getString("disable-condition", "%world%=disabledworld"));
    }
}
