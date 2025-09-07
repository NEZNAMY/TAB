package me.neznamy.tab.shared.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Class holding configuration for chat components.
 */
@Getter
@RequiredArgsConstructor
public class ComponentConfiguration {

    /** Whether MiniMessage should be used if available or not */
    private final boolean minimessageSupport;

    /** Whether to automatically disable shadows for head components or not */
    private final boolean disableShadowForHeads;

    /**
     * Returns instance of this class created from given configuration section. If there are
     * issues in the configuration, console warns are printed.
     *
     * @param   section
     *          Configuration section to load from
     * @return  Loaded instance from given configuration section
     */
    @NotNull
    public static ComponentConfiguration fromSection(@NotNull ConfigurationSection section) {
        // Check keys
        section.checkForUnknownKey(Arrays.asList("minimessage-support", "disable-shadow-for-heads"));

        return new ComponentConfiguration(
                section.getBoolean("minimessage-support", true),
                section.getBoolean("disable-shadow-for-heads", true)
        );
    }
}
