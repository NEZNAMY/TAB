package me.neznamy.tab.shared.config.file;

import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.config.PropertyConfiguration;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.data.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class represents a configuration file for properties (groups.yml or users.yml).
 */
public class YamlPropertyConfigurationFile extends YamlConfigurationFile implements PropertyConfiguration {

    private final String PER_SERVER = "per-server";
    private final String PER_WORLD = "per-world";
    
    private final String category;
    private final ConfigurationSection root = getConfigurationSection("");
    private final Collection<String> worldGroups = section(PER_WORLD).getKeys().stream().map(Object::toString).collect(Collectors.toList());
    private final Collection<String> serverGroups = section(PER_SERVER).getKeys().stream().map(Object::toString).collect(Collectors.toList());

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
     * @throws  YAMLException
     *          if file has invalid YAML syntax
     * @throws  IOException
     *          if I/O operation with the file unexpectedly fails
     */
    public YamlPropertyConfigurationFile(@Nullable InputStream source, @NonNull File destination) throws IOException {
        super(source, destination);
        category = destination.getName().contains("groups") ? "group" : "user";
        for (Object key : root.getKeys()) {
            String entry = key.toString();
            if (entry.equals(PER_SERVER)) {
                for (String server : serverGroups) {
                    ConfigurationSection serverSection = section(PER_SERVER, server);
                    for (Object name : serverSection.getKeys()) {
                        for (Object property : serverSection.getConfigurationSection(name.toString()).getKeys()) {
                            checkProperty(destination.getName(), category, name.toString(), property.toString(), server, null, true);
                        }
                    }
                }
            } else if (entry.equals(PER_WORLD)) {
                for (String world : worldGroups) {
                    ConfigurationSection worldSection = section(PER_WORLD, world);
                    for (Object name : worldSection.getKeys()) {
                        for (Object property : worldSection.getConfigurationSection(name.toString()).getKeys()) {
                            checkProperty(destination.getName(), category, name.toString(), property.toString(), null, world, true);
                        }
                    }
                }
            } else {
                for (Object property : root.getConfigurationSection(entry).getKeys()) {
                    checkProperty(destination.getName(), category, entry, property.toString(), null, null, true);
                }
            }
        }
    }

    @Override
    public void setProperty(@NonNull String name, @NonNull String property, @Nullable Server server, @Nullable World world, @Nullable String value) {
        ConfigurationSection target;
        if (world != null) {
            target = getOrCreateSection(PER_WORLD, world.getName(), name);
        } else if (server != null) {
            target = getOrCreateSection(PER_SERVER, server.getName(), name);
        } else {
            target = getOrCreateSection(name);
        }
        target.put(property, fromString(value));
        save();
    }

    @Override
    public String[] getProperty(@NonNull String name, @NonNull String property, @Nullable Server server, @Nullable World world) {
        String worldName = world == null ? null : world.getName();
        String serverName = server == null ? null : server.getName();
        Object value;
        if ((value = section(PER_WORLD, TAB.getInstance().getConfiguration().getGroup(worldGroups, worldName), name).getObject(property)) != null) {
            return new String[] {toString(value), category + "=" + name + ", world=" + worldName};
        }
        if ((value = section(PER_WORLD, TAB.getInstance().getConfiguration().getGroup(worldGroups, worldName), TabConstants.DEFAULT_GROUP).getObject(property)) != null) {
            return new String[] {toString(value), category + "=" + TabConstants.DEFAULT_GROUP + ", world=" + worldName};
        }
        if ((value = section(PER_SERVER, TAB.getInstance().getConfiguration().getServerGroup(serverGroups, server), name).getObject(property)) != null) {
            return new String[] {toString(value), category + "=" + name + ", server=" + serverName};
        }
        if ((value = section(PER_SERVER, TAB.getInstance().getConfiguration().getServerGroup(serverGroups, server), TabConstants.DEFAULT_GROUP).getObject(property)) != null) {
            return new String[] {toString(value), category + "=" + TabConstants.DEFAULT_GROUP + ", server=" + serverName};
        }
        if ((value = section(name).getObject(property)) != null) {
            return new String[] {toString(value), category + "=" + name};
        }
        if ((value = section(TabConstants.DEFAULT_GROUP).getObject(property)) != null) {
            return new String[] {toString(value), category + "=" + TabConstants.DEFAULT_GROUP};
        }
        return new String[0];
    }

    @Override
    public void remove(@NonNull String name) {
        root.remove(name);
        for (Object world : section(PER_WORLD).getKeys()) {
            section(PER_WORLD, world.toString()).remove(name);
        }
        for (Object server : section(PER_SERVER).getKeys()) {
            section(PER_SERVER, server.toString()).remove(name);
        }
        save();
    }

    @Override
    @NotNull
    public Map<String, Object> getGlobalSettings(@NonNull String name) {
        Map<String, Object> settings = root.getMap(name);
        return settings == null ? new LinkedHashMap<>() : settings;
    }

    @Override
    @NotNull
    public Map<String, Map<String, Object>> getPerWorldSettings(@NonNull String name) {
        return getSettingsByGroup(PER_WORLD, name);
    }

    @Override
    @NotNull
    public Map<String, Map<String, Object>> getPerServerSettings(@NonNull String name) {
        return getSettingsByGroup(PER_SERVER, name);
    }

    @Override
    @NotNull
    public Set<String> getAllEntries() {
        Set<Object> set = new HashSet<>(root.getKeys());
        set.remove(PER_WORLD);
        set.remove(PER_SERVER);
        addNestedEntryNames(set, PER_WORLD);
        addNestedEntryNames(set, PER_SERVER);
        return set.stream().map(Object::toString).collect(Collectors.toSet());
    }

    @NotNull
    private ConfigurationSection section(@NotNull String... path) {
        ConfigurationSection section = root;
        for (String key : path) {
            section = section.getConfigurationSection(key);
        }
        return section;
    }

    @NotNull
    private ConfigurationSection getOrCreateSection(@NotNull String... path) {
        ConfigurationSection section = root;
        for (String key : path) {
            if (section.getMap(key) == null) {
                section.put(key, new LinkedHashMap<>());
            }
            section = section.getConfigurationSection(key);
        }
        return section;
    }

    @NotNull
    private Map<String, Map<String, Object>> getSettingsByGroup(@NotNull String sectionName, @NotNull String name) {
        Map<String, Map<String, Object>> settings = new HashMap<>();
        ConfigurationSection sections = section(sectionName);
        for (Object key : sections.getKeys()) {
            settings.put(key.toString(), sections.getConfigurationSection(key.toString()).getMap(name));
        }
        return settings;
    }

    private void addNestedEntryNames(@NotNull Set<Object> entries, @NotNull String sectionName) {
        ConfigurationSection sections = section(sectionName);
        for (Object key : sections.getKeys()) {
            entries.addAll(sections.getConfigurationSection(key.toString()).getKeys());
        }
    }
}