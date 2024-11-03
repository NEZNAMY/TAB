package me.neznamy.tab.shared.features.pingspoof;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * This class represents ping spoof configuration settings.
 */
@Getter
@RequiredArgsConstructor
public class PingSpoofConfiguration {

    private final int value;

    /**
     * Returns instance of this class created from given configuration section. If there are
     * issues in the configuration, console warns are printed.
     *
     * @param   section
     *          Configuration section to load from
     * @return  Loaded instance from given configuration section
     */
    @NotNull
    public static PingSpoofConfiguration fromSection(@NotNull ConfigurationSection section) {
        // Check keys
        section.checkForUnknownKey(Arrays.asList("enabled", "value"));

        return new PingSpoofConfiguration(section.getInt("value", 0));
    }
}
