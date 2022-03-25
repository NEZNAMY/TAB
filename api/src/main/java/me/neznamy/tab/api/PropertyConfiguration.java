package me.neznamy.tab.api;

import java.util.*;
import java.util.stream.Collectors;

public interface PropertyConfiguration {

    void setProperty(String name, String property, String server, String world, String value);

    String[] getProperty(String name, String property, String server, String world);

    void remove(String name);

    Map<String, Object> getGlobalSettings(String name);

    Map<String, Map<String, Object>> getPerWorldSettings(String name);

    Map<String, Map<String, Object>> getPerServerSettings(String name);

    Set<String> getAllEntries();

    default Map<String, Map<String, Object>> convertMap(Map<String, Map<String, Map<String, Object>>> map, String key) {
        Map<String, Map<String, Object>> converted = new HashMap<>();
        for (Map.Entry<String, Map<String, Map<String, Object>>> entry : map.entrySet()) {
            converted.put(entry.getKey(), entry.getValue().get(key));
        }
        return converted;
    }

    @SuppressWarnings("unchecked")
    default String toString(Object obj) {
        if (obj instanceof List) {
            return ((List<Object>)obj).stream().map(Object::toString).collect(Collectors.joining("\n"));
        }
        return obj.toString();
    }

    default Object fromString(String string) {
        if (string != null && string.contains("\n")) {
            return Arrays.asList(string.split("\n"));
        }
        return string;
    }
}