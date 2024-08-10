package me.neznamy.tab.shared.config.files.config;

import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.files.ConfigurationSection;
import me.neznamy.tab.shared.placeholders.PlaceholderReplacementPattern;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PlaceholderReplacementsConfiguration extends ConfigurationSection {

    private final String SECTION = "placeholder-output-replacements";
    @NotNull public final Map<String, Map<Object, Object>> raw = new HashMap<>();
    @NotNull public final Map<String, PlaceholderReplacementPattern> compiled = new HashMap<>();

    public PlaceholderReplacementsConfiguration(@NotNull ConfigurationFile config) {
        super(config);
        for (Map.Entry<Object, Object> entry : getMap(SECTION, Collections.emptyMap()).entrySet()) {
            String identifier = entry.getKey().toString();
            Map<Object, Object> map = getMap(new String[]{SECTION, identifier}, Collections.emptyMap());
            if (!identifier.startsWith("%") || !identifier.endsWith("%")) {
                startupWarn("Placeholder output replacements have a section for \"" + identifier + "\", which is not " +
                        "a valid placeholder pattern (placeholders must start and end with %)");
                continue;
            }
            for (Map.Entry<?, ?> pattern : map.entrySet()) {
                if (pattern.getKey().equals("else") && pattern.getValue().equals(identifier)) {
                    hint(String.format("Placeholder %s has configured \"else -> %s\" replacement pattern, but this is already the default behavior " +
                            "and therefore this pattern can be removed.", identifier, identifier));
                }
            }
            raw.put(identifier, map);
            compiled.put(identifier, PlaceholderReplacementPattern.create(identifier, map));
        }
    }
}
