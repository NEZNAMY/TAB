package me.neznamy.tab.shared.placeholders;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Class holding placeholder output replacements configuration.
 */
@Getter
@RequiredArgsConstructor
public class PlaceholderReplacementsConfiguration {

    @NotNull private final Map<String, Map<Object, Object>> values;

    /**
     * Returns instance of this class created from given configuration section. If there are
     * issues in the configuration, console warns are printed.
     *
     * @param   section
     *          Configuration section to load from
     * @return  Loaded instance from given configuration section
     */
    @NotNull
    public static PlaceholderReplacementsConfiguration fromSection(@NotNull ConfigurationSection section) {
        Map<String, Map<Object, Object>> values = new HashMap<>();
        for (Object key : section.getKeys()) {
            String identifier = key.toString();
            Map<Object, Object> map = section.getMap(identifier, Collections.emptyMap());
            if (!identifier.startsWith("%") || !identifier.endsWith("%")) {
                section.startupWarn("Placeholder output replacements have a section for \"" + identifier + "\", which is not " +
                        "a valid placeholder pattern (placeholders must start and end with %)");
                continue;
            }
            for (Map.Entry<?, ?> pattern : map.entrySet()) {
                if (pattern.getKey().equals("else") && pattern.getValue().equals(identifier)) {
                    section.hint(String.format("Placeholder %s has configured \"else -> %s\" replacement pattern, but this is already the default behavior " +
                            "and therefore this pattern can be removed.", identifier, identifier));
                }
            }
            values.put(identifier, map);
        }

        return new PlaceholderReplacementsConfiguration(values);
    }
}
