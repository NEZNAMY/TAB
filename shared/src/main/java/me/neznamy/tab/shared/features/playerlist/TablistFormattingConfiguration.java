package me.neznamy.tab.shared.features.playerlist;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Class storing configuration for tablist name formatting.
 */
@Getter
@RequiredArgsConstructor
public class TablistFormattingConfiguration {

    private final boolean antiOverride;
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
        section.checkForUnknownKey(Arrays.asList("enabled", "anti-override", "disable-condition"));
        
        // Check anti-override
        boolean antiOverride = section.getBoolean("anti-override", true);
        if (!antiOverride) {
            TAB.getInstance().getConfigHelper().startup().startupWarn("anti-override for tablist-name-formatting is disabled in config. " +
                    "This is usually a mistake. If you notice the feature randomly breaking, enable it back.");
        }
        
        return new TablistFormattingConfiguration(antiOverride, section.getString("disable-condition", "%world%=disabledworld"));
    }
}
