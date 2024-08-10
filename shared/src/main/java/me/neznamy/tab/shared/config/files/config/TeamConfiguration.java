package me.neznamy.tab.shared.config.files.config;

import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.files.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class TeamConfiguration extends ConfigurationSection {

    private final String SECTION = "scoreboard-teams";
    @Nullable public final String enableCollision = getObject(SECTION + ".enable-collision", "true").toString();
    public final boolean invisibleNameTags = getBoolean(SECTION + ".invisible-nametags", false);
    public final boolean antiOverride = getBoolean(SECTION + ".anti-override", true);
    public final boolean canSeeFriendlyInvisibles = getBoolean(SECTION + ".can-see-friendly-invisibles", false);
    @Nullable public final String disableCondition = getString(SECTION + ".disable-condition", "%world%=disabledworld");

    public TeamConfiguration(@NotNull ConfigurationFile config) {
        super(config);
        checkForUnknownKey(SECTION, Arrays.asList("enabled", "enable-collision", "invisible-nametags", "anti-override", "sorting-types",
                        "case-sensitive-sorting", "can-see-friendly-invisibles", "disable-condition"));

        if (!antiOverride) {
            startupWarn("anti-override for scoreboard-teams is disabled in config. This is usually a mistake. If you notice the feature randomly breaking, enable it back.");
        }
    }
}
