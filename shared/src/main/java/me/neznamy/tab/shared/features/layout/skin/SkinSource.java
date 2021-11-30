package me.neznamy.tab.shared.features.layout.skin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import me.neznamy.tab.api.config.ConfigurationFile;

public abstract class SkinSource {

	private final ConfigurationFile file;
	private final String path;
	private final Map<String, List<String>> cache;
	
	protected SkinSource(ConfigurationFile file, String path) {
		this.file = file;
		this.path = path;
		this.cache = file.getConfigurationSection(path);
	}
	
	public List<String> getSkin(String skin) {
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
	
	public abstract List<String> download(String input);
	
	protected JSONObject getResponse(String url) throws IOException, ParseException {
		try (InputStreamReader reader = new InputStreamReader(new URL(url).openStream())){
			return (JSONObject) new JSONParser().parse(reader);
		}
	}
}
