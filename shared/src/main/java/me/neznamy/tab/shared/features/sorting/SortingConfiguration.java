package me.neznamy.tab.shared.features.sorting;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Class for storing sorting configuration section.
 */
@Getter
@RequiredArgsConstructor
public class SortingConfiguration {

    private final boolean caseSensitiveSorting;
    private final List<String> sortingTypes;

    /**
     * Returns instance of this class created from given configuration section. If there are
     * issues in the configuration, console warns are printed.
     *
     * @param   section
     *          Configuration section to load from
     * @return  Loaded instance from given configuration section
     */
    @NotNull
    public static SortingConfiguration fromSection(@NotNull ConfigurationSection section) {
        return new SortingConfiguration(
                section.getBoolean("case-sensitive-sorting", true),
                section.getStringList("sorting-types", Collections.emptyList())
        );
    }
}
