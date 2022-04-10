package me.neznamy.tab.shared.config.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.tab.api.PropertyConfiguration;
import me.neznamy.tab.api.config.YamlConfigurationFile;
import me.neznamy.tab.shared.TAB;

public class YamlPropertyConfigurationFile extends YamlConfigurationFile implements PropertyConfiguration {

    private final String PER_SERVER = "per-server";
    private final String PER_WORLD = "per-world";
    private final String DEFAULT_GROUP = "_DEFAULT_";
    
    private final String category;
    private final List<Object> worldGroups = new ArrayList<>(getConfigurationSection(PER_WORLD).keySet());
    private final List<Object> serverGroups = new ArrayList<>(getConfigurationSection(PER_SERVER).keySet());
    
    public YamlPropertyConfigurationFile(InputStream source, File destination) throws YAMLException, IOException {
        super(source, destination);
        category = destination.getName().contains("groups") ? "group" : "user";
    }

    @Override
    public void setProperty(String name, String property, String server, String world, String value) {
        if (world != null) {
            set(String.format("%s.%s.%s.%s", PER_WORLD, world, name, property), fromString(value));
        } else if (server != null) {
            set(String.format("%s.%s.%s.%s", PER_SERVER, server, name, property), fromString(value));
        } else {
            set(String.format("%s.%s", name, property), fromString(value));
        }
    }

    @Override
    public String[] getProperty(String name, String property, String server, String world) {
        Object value;
        if ((value = getObject(new String[] {PER_WORLD, TAB.getInstance().getConfiguration().getGroup(worldGroups, world), name, property})) != null) {
            return new String[] {toString(value), category + "=" + name + ", world=" + world};
        }
        if ((value = getObject(new String[] {PER_WORLD, TAB.getInstance().getConfiguration().getGroup(worldGroups, world), DEFAULT_GROUP, property})) != null) {
            return new String[] {toString(value), category + "=" + DEFAULT_GROUP + ", world=" + world};
        }
        if ((value = getObject(new String[] {PER_SERVER, TAB.getInstance().getConfiguration().getGroup(serverGroups, server), name, property})) != null) {
            return new String[] {toString(value), category + "=" + name + ", server=" + server};
        }
        if ((value = getObject(new String[] {PER_SERVER, TAB.getInstance().getConfiguration().getGroup(serverGroups, server), DEFAULT_GROUP, property})) != null) {
            return new String[] {toString(value), category + "=" + DEFAULT_GROUP + ", server=" + server};
        }
        if ((value = getObject(new String[] {name, property})) != null) {
            return new String[] {toString(value), category + "=" + name};
        }
        if ((value = getObject(new String[] {DEFAULT_GROUP, property})) != null) {
            return new String[] {toString(value), category + "=" + DEFAULT_GROUP};
        }
        return new String[0];
    }

    @Override
    public void remove(String name) {
        set(name, null);
        getConfigurationSection(PER_WORLD).keySet().forEach(world -> set(PER_WORLD + "." + world + "." + name, null));
        getConfigurationSection(PER_SERVER).keySet().forEach(server -> set(PER_SERVER + "." + server + "." + name, null));
    }

    @Override
    public Map<String, Object> getGlobalSettings(String name) {
        return getConfigurationSection(name);
    }

    @Override
    public Map<String, Map<String, Object>> getPerWorldSettings(String name) {
        return convertMap(getConfigurationSection(PER_WORLD), name);
    }

    @Override
    public Map<String, Map<String, Object>> getPerServerSettings(String name) {
        return convertMap(getConfigurationSection(PER_SERVER), name);
    }
    @Override
    public Set<String> getAllEntries() {
        Set<String> set = new HashSet<>(values.keySet());
        set.remove(PER_WORLD);
        set.remove(PER_SERVER);
        Map<String, Map<String, Map<String, String>>> perWorld = getConfigurationSection(PER_WORLD);
        perWorld.values().forEach(m -> set.addAll(m.keySet()));
        Map<String, Map<String, Map<String, String>>> perServer = getConfigurationSection(PER_SERVER);
        perServer.values().forEach(m -> set.addAll(m.keySet()));
        return set;
    }
}