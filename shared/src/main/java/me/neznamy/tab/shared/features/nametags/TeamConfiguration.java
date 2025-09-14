package me.neznamy.tab.shared.features.nametags;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Class storing teams feature configuration.
 */
@Getter
@RequiredArgsConstructor
public class TeamConfiguration {

    @NotNull private final String enableCollision;
    @NotNull private final String invisibleNameTags;
    private final boolean canSeeFriendlyInvisibles;
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
    public static TeamConfiguration fromSection(@NotNull ConfigurationSection section) {
        // Check keys
        section.checkForUnknownKey(Arrays.asList("enabled", "enable-collision", "invisible-nametags", "sorting-types",
                "case-sensitive-sorting", "can-see-friendly-invisibles", "disable-condition"));

        return new TeamConfiguration(
                section.getObject("enable-collision", "true").toString(),
                section.getObject("invisible-nametags", "false").toString(),
                section.getBoolean("can-see-friendly-invisibles", false),
                section.getString("disable-condition", "%world%=disabledworld")
        );
    }
}
