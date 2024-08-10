package me.neznamy.tab.shared.config.files;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor
public class ConfigurationSection {

    @NotNull private final ConfigurationFile file;

    protected void checkForUnknownKey(@NotNull String section, @NotNull List<String> validProperties) {
        Map<Object, ?> map = getMap(section);
        if (map == null) return;
        for (Object mapKey : map.keySet()) {
            if (!validProperties.contains(mapKey.toString())) {
                startupWarn(String.format("Configuration section \"%s\" has unknown key \"%s\". Valid keys: %s", section, mapKey, validProperties));
            }
        }
    }

    protected void checkForUnknownKey(@NotNull String[] section, @NotNull List<String> validProperties) {
        Map<Object, ?> map = getMap(section);
        if (map == null) return;
        for (Object mapKey : map.keySet()) {
            if (!validProperties.contains(mapKey.toString())) {
                startupWarn(String.format("Configuration section \"%s\" has unknown key \"%s\". Valid keys: %s", Arrays.toString(section), mapKey, validProperties));
            }
        }
    }

    protected void startupWarn(@NotNull String message) {
        TAB.getInstance().getConfigHelper().startup().startupWarn("[" + file.getFile().getName() + "] " + message);
    }
    
    protected void hint(@NotNull String message) {
        TAB.getInstance().getPlatform().logInfo(TabComponent.fromColoredText(EnumChatFormat.GOLD + "[Hint] " + message));
    }

    @Nullable
    protected Boolean getBoolean(@NotNull String path) {
        return getNullable(path, Boolean.class);
    }

    protected boolean getBoolean(@NotNull String path, boolean defaultValue) {
        return getRequired(path, defaultValue, Boolean.class);
    }

    @Nullable
    protected Integer getInt(@NotNull String path) {
        return getNullable(path, Integer.class);
    }

    protected int getInt(@NotNull String path, int defaultValue) {
        return getRequired(path, defaultValue, Integer.class);
    }

    @Nullable
    protected Number getNumber(@NotNull String path) {
        return getNullable(path, Number.class);
    }

    @NotNull
    protected Number getNumber(@NotNull String path, @NotNull Number defaultValue) {
        return getRequired(path, defaultValue, Number.class);
    }

    @Nullable
    protected String getString(@NotNull String path) {
        return getNullable(path, String.class);
    }

    @Nullable
    protected String getString(@NotNull String[] path) {
        return getNullable(path, String.class);
    }

    @NotNull
    protected String getString(@NotNull String path, @NotNull String defaultValue) {
        return getRequired(path, defaultValue, String.class);
    }

    @NotNull
    protected String getString(@NotNull String[] path, @NotNull String defaultValue) {
        return getRequired(path, defaultValue, String.class);
    }

    @Nullable
    protected List<String> getStringList(@NotNull String path) {
        return getNullable(path, List.class);
    }

    @NotNull
    protected List<String> getStringList(@NotNull String path, @NotNull List<String> defaultValue) {
        return getRequired(path, defaultValue, List.class);
    }

    @NotNull
    protected List<String> getStringList(@NotNull String[] path, @NotNull List<String> defaultValue) {
        return getRequired(path, defaultValue, List.class);
    }

    @Nullable
    protected <K, V> Map<K, V> getMap(@NotNull String path) {
        return getNullable(path, Map.class);
    }

    @Nullable
    protected <K, V> Map<K, V> getMap(@NotNull String[] path) {
        return getNullable(path, Map.class);
    }

    @NotNull
    protected <K, V> Map<K, V> getMap(@NotNull String path, @NotNull Map<?, ?> defaultValue) {
        return getRequired(path, defaultValue, Map.class);
    }

    @NotNull
    protected <K, V> Map<K, V> getMap(@NotNull String[] path, @NotNull Map<?, ?> defaultValue) {
        return getRequired(path, defaultValue, Map.class);
    }

    @Nullable
    protected Object getObject(@NotNull String path) {
        return file.getObject(path);
    }

    @NotNull
    protected Object getObject(@NotNull String path, @NotNull Object defaultValue) {
        Object value = file.getObject(path);
        if (value == null) {
            startupWarn("Missing configuration section \"" + path + "\", using default value " + defaultValue + ".");
            return defaultValue;
        }
        return value;
    }

    private <T> T getNullable(@NotNull String path, @NotNull Class<T> clazz) {
        return evaluateNullable(file.getObject(path), path, clazz);
    }

    private <T> T getNullable(@NotNull String[] path, @NotNull Class<T> clazz) {
        return evaluateNullable(file.getObject(path), Arrays.toString(path), clazz);
    }

    private <T> T evaluateNullable(@Nullable Object value, @NotNull String path, @NotNull Class<T> clazz) {
        if (value != null && !clazz.isInstance(value)) {
            startupWarn("Configuration section \"" + path + "\" is expected to be of type " +
                    clazz.getSimpleName() + ", but was " + value.getClass().getSimpleName());
            return null;
        }
        return (T) value;
    }

    private <T> T getRequired(@NotNull String path, @NotNull T defaultValue, @NotNull Class<T> clazz) {
        return evaluateRequired(file.getObject(path), path, defaultValue, clazz);
    }

    private <T> T getRequired(@NotNull String[] path, @NotNull T defaultValue, @NotNull Class<T> clazz) {
        return evaluateRequired(file.getObject(path), Arrays.toString(path), defaultValue, clazz);
    }

    private <T> T evaluateRequired(@Nullable Object value, @NotNull String path, @NotNull T defaultValue, @NotNull Class<T> clazz) {
        if (value == null) {
            startupWarn("Missing configuration section \"" + path +
                    "\" of type " + clazz.getSimpleName() + ", using default value " + defaultValue + ".");
            return defaultValue;
        }
        if (!clazz.isInstance(value)) {
            startupWarn("Configuration section \"" + path + "\" is expected to be of type " +
                    clazz.getSimpleName() + ", but was " + value.getClass().getSimpleName() + " (" + value + "). Using default value \"" + defaultValue + "\".");
            return defaultValue;
        }
        return (T) value;
    }
}
