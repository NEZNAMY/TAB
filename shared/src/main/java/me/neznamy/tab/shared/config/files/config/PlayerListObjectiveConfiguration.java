package me.neznamy.tab.shared.config.files.config;

import me.neznamy.tab.shared.TabConstants.Placeholder;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.files.ConfigurationSection;
import me.neznamy.tab.shared.platform.Scoreboard.HealthDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class PlayerListObjectiveConfiguration extends ConfigurationSection {

    private final String SECTION = "playerlist-objective";
    @NotNull public final String value = getString(SECTION + ".value", Placeholder.PING);
    @NotNull public final String fancyValue = getString(SECTION + ".fancy-value", "&7Ping: " + Placeholder.PING);
    @Nullable public final String disableCondition = getString(SECTION + ".disable-condition", "%world%=disabledworld");
    @NotNull public final HealthDisplay healthDisplay = Arrays.asList(Placeholder.HEALTH, "%player_health%", "%player_health_rounded%").contains(value)
            ? HealthDisplay.HEARTS : HealthDisplay.INTEGER;

    public PlayerListObjectiveConfiguration(@NotNull ConfigurationFile config) {
        super(config);
        checkForUnknownKey(SECTION, Arrays.asList("enabled", "value", "fancy-value", "disable-condition"));
    }
}
