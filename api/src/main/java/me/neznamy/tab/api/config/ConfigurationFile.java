package me.neznamy.tab.api.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.neznamy.tab.api.TabAPI;

/**
 * Abstract class for configuration file
 */
@SuppressWarnings("unchecked")
public abstract class ConfigurationFile {

	/** Comments on top of the file */
	protected List<String> header;

	/** Configuration file content */
	protected Map<String, Object> values;

	/** File to use */
	protected final File file;

	/**
	 * Constructs new instance and attempts to load specified configuration file.
	 * If file does not exist, default file is copied from {@code source}.
	 * 
	 * @param	source
	 * 			Source to copy file from if it does not exist
	 * @param	destination
	 * 			File destination to use
	 * @throws	IllegalArgumentException
	 * 			if {@code destination} is null
	 * @throws	IllegalStateException
	 * 			if file does not exist and source is null
	 * @throws	IOException
	 * 			if I/O operation with the file unexpectedly fails
	 */
	protected ConfigurationFile(InputStream source, File destination) throws IOException {
		if (destination == null) throw new IllegalArgumentException("Destination cannot be null");
		this.file = destination;
		if (file.getParentFile() != null) Files.createDirectories(file.getParentFile().toPath());
		if (!file.exists() && source == null) throw new IllegalStateException("File does not exist and source is null");
		if (file.createNewFile()) {
//			Files.copy(source, file.toPath());
			//avoiding MalformedInputException thrown on fabric due to file not having UTF8 encoding by default
			//also automatically using the encoding so users don't have to worry about it anymore
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(source, StandardCharsets.UTF_8));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))){
				String line;
				while ((line = reader.readLine()) != null) {
					writer.write(line + "\n");
				}
			}
		}
		detectHeader();
	}

	/**
	 * Saves values from map to the file
	 */
	public abstract void save();

	/**
	 * Returns simple name of the file
	 * @return	simple name of the file
	 */
	public String getName() {
		return file.getName();
	}

	/**
	 * Returns the root value map
	 * @return	the root value map
	 */
	public Map<String, Object> getValues(){
		return values;
	}

	public void setValues(Map<String, Object> values) {
		this.values = values;
	}

	/**
	 * Gets config option with specified path. If the option is not present and 
	 * {@code defaultValue} is not {@code null}, value is inserted, {@link #save()} 
	 * called and {@code defaultValue} returned.
	 * 
	 * @param	path
	 * 			Path to the option with sections separated with "{@code .}"
	 * @param	defaultValue
	 * 			Value to be inserted and returned if option is not present
	 * @return	value from configuration file
	 */
	public Object getObject(String path, Object defaultValue) {
		Object value = values;
		for (String section : path.contains(".") ? path.split("\\.") : new String[] {path}) {
			if (!(value instanceof Map)) {
				if (defaultValue != null) set(path, defaultValue);
				return defaultValue;
			}
			value = getIgnoreCase((Map<Object, Object>) value, section);
		}
		if (value == null && defaultValue != null) {
			TabAPI.getInstance().debug("Inserting missing config option \"" + path + "\" with value \"" + defaultValue + "\" into " + file.getName());
			set(path, defaultValue);
			return defaultValue;
		}
		return value;
	}

	/**
	 * Returns config option with specified path. If option is not present,
	 * {@code null} is returned.
	 * 
	 * @param	path	
	 * 			Path to the option with sections separated with "{@code .}"
	 * @return	value from configuration file or null if not present
	 */
	public Object getObject(String path) {
		return getObject(path, null);
	}
	
	public Object getObject(String[] path) {
		Object value = values;
		for (String section : path) {
			if (!(value instanceof Map)) {
				return null;
			}
			value = getIgnoreCase((Map<Object, Object>) value, section);
		}
		return value;
	}

	/**
	 * Returns value from map without case sensitivity of the key. Returns
	 * {@code null} if no such key is found.
	 * 
	 * @param	map
	 * 			map to get value from
	 * @param	key
	 * 			case-insensitive key name
	 * @return	map value from case-insensitive key
	 */
	private Object getIgnoreCase(Map<Object, Object> map, String key) {
		for (Entry<Object, Object> entry : map.entrySet()) {
			if (entry.getKey().toString().equalsIgnoreCase(key)) return entry.getValue();
		}
		return map.get(key);
	}

	/**
	 * Returns config option with specified path as {@code String}. Returns {@code null} if
	 * option is not present.
	 * 
	 * @param	path	
	 * 			Path to the option with sections separated with "{@code .}"
	 * @return	value from file or null if not present
	 */
	public String getString(String path) {
		return getString(path, null);
	}

	/**
	 * Returns config option with specified path as {@code String}. If the option is not present 
	 * and {@code defaultValue} is not {@code null}, value is inserted, {@link #save()} called 
	 * and {@code defaultValue} returned.
	 * 
	 * @param	path	
	 * 			Path to the option with sections separated with "{@code .}"
	 * @param	defaultValue
	 * 			Value to be inserted and returned if option is not present
	 * @return	value from configuration file as {@code String}
	 */
	public String getString(String path, String defaultValue) {
		Object value = getObject(path, defaultValue);
		if (value == null) return defaultValue;
		return String.valueOf(value);
	}

	/**
	 * Returns config option with specified path as {@code List<String>}. Returns {@code null} if
	 * option is not present.
	 * 
	 * @param	path
	 * 			Path to the option with sections separated with "{@code .}"
	 * @return	value from file or null if not present
	 */
	public List<String> getStringList(String path) {
		return getStringList(path, null);
	}

	/**
	 * Returns config option with specified path as {@code List<String>}. If the option is not present 
	 * and {@code defaultValue} is not {@code null}, value is inserted, {@link #save()} called 
	 * and {@code defaultValue} returned.
	 * 
	 * @param	path	
	 * 			Path of the option with sections separated with "{@code .}"
	 * @param	defaultValue
	 * 			Value to be inserted and returned if option is not present
	 * @return	value from configuration file as {@code List<String>}
	 */
	public List<String> getStringList(String path, List<String> defaultValue) {
		Object value = getObject(path, defaultValue);
		if (value == null) return defaultValue;
		if (!(value instanceof List)) {
			return new ArrayList<>();
		}
		List<String> fixedList = new ArrayList<>();
		for (Object key : (List<Object>)value) {
			fixedList.add(key.toString());
		}
		return fixedList;
	}

	/**
	 * Returns config option with specified path as {@code Integer}. Returns {@code null} if
	 * option is not present.
	 * 
	 * @param	path
	 * 			Path to the option with sections separated with "{@code .}"
	 * @return	value from file or null if not present
	 */
	public Integer getInt(String path) {
		return getInt(path, null);
	}

	/**
	 * Returns config option with specified path as {@code Integer}. If the option is not present 
	 * and {@code defaultValue} is not {@code null}, value is inserted, {@link #save()} called 
	 * and {@code defaultValue} returned.
	 * 
	 * @param	path	
	 * 			Path to the option with sections separated with "{@code .}"
	 * @param	defaultValue
	 * 			Value to be inserted and returned if option is not present
	 * @return	value from configuration file as {@code Integer}
	 */
	public Integer getInt(String path, Integer defaultValue) {
		Object value = getObject(path, defaultValue);
		if (value == null) return defaultValue;
		try{
			return Integer.parseInt(value.toString());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * Returns config option with specified path as {@code Boolean}. If the option is not present 
	 * and {@code defaultValue} is not {@code null}, value is inserted, {@link #save()} called 
	 * and {@code defaultValue} returned.
	 * 
	 * @param	path	
	 * 			Path to the option with sections separated with "{@code .}"
	 * @param	defaultValue
	 * 			Value to be inserted and returned if option is not present
	 * @return	value from configuration file as {@code Boolean}
	 */
	public boolean getBoolean(String path, boolean defaultValue) {
		Object value = getObject(path, defaultValue);
		if (value == null) return defaultValue;
		return Boolean.parseBoolean(value.toString());
	}

	/**
	 * Returns config option with specified path as {@code Double}. If the option is not present 
	 * and {@code defaultValue} is not {@code null}, value is inserted, {@link #save()} called 
	 * and {@code defaultValue} returned.
	 * 
	 * @param	path	
	 * 			Path to the option with sections separated with "{@code .}"
	 * @param	defaultValue
	 * 			Value to be inserted and returned if option is not present
	 * @return	value from configuration file as {@code Double}
	 */
	public Double getDouble(String path, double defaultValue) {
		Object value = getObject(path, defaultValue);
		if (value == null) return defaultValue;
		try {
			return Double.parseDouble(value.toString());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * Returns config option with specified path as {@code Map<K, V>}. If the option 
	 * is not present or value is not a {@code Map}, new empty {@code Map} is returned.
	 * 
	 * @param	path	
	 * 			Path to the option with sections separated with "{@code .}"
	 * @return	value from configuration file as {@code Map<K, V>}
	 */
	public <K, V> Map<K, V> getConfigurationSection(String path) {
		if (path == null || path.length() == 0) return (Map<K, V>) values;
		Object value = getObject(path, null);
		if (value instanceof Map) {
			return (Map<K, V>) value;
		} else {
			return new LinkedHashMap<>();
		}
	}

	/**
	 * Returns {@code true} if the file has option with specified path, {@code false}
	 * if not.
	 * 
	 * @param	path
	 * 			Path to the option with sections separated with "{@code .}"
	 * @return	{@code true} if present, {@code false} if not
	 */
	public boolean hasConfigOption(String path) {
		return getObject(path) != null;
	}

	/**
	 * Sets value to the specified path and saves the file to disk by calling {@link #save()}.
	 * 
	 * @param	path
	 * 			Path to the option with sections separated with "{@code .}"
	 * @param	value
	 * 			Value to save
	 */
	public void set(String path, Object value) {
		set(values, path, value);
		save();
	}

	/**
	 * Sets value to the specified map with specified path and value. This is an internal method
	 * that correctly creates maps to separate sections using "{@code .}".
	 * 
	 * @param	map
	 * 			Map to insert value to
	 * @param	path
	 * 			Path to the option with sections separated with "{@code .}"
	 * @param	value
	 * 			Value to save
	 * @return	the first argument to allow chaining
	 */
	private Map<String, Object> set(Map<String, Object> map, String path, Object value) {
		if (path.contains(".")) {
			String keyWord = getRealKey(map, path.split("\\.")[0]);
			Object subMap = map.get(keyWord);
			if (!(subMap instanceof Map)) {
				subMap = new LinkedHashMap<>();
			}
			map.put(keyWord, set((Map<String, Object>) subMap, path.substring(keyWord.length()+1), value));
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
	 * Returns the real key name without case sensitivity from map. If not found, inserted key
	 * is returned.
	 * 
	 * @param	map
	 * 			Map to check keys of
	 * @param	key
	 * 			Key to find
	 * @return	The real key name
	 */
	private String getRealKey(Map<?, ?> map, String key) {
		for (Object mapKey : map.keySet()) {
			if (mapKey.toString().equalsIgnoreCase(key)) return mapKey.toString();
		}
		return key;
	}

	/**
	 * Detects header of a file (first lines of file starting with #).
	 *
	 * @throws	IOException
	 * 			if I/O operation fails
	 */
	private void detectHeader() throws IOException {
		header = new ArrayList<>();
		for (String line : Files.readAllLines(file.toPath())) {
			if (line.startsWith("#")) {
				header.add(line);
			} else {
				break;
			}
		}
	}

	/**
	 * Inserts header back into file. This is required after calling {@link #save()}, because
	 * it destroys the header.
	 * 
	 * @throws	IOException
	 * 			if I/O operation fails
	 */
	public void fixHeader() throws IOException {
		if (header == null) return;
		List<String> content = new ArrayList<>(header);
		content.addAll(Files.readAllLines(file.toPath()));
		Files.delete(file.toPath());
		if (file.createNewFile()) {
			Files.write(file.toPath(), content);
		}
	}

	public File getFile() {
		return file;
	}
}