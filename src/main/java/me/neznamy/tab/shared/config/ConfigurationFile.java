package me.neznamy.tab.shared.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
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

	//comments in the header of the file
	protected List<String> header;
	
	//config values
	protected Map<String, Object> values;
	
	//the file
	protected File file;
	
	public ConfigurationFile(File dataFolder, String source, String destination, List<String> header) throws IOException {
		this.header = header;
		dataFolder.mkdirs();
		file = new File(dataFolder, destination);
		if (!file.exists()) {
			Files.copy(getClass().getClassLoader().getResourceAsStream(source), file.toPath());
		}
	}
	
	/**
	 * Saves the file to disk
	 */
	public abstract void save();
	
	/**
	 * Returns name of the file
	 * @return name of the file
	 */
	public String getName() {
		return file.getName();
	}
	
	/**
	 * Returns the root map
	 * @return all values
	 */
	public Map<String, Object> getValues(){
		return values;
	}
	
	/**
	 * Gets config option with specified path. If the option is not present and defaultValue is not null,
	 * value is inserted, save() called and defaultValue returned.
	 * @param path - path of the config option
	 * @param defaultValue - value to be inserted and returned if option is not present
	 * @return value from config
	 */
	public Object getObject(String path, Object defaultValue) {
		try {
			Object value = values;
			for (String tab : path.split("\\.")) {
				//reverting compensation for groups with "." in their name
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
	
	/**
	 * Returns config option with specified path or null if not present
	 * @param path - path to the value
	 * @return value from file or null if not present
	 */
	public Object getObject(String path) {
		return getObject(path, null);
	}
	
	/**
	 * Gets value from map independent of case
	 * @param map - map to be taken from
	 * @param key - case insensitive key name
	 * @return value from map
	 */
	private Object getIgnoreCase(Map<Object, Object> map, String key) {
		for (Object mapkey : map.keySet()) {
			if (mapkey.toString().equalsIgnoreCase(key)) return map.get(mapkey);
		}
		return map.get(key);
	}
	
	/**
	 * Returns config option with specified path or null if not present
	 * @param path - path to the value
	 * @return value from file or null if not present
	 */
	public String getString(String path) {
		return getString(path, null);
	}
	
	/**
	 * Gets config option with specified path. If the option is not present and defaultValue is not null,
	 * value is inserted, save() called and defaultValue returned.
	 * @param path - path of the config option
	 * @param defaultValue - value to be inserted and returned if option is not present
	 * @return value from config
	 */
	public String getString(String path, String defaultValue) {
		Object value = getObject(path, defaultValue);
		if (value == null) return defaultValue;
		return value+"";
	}
	
	/**
	 * Returns config option with specified path or null if not present
	 * @param path - path to the value
	 * @return value from file or null if not present
	 */
	public List<String> getStringList(String path) {
		return getStringList(path, null);
	}
	
	/**
	 * Gets config option with specified path. If the option is not present and defaultValue is not null,
	 * value is inserted, save() called and defaultValue returned.
	 * @param path - path of the config option
	 * @param defaultValue - value to be inserted and returned if option is not present
	 * @return value from config
	 */
	public List<String> getStringList(String path, List<String> defaultValue) {
		Object value = getObject(path, defaultValue);
		if (value == null) return defaultValue;
		if (!(value instanceof List)) {
			dataMismatch(path, ArrayList.class, value.getClass());
			return new ArrayList<String>();
		}
		List<String> fixedList = new ArrayList<String>();
		for (Object key : (List<Object>)value) {
			fixedList.add(key+"");
		}
		return fixedList;
	}
	
	/**
	 * Returns whether file has specified option or not
	 * @param path - path to variable
	 * @return true if present, false if not
	 */
	public boolean hasConfigOption(String path) {
		return getObject(path) != null;
	}
	
	/**
	 * Returns config option with specified path or null if not present
	 * @param path - path to the value
	 * @return value from file or null if not present
	 */
	public Integer getInt(String path) {
		return getInt(path, null);
	}
	
	/**
	 * Gets config option with specified path. If the option is not present and defaultValue is not null,
	 * value is inserted, save() called and defaultValue returned.
	 * @param path - path of the config option
	 * @param defaultValue - value to be inserted and returned if option is not present
	 * @return value from config
	 */
	public Integer getInt(String path, Integer defaultValue) {
		Object value = getObject(path, defaultValue);
		if (value == null) return defaultValue;
		try{
			return Integer.parseInt(value+"");
		} catch (Exception e) {
			dataMismatch(path, Integer.class, value.getClass());
			return defaultValue;
		}
	}
	
	/**
	 * Returns config option with specified path or null if not present
	 * @param path - path to the value
	 * @return value from file or null if not present
	 */
	public Boolean getBoolean(String path) {
		return getBoolean(path, null);
	}
	
	/**
	 * Gets config option with specified path. If the option is not present and defaultValue is not null,
	 * value is inserted, save() called and defaultValue returned.
	 * @param path - path of the config option
	 * @param defaultValue - value to be inserted and returned if option is not present
	 * @return value from config
	 */
	public Boolean getBoolean(String path, Boolean defaultValue) {
		Object value = getObject(path, defaultValue);
		if (value == null) return defaultValue;
		try{
			return Boolean.parseBoolean(value+"");
		} catch (Exception e) {
			dataMismatch(path, Boolean.class, value.getClass());
			return defaultValue;
		}
	}
	
	/**
	 * Gets config option with specified path. If the option is not present and defaultValue is not null,
	 * value is inserted, save() called and defaultValue returned.
	 * @param path - path of the config option
	 * @param defaultValue - value to be inserted and returned if option is not present
	 * @return value from config
	 */
	public Double getDouble(String path, double defaultValue) {
		Object value = getObject(path, defaultValue);
		if (value == null) return defaultValue;
		try{
			return Double.parseDouble(value+"");
		} catch (Exception e) {
			dataMismatch(path, Double.class, value.getClass());
			return defaultValue;
		}
	}
	
	/**
	 * Returns config option with specified path or empty map if not present
	 * @param path - path to the value
	 * @return value from file or empty map if not present
	 */
	public <K, V> Map<K, V> getConfigurationSection(String path) {
		if (path == null || path.length() == 0) return (Map<K, V>) values;
		Object value = getObject(path, null);
		if (value == null) return new HashMap<>();
		if (value instanceof Map) {
			return (Map<K, V>) value;
		} else {
			dataMismatch(path, Map.class, value.getClass());
			return new HashMap<>();
		}
	}
	
	/**
	 * Prints a warning message about data type mismatch
	 * @param path - path to the variable
	 * @param expected - expected class type
	 * @param found - found class type
	 */
	private void dataMismatch(String path, Class<?> expected, Class<?> found) {
		Shared.errorManager.startupWarn("Data mismatch in &e" + file.getName() + "&c. Value of &e" + path + "&c is expected to be &e" + expected.getSimpleName() + "&c, but is &e" + found.getSimpleName() + "&c. This is a misconfiguration issue.");
	}
	
	/**
	 * Sets value to the specified path and saves to disk
	 * @param path - path to save to
	 * @param value - value to save
	 */
	public void set(String path, Object value) {
		set(values, path, value);
		save();
	}
	
	/**
	 * Sets value into provided map with provided path and value
	 * @param map - map to insert to
	 * @param path - path
	 * @param value - value
	 * @return the inserted map to allow code chaining
	 */
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
	
	/**
	 * Returns the real key name without case sensitivity
	 * @param map - map to check
	 * @param key - key to find
	 * @return The real key name
	 */
	private String getRealKey(Map<?, ?> map, String key) {
		for (Object mapkey : map.keySet()) {
			if (mapkey.toString().equalsIgnoreCase(key)) return mapkey.toString();
		}
		return key;
	}
	
	/**
	 * Returns whether the file contains header with comments or not
	 * @return true if contains or configured header is null, false otherwise
	 */
	public boolean hasHeader() {
		if (header == null) return true;
		for (String line : readAllLines()) {
			if (line.contains("#")) return true;
		}
		return false;
	}
	
	/**
	 * Inserts header into file
	 */
	public void fixHeader() {
		if (header == null) return;
		try {
			List<String> content = Lists.newArrayList(header);
			content.addAll(readAllLines());
			file.delete();
			file.createNewFile();
			BufferedWriter buf = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8));
			for (String line : content) {
				buf.write(line + System.getProperty("line.separator"));
			}
			buf.close();
		} catch (Exception ex) {
			Shared.errorManager.criticalError("Failed to modify file " + file, ex);
		}
	}
	
	/**
	 * Reads all lines in file and returns them as List
	 * @return list of lines in file
	 */
	protected List<String> readAllLines() {
		List<String> list = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
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
	
	/**
	 * Finds all used placeholder identifiers in the file including nested placeholders in given keys
	 * @param simpleKeys - keys to check
	 * @return Set of all used identifiers
	 */
	public Set<String> getUsedPlaceholderIdentifiersRecursive(String... simpleKeys){
		Set<String> base = getUsedPlaceholders(values, simpleKeys);
		for (String placeholder : base.toArray(new String[0])) { //avoiding concurrent modification
			List<Placeholder> pl = Placeholders.detectPlaceholders(placeholder);
			for (Placeholder p : pl) {
				base.add(p.getIdentifier());
			}
		}
		return base;
	}
	
	/**
	 * Returns Set of all used placeholder identifiers in given map and keys
	 * @param map - map to search
	 * @param simpleKeys - names of keys to look for
	 * @return Set of used identifiers
	 */
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