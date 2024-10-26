package me.neznamy.tab.shared.config.files.config;

import me.neznamy.tab.shared.TabConstants.Placeholder;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.files.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class BelownameConfiguration extends ConfigurationSection {

    private final String SECTION = "belowname-objective";
    public final boolean enabled = getBoolean(SECTION + ".enabled", false);
    @NotNull public final String value = getString(SECTION + ".value", Placeholder.HEALTH);
    @NotNull public final String title = getString(SECTION + ".title", "Health");
    @NotNull public final String fancyValue = getString(SECTION + ".fancy-value", "&c" + Placeholder.HEALTH);
    @NotNull public final String fancyValueDefault = getString(SECTION + ".fancy-value-default", "NPC");
    @Nullable public final String disableCondition = getString(SECTION + ".disable-condition", "%world%=disabledworld");

    public BelownameConfiguration(@NotNull ConfigurationFile config) {
        super(config);
        checkForUnknownKey(SECTION, Arrays.asList("enabled", "value", "title", "fancy-value-default", "fancy-value", "disable-condition"));

        if (!title.contains("%") || title.contains("%animation") || title.contains("%condition")) return;
        startupWarn("Belowname text is set to " + title + ", however, the feature cannot display different text on different players " +
                "due to a minecraft limitation. Placeholders will be parsed for viewing player.");
    }
}
