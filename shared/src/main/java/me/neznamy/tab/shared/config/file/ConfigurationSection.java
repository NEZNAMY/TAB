package me.neznamy.tab.shared.config.file;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor
public class ConfigurationSection {
    
    @NotNull private final String file;
    @NotNull private final String section;
    @NotNull private final Map<Object, Object> map;

    public void checkForUnknownKey(@NotNull List<String> validProperties) {
        for (Object mapKey : map.keySet()) {
            if (!validProperties.contains(mapKey.toString())) {
                startupWarn(String.format("Configuration section \"%s\" has unknown key \"%s\". Valid keys: %s", section, mapKey, validProperties));
            }
        }
    }

    public void startupWarn(@NotNull String message) {
        TAB.getInstance().getConfigHelper().startup().startupWarn("[" + file + "] " + message);
    }

    public void hint(@NotNull String message) {
        TAB.getInstance().getConfigHelper().hint(file, message);
    }

    @Nullable
    public Boolean getBoolean(@NotNull String path) {
        return getNullable(path, Boolean.class);
    }

    public boolean getBoolean(@NotNull String path, boolean defaultValue) {
        return getRequired(path, defaultValue, Boolean.class);
    }

    @Nullable
    public Integer getInt(@NotNull String path) {
        return getNullable(path, Integer.class);
    }

    public int getInt(@NotNull String path, int defaultValue) {
        return getRequired(path, defaultValue, Integer.class);
    }

    @Nullable
    public Number getNumber(@NotNull String path) {
        return getNullable(path, Number.class);
    }

    @NotNull
    public Number getNumber(@NotNull String path, @NotNull Number defaultValue) {
        return getRequired(path, defaultValue, Number.class);
    }

    @Nullable
    public String getString(@NotNull String path) {
        return getNullable(path, String.class);
    }

    @NotNull
    public String getString(@NotNull String path, @NotNull String defaultValue) {
        return getRequired(path, defaultValue, String.class);
    }

    @Nullable
    public List<String> getStringList(@NotNull String path) {
        return getNullable(path, List.class);
    }

    @NotNull
    public List<String> getStringList(@NotNull String path, @NotNull List<String> defaultValue) {
        return getRequired(path, defaultValue, List.class);
    }

    @Nullable
    public <K, V> Map<K, V> getMap(@NotNull String path) {
        return getNullable(path, Map.class);
    }

    @NotNull
    public <K, V> Map<K, V> getMap(@NotNull String path, @NotNull Map<?, ?> defaultValue) {
        return getRequired(path, defaultValue, Map.class);
    }

    @Nullable
    public Object getObject(@NotNull String path) {
        return getNullable(path, Object.class);
    }

    @NotNull
    public Object getObject(@NotNull String path, @NotNull Object defaultValue) {
        return getRequired(path, defaultValue, Object.class);
    }

    @Nullable
    private <T> T getNullable(@NotNull String path, @NotNull Class<T> clazz) {
        return evaluateNullable(get(path), path, clazz);
    }

    @Nullable
    private <T> T evaluateNullable(@Nullable Object value, @NotNull String path, @NotNull Class<T> clazz) {
        if (value != null && !clazz.isInstance(value)) {
            startupWarn("Configuration section \"" + section + "." + path + "\" is expected to be of type " +
                    clazz.getSimpleName() + ", but was " + value.getClass().getSimpleName());
            return null;
        }
        return (T) value;
    }

    @NotNull
    private <T> T getRequired(@NotNull String path, @NotNull T defaultValue, @NotNull Class<T> clazz) {
        return evaluateRequired(get(path), path, defaultValue, clazz);
    }

    @NotNull
    private <T> T evaluateRequired(@Nullable Object value, @NotNull String path, @NotNull T defaultValue, @NotNull Class<T> clazz) {
        if (value == null) {
            startupWarn("Missing configuration section \"" + section + "." + path +
                    "\" of type " + clazz.getSimpleName() + ", using default value " + defaultValue + ".");
            return defaultValue;
        }
        if (!clazz.isInstance(value)) {
            startupWarn("Configuration section \"" + section + "." + path + "\" is expected to be of type " +
                    clazz.getSimpleName() + ", but was " + value.getClass().getSimpleName() + " (" + value + "). Using default value \"" + defaultValue + "\".");
            return defaultValue;
        }
        return (T) value;
    }

    @Nullable
    private Object get(@NotNull String key) {
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            if (key.equalsIgnoreCase(entry.getKey().toString())) return entry.getValue();
        }
        return null;
    }

    @NotNull
    public Collection<Object> getKeys() {
        return map.keySet();
    }

    @NotNull
    public ConfigurationSection getConfigurationSection(@NotNull String path) {
        Map<Object, Object> map = getMap(path);
        if (map == null) map = Collections.emptyMap();
        return new ConfigurationSection(file, section + "." + path, map);
    }
}
