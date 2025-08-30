package me.neznamy.tab.shared.config.file;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor
public class ConfigurationSection {
    
    @NotNull private final String file;
    @NotNull private final String section;
    @NotNull private final Map<Object, Object> map;

    public void checkForUnknownKey(@NonNull List<String> validProperties) {
        for (Object mapKey : map.keySet()) {
            if (!validProperties.contains(mapKey.toString().toLowerCase(Locale.US))) {
                startupWarn(String.format("Configuration section \"%s\" has unknown key \"%s\". Valid keys: %s", section, mapKey, validProperties));
            }
        }
    }

    public void startupWarn(@NonNull String message) {
        TAB.getInstance().getConfigHelper().startup().startupWarn("[" + file + "] " + section + ": " + message);
    }

    public void hint(@NonNull String message) {
        TAB.getInstance().getConfigHelper().hint(file, message);
    }

    @Nullable
    public Boolean getBoolean(@NonNull String path) {
        return getNullable(path, Boolean.class);
    }

    public boolean getBoolean(@NonNull String path, boolean defaultValue) {
        return getRequired(path, defaultValue, Boolean.class);
    }

    @Nullable
    public Integer getInt(@NonNull String path) {
        return getNullable(path, Integer.class);
    }

    public int getInt(@NonNull String path, int defaultValue) {
        return getRequired(path, defaultValue, Integer.class);
    }

    @Nullable
    public Number getNumber(@NonNull String path) {
        return getNullable(path, Number.class);
    }

    @NotNull
    public Number getNumber(@NonNull String path, @NonNull Number defaultValue) {
        return getRequired(path, defaultValue, Number.class);
    }

    @Nullable
    public String getString(@NonNull String path) {
        return fixString(getNullable(path, String.class));
    }

    @NotNull
    public String getString(@NonNull String path, @NonNull String defaultValue) {
        return fixString(getRequired(path, defaultValue, String.class));
    }

    @Nullable
    public List<String> getStringList(@NonNull String path) {
        List<Object> list = getNullable(path, List.class);
        if (list == null) return null;
        return list.stream().map(o -> fixString(o.toString())).collect(Collectors.toList());
    }

    @NotNull
    public List<String> getStringList(@NonNull String path, @NonNull List<String> defaultValue) {
        List<Object> list = getRequired(path, defaultValue, List.class);
        return list.stream().map(o -> fixString(o.toString())).collect(Collectors.toList());
    }

    @Nullable
    public <K, V> Map<K, V> getMap(@NonNull String path) {
        return getNullable(path, Map.class);
    }

    @NotNull
    public <K, V> Map<K, V> getMap(@NonNull String path, @NonNull Map<?, ?> defaultValue) {
        return getRequired(path, defaultValue, Map.class);
    }

    @Nullable
    public Object getObject(@NonNull String path) {
        return getNullable(path, Object.class);
    }

    @NotNull
    public Object getObject(@NonNull String path, @NonNull Object defaultValue) {
        return getRequired(path, defaultValue, Object.class);
    }

    @Nullable
    private <T> T getNullable(@NonNull String path, @NonNull Class<T> clazz) {
        return evaluateNullable(get(path), path, clazz);
    }

    @Nullable
    private <T> T evaluateNullable(@Nullable Object value, @NonNull String path, @NonNull Class<T> clazz) {
        if (value != null && !clazz.isInstance(value)) {
            startupWarn("Configuration section \"" + section + "." + path + "\" is expected to be of type " +
                    clazz.getSimpleName() + ", but was " + value.getClass().getSimpleName());
            return null;
        }
        return (T) value;
    }

    @NotNull
    private <T> T getRequired(@NonNull String path, @NonNull T defaultValue, @NonNull Class<T> clazz) {
        return evaluateRequired(get(path), path, defaultValue, clazz);
    }

    @NotNull
    private <T> T evaluateRequired(@Nullable Object value, @NonNull String path, @NonNull T defaultValue, @NonNull Class<T> clazz) {
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
    private Object get(@NonNull String key) {
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
    public ConfigurationSection getConfigurationSection(@NonNull String path) {
        Map<Object, Object> map = getMap(path);
        if (map == null) map = Collections.emptyMap();
        return new ConfigurationSection(file, section + "." + path, map);
    }

    @Contract("null -> null; !null -> !null")
    private String fixString(@Nullable String string) {
        if (string == null) return null;
        // Make \n work even if used in '', which snakeyaml does not convert to newline
        return string.replace("\\n", "\n");
    }
}
