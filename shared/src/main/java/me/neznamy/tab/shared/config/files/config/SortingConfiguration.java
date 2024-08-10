package me.neznamy.tab.shared.config.files.config;

import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.files.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class SortingConfiguration extends ConfigurationSection {

    private final String SECTION = "scoreboard-teams";
    public final boolean caseSensitiveSorting = getBoolean(SECTION + ".case-sensitive-sorting", true);
    public final List<String> sortingTypes = getStringList("scoreboard-teams.sorting-types", Collections.emptyList());

    public SortingConfiguration(@NotNull ConfigurationFile config) {
        super(config);
    }
}
