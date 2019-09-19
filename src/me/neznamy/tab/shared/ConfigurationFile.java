package me.neznamy.tab.shared;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

@SuppressWarnings("unchecked")
public class ConfigurationFile{
	
	public static final File dataFolder = new File("plugins" + System.getProperty("file.separator") + "TAB");
	
	private File file;
	private Yaml yaml;
	public Map<String, Object> values;
	
	public ConfigurationFile(String source, String destination) throws Exception{
		FileInputStream input = null;
		try {
			dataFolder.mkdirs();
			file = new File(dataFolder, destination);
			if (!file.exists()) Files.copy(getClass().getClassLoader().getResourceAsStream(source), file.toPath());
			DumperOptions options = new DumperOptions();
			options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			yaml = new Yaml(options);
			input = new FileInputStream(file);
			values = yaml.load(new InputStreamReader(input, Charset.forName("UTF-8")));
			if (values == null) values = new HashMap<String, Object>();
			input.close();
		} catch (Exception e) {
			input.close();
			Shared.startupWarn("File " + destination + " has broken formatting.");
			Shared.print("§6", "Error message: " + e.getMessage());
			throw e;
		}
	}
	public ConfigurationFile(String sourceAndDestination) throws Exception{
		this(sourceAndDestination, sourceAndDestination);
	}
	public Object get(String path) {
		return get(path, null);
	}
	public Object get(String path, Object defaultValue) {
		try {
			Object value = values;
			for (String tab : path.split("\\.")) {
				value = getIgnoreCase((Map<String, Object>) value, tab);
			}
			if (value == null && defaultValue != null) {
				set(path, defaultValue);
				return defaultValue;
			}
			return value;
		} catch (Throwable e) {
			if (defaultValue != null) set(path, defaultValue);
			return defaultValue;
		}
	}
	private Object getIgnoreCase(Map<String, Object> map, String key) {
		for (String mapkey : map.keySet()) {
			if (mapkey.equalsIgnoreCase(key)) return map.get(mapkey);
		}
		return null;
	}
	public String getString(String path) {
		return getString(path, null);
	}
	public String getString(String path, String defaultValue) {
		Object value = get(path, defaultValue);
		if (value == null) return null;
		return value+"";
	}
	public List<Object> getList(String path) {
		return getList(path, null);
	}
	public List<Object> getList(String path, List<String> defaultValue) {
		return (List<Object>) get(path, defaultValue);
	}
	public List<String> getStringList(String path) {
		return getStringList(path, null);
	}
	public List<String> getStringList(String path, List<String> defaultValue) {
		return (List<String>) get(path, defaultValue);
	}
	public int getInt(String path, int defaultValue) {
		return Integer.parseInt(get(path, defaultValue)+"");
	}
	public int getInt(String path) {
		return Integer.parseInt(get(path, 0)+"");
	}
	public boolean getBoolean(String path) {
		return Boolean.parseBoolean(get(path, null)+"");
	}
	public boolean getBoolean(String path, boolean defaultValue) {
		return Boolean.parseBoolean(get(path, defaultValue)+"");
	}
	public double getDouble(String path, double defaultValue) {
		return Double.parseDouble(get(path, defaultValue)+"");
	}
	public void set(String path, Object value) {
		set(values, path, value);
		save();
	}
	private Map<String, Object> set(Map<String, Object> map, String path, Object value) {
		if (path.contains(".")) {
			String keyWord = path.split("\\.")[0];
			Object submap = map.get(keyWord);
			if (submap == null || !(submap instanceof Map)) {
				submap = new HashMap<String, Object>();
			}
			map.put(keyWord, set((Map<String, Object>) submap, path.substring(keyWord.length()+1, path.length()), value));
		} else {
			map.put(path, value);
		}
		return map;
	}
	public Map<String, Object> getConfigurationSection(String path) {
		if (path == null || path.length() == 0) return values;
		return ((Map<String, Object>)get(path, null));
	}
	public void save() {
		try {
			Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
			yaml.dump(values, writer);
			writer.close();
		} catch (Throwable e) {
			Shared.error("Failed to save yaml file " + file.getPath(), e);
		}
	}
}