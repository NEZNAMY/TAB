package me.neznamy.tab.shared.config.files.config;

import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.files.ConfigurationSection;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class PingSpoofConfiguration extends ConfigurationSection {

    private final String SECTION = "ping-spoof";
    public final int value = getInt(SECTION + ".value", 0);

    public PingSpoofConfiguration(@NotNull ConfigurationFile config) {
        super(config);
        checkForUnknownKey(SECTION, Arrays.asList("enabled", "value"));
    }
}
