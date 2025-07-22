package me.neznamy.tab.shared.config.file;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;

/**
 * Abstract class for configuration file
 */
@Getter
@SuppressWarnings("unchecked")
public abstract class ConfigurationFile {

    /** Configuration file content */
    @Setter
    @NotNull
    protected Map<Object, Object> values;

    /** File to use */
    @NotNull
    protected final File file;

    /**
     * Constructs new instance and attempts to load specified configuration file.
     * If file does not exist, default file is copied from {@code source}.
     *
     * @param   source
     *          Source to copy file from if it does not exist
     * @param   destination
     *          File destination to use
     * @throws  IllegalArgumentException
     *          if {@code destination} is null
     * @throws  IllegalStateException
     *          if file does not exist and source is null
     * @throws  IOException
     *          if I/O operation with the file unexpectedly fails
     */
    protected ConfigurationFile(@Nullable InputStream source, @NonNull File destination) throws IOException {
        file = destination;
        if (file.getParentFile() != null && !file.getParentFile().exists()) Files.createDirectories(file.getParentFile().toPath());
        if (!file.exists()) {
            if (source == null) throw new IllegalStateException("File does not exist and source is null");
            Files.copy(source, file.toPath());
        }
    }

    /**
     * Saves values from map to the file
     */
    public abstract void save();

    /**
     * Gets config option with specified path. If the option is not present and
     * {@code defaultValue} is not {@code null}, value is inserted, {@link #save()}
     * called and {@code defaultValue} returned.
     *
     * @param   path
     *          Path to the option with sections separated with "{@code .}"
     * @param   defaultValue
     *          Value to be inserted and returned if option is not present
     * @return  value from configuration file
     */
    @Nullable
    public Object getObject(@NonNull String path, @Nullable Object defaultValue) {
        if (path.isEmpty()) return values;
        Object value = values;
        for (String section : path.contains(".") ? path.split("\\.") : new String[] {path}) {
            if (!(value instanceof Map)) {
                if (defaultValue != null) set(path, defaultValue);
                return defaultValue;
            }
            value = getIgnoreCase((Map<Object, Object>) value, section);
        }
        if (value == null && defaultValue != null) {
            TAB.getInstance().debug("Inserting missing config option \"" + path + "\" with value \"" + defaultValue + "\" into " + file.getName());
            set(path, defaultValue);
            return defaultValue;
        }
        return value;
    }

    /**
     * Returns config option with specified path. If option is not present,
     * {@code null} is returned.
     *
     * @param   path
     *          Path to the option with sections separated with "{@code .}"
     * @return  value from configuration file or null if not present
     */
    @Nullable
    public Object getObject(@NonNull String path) {
        return getObject(path, null);
    }

    /**
     * Returns element with specified path. If nothing is found, {@code null} is returned.
     *
     * @param   path
     *          Path as an array of map keys
     * @return  Value at specified path
     */
    @Nullable
    public Object getObject(@NonNull String[] path) {
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
     * @param   map
     *          map to get value from
     * @param   key
     *          case-insensitive key name
     * @return  map value from case-insensitive key
     */
    @Nullable
    private Object getIgnoreCase(@NonNull Map<Object, Object> map, @NonNull String key) {
        try {
            for (Entry<Object, Object> entry : map.entrySet()) {
                if (entry.getKey().toString().equalsIgnoreCase(key)) return entry.getValue();
            }
            return map.get(key);
        } catch (ConcurrentModificationException e) {
            // Map modified by another thread during iteration (such as running /tab group or /tab player)
            return getIgnoreCase(map, key);
        }
    }

    /**
     * Returns config option with specified path as {@code String}. If the option is not present
     * and {@code defaultValue} is not {@code null}, value is inserted, {@link #save()} called
     * and {@code defaultValue} returned.
     *
     * @param   path
     *          Path to the option with sections separated with "{@code .}"
     * @param   defaultValue
     *          Value to be inserted and returned if option is not present
     * @return  value from configuration file as {@code String}
     */
    @Contract("_, !null -> !null")
    public String getString(@NonNull String path, @Nullable String defaultValue) {
        Object value = getObject(path, defaultValue);
        if (value == null) return defaultValue;
        return String.valueOf(value);
    }

    /**
     * Returns config option with specified path as {@code List<String>}. If the option is not present
     * and {@code defaultValue} is not {@code null}, value is inserted, {@link #save()} called
     * and {@code defaultValue} returned.
     *
     * @param   path
     *          Path of the option with sections separated with "{@code .}"
     * @param   defaultValue
     *          Value to be inserted and returned if option is not present
     * @return  value from configuration file as {@code List<String>}
     */
    @Contract("_, !null -> !null")
    public List<String> getStringList(@NonNull String path, @Nullable List<String> defaultValue) {
        Object value = getObject(path, defaultValue);
        if (value == null) return defaultValue;
        if (!(value instanceof List)) {
            return Collections.emptyList();
        }
        List<String> fixedList = new ArrayList<>();
        for (Object key : (List<Object>)value) {
            fixedList.add(String.valueOf(key));
        }
        return fixedList;
    }

    /**
     * Returns config option with specified path as {@code Integer}. If the option is not present
     * and {@code defaultValue} is not {@code null}, value is inserted, {@link #save()} called
     * and {@code defaultValue} returned.
     *
     * @param   path
     *          Path to the option with sections separated with "{@code .}"
     * @param   defaultValue
     *          Value to be inserted and returned if option is not present
     * @return  value from configuration file as {@code Integer}
     */
    @Contract("_, !null -> !null")
    public Integer getInt(@NonNull String path, @Nullable Integer defaultValue) {
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
     * @param   path
     *          Path to the option with sections separated with "{@code .}"
     * @param   defaultValue
     *          Value to be inserted and returned if option is not present
     * @return  value from configuration file as {@code Boolean}
     */
    public boolean getBoolean(@NonNull String path, boolean defaultValue) {
        Object value = getObject(path, defaultValue);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value.toString());
    }

    /**
     * Returns config option with specified path as {@code Map<K, V>}. If the option
     * is not present or value is not a {@code Map}, new empty {@code Map} is returned.
     *
     * @param   path
     *          Path to the option with sections separated with "{@code .}"
     * @return  value from configuration file as {@code Map<K, V>}
     * @param   <K>
     *          Map key type
     * @param   <V>
     *          Map value type
     */
    public @NotNull <K, V> Map<K, V> getMap(@NonNull String path) {
        if (path.isEmpty()) return (Map<K, V>) values;
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
     * @param   path
     *          Path to the option with sections separated with "{@code .}"
     * @return  {@code true} if present, {@code false} if not
     */
    public boolean hasConfigOption(@NonNull String path) {
        return getObject(path) != null;
    }

    /**
     * Sets value to the specified path and saves the file to disk by calling {@link #save()}.
     *
     * @param   path
     *          Path to the option with sections separated with "{@code .}"
     * @param   value
     *          Value to save
     */
    public void set(@NonNull String path, @Nullable Object value) {
        set(values, path, value);
        save();
    }

    /**
     * Sets value to the specified map with specified path and value. This is an internal method
     * that correctly creates maps to separate sections using "{@code .}".
     *
     * @param   map
     *          Map to insert value to
     * @param   path
     *          Path to the option with sections separated with "{@code .}"
     * @param   value
     *          Value to save
     * @return  the first argument to allow chaining
     */
    @NotNull
    private Map<Object, Object> set(@NonNull Map<Object, Object> map, @NonNull String path, @Nullable Object value) {
        if (path.contains(".")) {
            String keyWord = getRealKey(map, path.split("\\.")[0]);
            Object subMap = map.get(keyWord);
            if (!(subMap instanceof Map)) {
                subMap = new LinkedHashMap<>();
            }
            map.put(keyWord, set((Map<Object, Object>) subMap, path.substring(keyWord.length()+1), value));
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
     * @param   map
     *          Map to check keys of
     * @param   key
     *          Key to find
     * @return  The real key name
     */
    @NotNull
    private String getRealKey(@NonNull Map<?, ?> map, @NonNull String key) {
        for (Object mapKey : map.keySet()) {
            if (mapKey.toString().equalsIgnoreCase(key)) return mapKey.toString();
        }
        return key;
    }

    /**
     * Sets value to specified key if key does not exist.
     *
     * @param   key
     *          Map key
     * @param   value
     *          Value to insert if missing
     * @return  {@code true} if value was set, {@code false} if key already exists
     */
    public boolean setIfMissing(@NonNull String key, @NonNull Object value) {
        if (!hasConfigOption(key)) {
            set(key, value);
            return true;
        }
        return false;
    }

    /**
     * Removes option from config if present and returns {@code true}. If option was
     * not present, returns {@code false}.
     *
     * @param   key
     *          Key to remove
     * @return  {@code true} if option was present and removed, {@code false} if not.
     */
    public boolean removeOption(@NonNull String key) {
        if (hasConfigOption(key)) {
            set(key, null);
            return true;
        }
        return false;
    }

    /**
     * Renames an option from old path to new path. Returns {@code true} if option was renamed,
     * {@code false} if the option was renamed previously already.
     *
     * @param   oldPath
     *          Old path to the option
     * @param   newPath
     *          New path to the option
     * @return  {@code true} if option was renamed successfully, {@code false} if not.
     */
    public boolean rename(@NonNull String oldPath, @NonNull String newPath) {
        if (hasConfigOption(oldPath)) {
            set(newPath, getObject(oldPath));
            set(oldPath, null);
            return true;
        }
        return false;
    }

    /**
     * Returns configuration section with given path.
     *
     * @param   path
     *          Path to get configuration section from
     * @return  Configuration section from given path
     */
    @NotNull
    public ConfigurationSection getConfigurationSection(@NonNull String path) {
        return new ConfigurationSection(file.getName(), path, getMap(path));
    }
}