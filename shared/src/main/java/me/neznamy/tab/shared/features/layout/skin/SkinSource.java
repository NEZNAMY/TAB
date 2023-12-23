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

public abstract class SkinSource {

    @NotNull private final ConfigurationFile file;
    @NotNull private final String path;
    @NotNull private final Map<String, List<String>> cache;

    protected SkinSource(@NotNull ConfigurationFile file, @NotNull String path) {
        this.file = file;
        this.path = path;
        cache = file.getConfigurationSection(path);
    }

    public @NotNull List<String> getSkin(@NotNull String skin) {
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

    public abstract @NotNull List<String> download(@NotNull String input);

    protected @NotNull JSONObject getResponse(@NotNull String url) throws IOException, ParseException {
        try (InputStreamReader reader = new InputStreamReader(new URL(url).openStream())) {
            return (JSONObject) new JSONParser().parse(reader);
        }
    }
}
