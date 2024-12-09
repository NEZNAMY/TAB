package me.neznamy.tab.shared.config.file;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.config.PropertyConfiguration;
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
    private final Collection<String> worldGroups = new ArrayList<>(this.<String, Object>getMap(PER_WORLD).keySet());
    private final Collection<String> serverGroups = new ArrayList<>(this.<String, Object>getMap(PER_SERVER).keySet());

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
    public YamlPropertyConfigurationFile(@Nullable InputStream source, @NotNull File destination) throws IOException {
        super(source, destination);
        category = destination.getName().contains("groups") ? "group" : "user";
        for (Map.Entry<Object, Object> entry : getValues().entrySet()) {
            if (entry.getKey().equals(PER_SERVER)) {
                for (String server : serverGroups) {
                    for (String name : this.<String, Object>getMap(PER_SERVER + "." + server).keySet()) {
                        for (String property : this.<String, Object>getMap(PER_SERVER + "." + server + "." + name).keySet()) {
                            checkProperty(destination.getName(), category, name, property, server, null, true);
                        }
                    }
                }
            } else if (entry.getKey().equals(PER_WORLD)) {
                for (String world : worldGroups) {
                    for (String name : this.<String, Object>getMap(PER_WORLD + "." + world).keySet()) {
                        for (String property : this.<String, Object>getMap(PER_WORLD + "." + world + "." + name).keySet()) {
                            checkProperty(destination.getName(), category, name, property, null, world, true);
                        }
                    }
                }
            } else {
                for (String property : this.<String, Object>getMap(entry.getKey().toString()).keySet()) {
                    checkProperty(destination.getName(), category, entry.getKey().toString(), property, null, null, true);
                }
            }
        }
    }

    @Override
    public void setProperty(@NotNull String name, @NotNull String property, @Nullable String server, @Nullable String world, @Nullable String value) {
        if (world != null) {
            set(String.format("%s.%s.%s.%s", PER_WORLD, world, name, property), fromString(value));
        } else if (server != null) {
            set(String.format("%s.%s.%s.%s", PER_SERVER, server, name, property), fromString(value));
        } else {
            set(String.format("%s.%s", name, property), fromString(value));
        }
    }

    @Override
    public String[] getProperty(@NotNull String name, @NotNull String property, @Nullable String server, @Nullable String world) {
        Object value;
        if ((value = getObject(new String[] {PER_WORLD, TAB.getInstance().getConfiguration().getGroup(worldGroups, world), name, property})) != null) {
            return new String[] {toString(value), category + "=" + name + ", world=" + world};
        }
        if ((value = getObject(new String[] {PER_WORLD, TAB.getInstance().getConfiguration().getGroup(worldGroups, world), TabConstants.DEFAULT_GROUP, property})) != null) {
            return new String[] {toString(value), category + "=" + TabConstants.DEFAULT_GROUP + ", world=" + world};
        }
        if ((value = getObject(new String[] {PER_SERVER, TAB.getInstance().getConfiguration().getServerGroup(serverGroups, server), name, property})) != null) {
            return new String[] {toString(value), category + "=" + name + ", server=" + server};
        }
        if ((value = getObject(new String[] {PER_SERVER, TAB.getInstance().getConfiguration().getServerGroup(serverGroups, server), TabConstants.DEFAULT_GROUP, property})) != null) {
            return new String[] {toString(value), category + "=" + TabConstants.DEFAULT_GROUP + ", server=" + server};
        }
        if ((value = getObject(new String[] {name, property})) != null) {
            return new String[] {toString(value), category + "=" + name};
        }
        if ((value = getObject(new String[] {TabConstants.DEFAULT_GROUP, property})) != null) {
            return new String[] {toString(value), category + "=" + TabConstants.DEFAULT_GROUP};
        }
        return new String[0];
    }

    @Override
    public void remove(@NotNull String name) {
        set(name, null);
        getMap(PER_WORLD).keySet().forEach(world -> set(PER_WORLD + "." + world + "." + name, null));
        getMap(PER_SERVER).keySet().forEach(server -> set(PER_SERVER + "." + server + "." + name, null));
    }

    @Override
    public @NotNull Map<String, Object> getGlobalSettings(@NotNull String name) {
        return getMap(name);
    }

    @Override
    public @NotNull Map<String, Map<String, Object>> getPerWorldSettings(@NotNull String name) {
        return convertMap(getMap(PER_WORLD), name);
    }

    @Override
    public @NotNull Map<String, Map<String, Object>> getPerServerSettings(@NotNull String name) {
        return convertMap(getMap(PER_SERVER), name);
    }

    @Override
    public @NotNull Set<String> getAllEntries() {
        Set<Object> set = new HashSet<>(values.keySet());
        set.remove(PER_WORLD);
        set.remove(PER_SERVER);
        Map<String, Map<String, Map<String, String>>> perWorld = getMap(PER_WORLD);
        perWorld.values().forEach(m -> set.addAll(m.keySet()));
        Map<String, Map<String, Map<String, String>>> perServer = getMap(PER_SERVER);
        perServer.values().forEach(m -> set.addAll(m.keySet()));
        return set.stream().map(Object::toString).collect(Collectors.toSet());
    }
}