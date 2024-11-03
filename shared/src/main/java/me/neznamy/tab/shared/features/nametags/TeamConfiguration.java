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
    private final boolean invisibleNameTags;
    private final boolean antiOverride;
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
        section.checkForUnknownKey(Arrays.asList("enabled", "enable-collision", "invisible-nametags", "anti-override", "sorting-types",
                "case-sensitive-sorting", "can-see-friendly-invisibles", "disable-condition"));

        // Check anti-override
        boolean antiOverride = section.getBoolean("anti-override", true);
        if (!antiOverride) {
            section.startupWarn("anti-override for scoreboard-teams is disabled in config. This is usually a mistake. If you notice the feature randomly breaking, enable it back.");
        }

        return new TeamConfiguration(
                section.getObject("enable-collision", "true").toString(),
                section.getBoolean("invisible-nametags", false),
                antiOverride,
                section.getBoolean("can-see-friendly-invisibles", false),
                section.getString("disable-condition", "%world%=disabledworld")
        );
    }
}
