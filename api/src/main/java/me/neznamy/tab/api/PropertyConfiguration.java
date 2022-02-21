package me.neznamy.tab.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public interface PropertyConfiguration {

    void setProperty(String name, String property, String server, String world, String value);

    String[] getProperty(String name, String property, String server, String world);

    void remove(String name);

    Map<String, String> getGlobalSettings(String name);

    Map<String, Map<String, String>> getPerWorldSettings(String name);

    Map<String, Map<String, String>> getPerServerSettings(String name);

    Set<String> getAllEntries();

    default Map<String, Map<String, String>> convertMap(Map<String, Map<String, Map<String, String>>> map, String key) {
        Map<String, Map<String, String>> converted = new HashMap<>();
        for (Map.Entry<String, Map<String, Map<String, String>>> entry : map.entrySet()) {
            converted.put(entry.getKey(), entry.getValue().get(key));
        }
        return converted;
    }
}