package me.neznamy.tab.shared.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * Abstract class for configuration file
 */
@SuppressWarnings("unchecked")
public abstract class ConfigurationFile {

	protected List<String> header;
	protected Map<String, Object> values;
	protected File file;
	
	public ConfigurationFile(File dataFolder, String source, String destination, List<String> header) throws IOException {
		this.header = header;
		dataFolder.mkdirs();
		file = new File(dataFolder, destination);
		if (!file.exists()) {
			Files.copy(getClass().getClassLoader().getResourceAsStream("resources/" + source), file.toPath());
		}
	}
	
	public abstract void save();
	
	public String getName() {
		return file.getName();
	}
	
	public Map<String, Object> getValues(){
		return values;
	}
	
	public Object getObject(String path) {
		return getObject(path, null);
	}
	
	public Object getObject(String path, Object defaultValue) {
		try {
			Object value = values;
			for (String tab : path.split("\\.")) {
				tab = tab.replace("@#@", ".");
				value = getIgnoreCase((Map<Object, Object>) value, tab);
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
	
	private Object getIgnoreCase(Map<Object, Object> map, String key) {
		for (Object mapkey : map.keySet()) {
			if (mapkey.toString().equalsIgnoreCase(key)) return map.get(mapkey);
		}
		return null;
	}
	
	public String getString(String path) {
		return getString(path, null);
	}
	
	public String getString(String path, String defaultValue) {
		Object value = getObject(path, defaultValue);
		if (value == null) return defaultValue;
		return value+"";
	}
	
	public List<String> getStringList(String path) {
		return getStringList(path, null);
	}
	
	public List<String> getStringList(String path, List<String> defaultValue) {
		Object value = getObject(path, defaultValue);
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
	
	public boolean hasConfigOption(String path) {
		return getObject(path) != null;
	}
	
	public Integer getInt(String path) {
		return getInt(path, null);
	}
	
	public Integer getInt(String path, Integer defaultValue) {
		Object value = getObject(path, defaultValue);
		if (value == null) return defaultValue;
		try{
			return Integer.parseInt(value+"");
		} catch (Exception e) {
			dataMismatch(path, "Integer", value.getClass().getSimpleName());
			return defaultValue;
		}
	}
	
	public Boolean getBoolean(String path) {
		return getBoolean(path, null);
	}
	
	public Boolean getBoolean(String path, Boolean defaultValue) {
		Object value = getObject(path, defaultValue);
		if (value == null) return defaultValue;
		try{
			return Boolean.parseBoolean(value+"");
		} catch (Exception e) {
			dataMismatch(path, "Boolean", value.getClass().getSimpleName());
			return defaultValue;
		}
	}
	
	public Double getDouble(String path, double defaultValue) {
		Object value = getObject(path, defaultValue);
		if (value == null) return defaultValue;
		try{
			return Double.parseDouble(value+"");
		} catch (Exception e) {
			dataMismatch(path, "Double", value.getClass().getSimpleName());
			return defaultValue;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public Map getConfigurationSection(String path) {
		if (path == null || path.length() == 0) return values;
		Object value = getObject(path, null);
		if (value == null) return new HashMap<>();
		if (value instanceof Map) {
			return (Map) value;
		} else {
			dataMismatch(path, "Map", value.getClass().getSimpleName());
			return new HashMap<>();
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
			String keyWord = getRealKey(map, path.split("\\.")[0]);
			Object submap = map.get(keyWord);
			if (submap == null || !(submap instanceof Map)) {
				submap = new HashMap<String, Object>();
			}
			map.put(keyWord.replace("@#@", "."), set((Map<String, Object>) submap, path.substring(keyWord.length()+1, path.length()), value));
		} else {
			if (value == null) {
				map.remove(getRealKey(map, path));
			} else {
				map.put(path, value);
			}
		}
		return map;
	}
	
	private String getRealKey(Map<?, ?> map, String key) {
		for (Object mapkey : map.keySet()) {
			if (mapkey.toString().equalsIgnoreCase(key)) return mapkey.toString();
		}
		return key;
	}
	
	public boolean hasHeader() {
		if (header == null) return true;
		for (String line : readAllLines()) {
			if (line.contains("#")) return true;
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
	
	protected List<String> readAllLines() {
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
	
	public Set<String> getUsedPlaceholderIdentifiersRecursive(String... simpleKeys){
		Set<String> base = getUsedPlaceholders(values, simpleKeys);
		for (String placeholder : base.toArray(new String[0])) {
			List<Placeholder> pl = Placeholders.detectPlaceholders(placeholder);
			for (Placeholder p : pl) {
				base.add(p.getIdentifier());
			}
		}
		return base;
	}
	
	private Set<String> getUsedPlaceholders(Map<String, Object> map, String... simpleKeys){
		Set<String> values = new HashSet<String>();
		for (Entry<String, Object> entry : map.entrySet()) {
			for (String simpleKey : simpleKeys) {
				if (String.valueOf(entry.getKey()).equals(simpleKey)) values.addAll(Placeholders.detectAll(String.valueOf(entry.getValue())));
			}
			if (entry.getValue() instanceof Map) {
				values.addAll(getUsedPlaceholders((Map<String, Object>)entry.getValue(), simpleKeys));
			}
			if (entry.getValue() instanceof List) {
				for (Object obj : (List<Object>)entry.getValue()) {
					for (String simpleKey : simpleKeys) {
						if (String.valueOf(obj).equals(simpleKey)) values.addAll(Placeholders.detectAll(String.valueOf(entry.getValue())));
					}
				}
			}
		}
		return values;
	}
}