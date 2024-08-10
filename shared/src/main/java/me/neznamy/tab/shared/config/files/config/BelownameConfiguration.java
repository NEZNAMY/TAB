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
    @NotNull public final String number = getString(SECTION + ".number", Placeholder.HEALTH);
    @NotNull public final String text = getString(SECTION + ".text", "Health");
    @NotNull public final String fancyDisplayDefault = getString(SECTION + ".fancy-display-default", "NPC");
    @NotNull public final String fancyDisplayPlayers = getString(SECTION + ".fancy-display-players", "&c" + Placeholder.HEALTH);
    @Nullable public final String disableCondition = getString(SECTION + ".disable-condition", "%world%=disabledworld");

    public BelownameConfiguration(@NotNull ConfigurationFile config) {
        super(config);
        checkForUnknownKey(SECTION, Arrays.asList("enabled", "number", "text", "fancy-display-default", "fancy-display-players", "disable-condition"));

        if (!text.contains("%") || text.contains("%animation") || text.contains("%condition")) return;
        startupWarn("Belowname text is set to " + text + ", however, the feature cannot display different text on different players " +
                "due to a minecraft limitation. Placeholders will be parsed for viewing player.");
    }
}
