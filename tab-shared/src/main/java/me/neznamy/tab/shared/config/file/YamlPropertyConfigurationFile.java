package me.neznamy.tab.shared.config.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import me.neznamy.tab.shared.TabConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.neznamy.tab.shared.config.PropertyConfiguration;
import me.neznamy.tab.shared.TAB;

public class YamlPropertyConfigurationFile extends YamlConfigurationFile implements PropertyConfiguration {

    private final String PER_SERVER = "per-server";
    private final String PER_WORLD = "per-world";
    
    private final String category;
    private final List<Object> worldGroups = new ArrayList<>(getConfigurationSection(PER_WORLD).keySet());
    private final List<Object> serverGroups = new ArrayList<>(getConfigurationSection(PER_SERVER).keySet());
    
    public YamlPropertyConfigurationFile(@Nullable InputStream source, @NotNull File destination) throws IOException {
        super(source, destination);
        category = destination.getName().contains("groups") ? "group" : "user";
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
        getConfigurationSection(PER_WORLD).keySet().forEach(world -> set(PER_WORLD + "." + world + "." + name, null));
        getConfigurationSection(PER_SERVER).keySet().forEach(server -> set(PER_SERVER + "." + server + "." + name, null));
    }

    @Override
    public @NotNull Map<String, Object> getGlobalSettings(@NotNull String name) {
        return getConfigurationSection(name);
    }

    @Override
    public @NotNull Map<String, Map<String, Object>> getPerWorldSettings(@NotNull String name) {
        return convertMap(getConfigurationSection(PER_WORLD), name);
    }

    @Override
    public @NotNull Map<String, Map<String, Object>> getPerServerSettings(@NotNull String name) {
        return convertMap(getConfigurationSection(PER_SERVER), name);
    }
    @Override
    public @NotNull Set<String> getAllEntries() {
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