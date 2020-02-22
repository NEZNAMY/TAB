package me.neznamy.tab.shared;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.google.common.collect.Lists;

import me.neznamy.tab.shared.placeholders.Placeholders;

@SuppressWarnings("unchecked")
public class ConfigurationFile{
	
	public static final File dataFolder = new File("plugins" + File.separatorChar + "TAB");
	
	private File file;
	private Yaml yaml;
	private List<String> header;
	private Map<String, Object> values;
	
	public ConfigurationFile(String source, String destination, List<String> header) throws Exception{
		FileInputStream input = null;
		try {
			this.header = header;
			dataFolder.mkdirs();
			file = new File(dataFolder, destination);
			if (!file.exists()) Files.copy(getClass().getClassLoader().getResourceAsStream("resources/" + source), file.toPath());
			DumperOptions options = new DumperOptions();
			options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			yaml = new Yaml(options);
			input = new FileInputStream(file);
			values = yaml.load(new InputStreamReader(input, Charset.forName("UTF-8")));
			if (values == null) values = new HashMap<String, Object>();
			input.close();
			Shared.mainClass.convertConfig(this);
			if (!hasHeader()) fixHeader();
			detectPlaceholders(values);
		} catch (Exception e) {
			input.close();
			Shared.errorManager.startupWarn("File " + destination + " has broken formatting.");
			Shared.mainClass.sendConsoleMessage("&6[TAB] Error message from yaml parser: " + e.getMessage());
			String fix = Shared.errorManager.suggestYamlFix(e, readAllLines());
			if (fix != null) {
				Shared.mainClass.sendConsoleMessage("&d[TAB] Suggestion: " + fix);
			}
			throw e;
		}
	}
	public ConfigurationFile(String sourceAndDestination, List<String> header) throws Exception{
		this(sourceAndDestination, sourceAndDestination, header);
	}
	private void detectPlaceholders(Map<String, Object> map) {
		for (Entry<String, Object> entry : map.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof String) {
				for (String placeholder : Placeholders.detectAll((String) value)) {
					if (!Placeholders.usedPlaceholders.contains(placeholder)) Placeholders.usedPlaceholders.add(placeholder);
				}
			}
			if (value instanceof Map) detectPlaceholders((Map<String, Object>) value);
			if (value instanceof List) {
				for (Object line : (List<Object>)value) {
					for (String placeholder : Placeholders.detectAll(line+"")) {
						if (!Placeholders.usedPlaceholders.contains(placeholder)) Placeholders.usedPlaceholders.add(placeholder);
					}
				}
			}
		}
	}
	public String getName() {
		return file.getName();
	}
	public Map<String, Object> getValues(){
		return values;
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
		if (value == null) return defaultValue;
		return value+"";
	}
	public List<String> getStringList(String path) {
		return getStringList(path, null);
	}
	public List<String> getStringList(String path, List<String> defaultValue) {
		Object value = get(path, defaultValue);
		if (value == null) return defaultValue;
		if (!(value instanceof List)) {
			dataMismatch(path, "ArrayList", value.getClass().getSimpleName());
			return new ArrayList<String>();
		}
		List<String> fixedList = new ArrayList<String>();
		for (Object key : (List<Object>)value) {
			fixedList.add(key+"");
		}
		return fixedList;
	}
	
	
	public int getInt(String path, int defaultValue) {
		Object value = get(path, defaultValue);
		if (value == null) return defaultValue;
		try{
			return Integer.parseInt(value+"");
		} catch (Exception e) {
			dataMismatch(path, "Integer", value.getClass().getSimpleName());
			return defaultValue;
		}
	}
	
	
	public boolean getBoolean(String path, boolean defaultValue) {
		Object value = get(path, defaultValue);
		if (value == null) return defaultValue;
		try{
			return Boolean.parseBoolean(value+"");
		} catch (Exception e) {
			dataMismatch(path, "Boolean", value.getClass().getSimpleName());
			return defaultValue;
		}
	}
	
	
	public double getDouble(String path, double defaultValue) {
		Object value = get(path, defaultValue);
		if (value == null) return defaultValue;
		try{
			return Double.parseDouble(value+"");
		} catch (Exception e) {
			dataMismatch(path, "Double", value.getClass().getSimpleName());
			return defaultValue;
		}
	}
	
	
	private void dataMismatch(String path, String expected, String found) {
		Shared.errorManager.startupWarn("Data mismatch in &e" + file.getName() + "&c. Value of &e" + path + "&c is expected to be &e" + expected + "&c, but is &e" + found + "&c. This is a misconfiguration issue.");
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
			if (value == null) {
				map.remove(path);
			} else {
				map.put(path, value);
			}
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
			fixHeader();
		} catch (Throwable e) {
			Shared.errorManager.criticalError("Failed to save yaml file " + file.getPath(), e);
		}
	}
	public boolean hasHeader() {
		if (header == null) return true;
		for (String line : readAllLines()) {
			if (line.startsWith("#")) return true;
		}
		return false;
	}
	public void fixHeader() {
		if (header == null) return;
		try {
			List<String> content = Lists.newArrayList(header);
			content.addAll(readAllLines());
			file.delete();
			file.createNewFile();
			BufferedWriter buf = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
			for (String line : content) {
				buf.write(line + System.getProperty("line.separator"));
			}
			buf.close();
		} catch (Exception ex) {
			Shared.errorManager.criticalError("Failed to modify file " + file, ex);
		}
	}
	private List<String> readAllLines() {
		List<String> list = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			String line;
			while ((line = br.readLine()) != null) {
				list.add(line);
			}
			br.close();
		} catch (Exception ex) {
			Shared.errorManager.criticalError("Failed to read file " + file, ex);
		}
		return list;
	}
}