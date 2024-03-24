package me.neznamy.tab.shared.features.layout.skin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import me.neznamy.tab.shared.config.file.ConfigurationFile;

/**
 * Abstract class for skin sources for getting skins.
 */
public abstract class SkinSource {

    /** Configuration file for storing cache */
    @NotNull private final ConfigurationFile file;

    /** Cache path of this source */
    @NotNull private final String path;

    /** Current cache of this source */
    @NotNull private final Map<String, List<String>> cache;

    protected SkinSource(@NotNull ConfigurationFile file, @NotNull String path) {
        this.file = file;
        this.path = path;
        cache = file.getConfigurationSection(path);
    }

    /**
     * Returns skin using given skin definition.
     *
     * @param   skin
     *          Skin definition
     * @return  Skin from definition or empty list if invalid
     */
    @NotNull
    public List<String> getSkin(@NotNull String skin) {
        if (cache.containsKey(skin)) {
            return cache.get(skin);
        }
        List<String> properties = download(skin);
        if (!properties.isEmpty()) {
            cache.put(skin, properties);
            file.set(path, cache);
            return properties;
        }
        return properties;
    }

    /**
     * Downloads skin with given skin definition.
     *
     * @param   input
     *          Skin definition
     * @return  Downloaded skin or empty list if invalid
     */
    @NotNull
    public abstract List<String> download(@NotNull String input);

    @NotNull
    protected JSONObject getResponse(@NotNull String url) throws IOException, ParseException {
        try (InputStreamReader reader = new InputStreamReader(new URL(url).openStream())) {
            return (JSONObject) new JSONParser().parse(reader);
        }
    }
}
