package me.neznamy.tab.shared.config.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.tab.api.PropertyConfiguration;
import me.neznamy.tab.api.config.YamlConfigurationFile;
import me.neznamy.tab.shared.TAB;

public class YamlPropertyConfigurationFile extends YamlConfigurationFile implements PropertyConfiguration {

	private static final String PER_SERVER = "per-server";
	private static final String PER_WORLD = "per-world";
	private static final String DEFAULT_GROUP = "_DEFAULT_";
	
	private final String category;
	private final List<Object> worldGroups = new ArrayList<>();
	private final List<Object> serverGroups = new ArrayList<>();
	
	public YamlPropertyConfigurationFile(InputStream source, File destination) throws IllegalStateException, YAMLException, IOException {
		super(source, destination);
		category = destination.getName().contains("groups") ? "group" : "user";
		serverGroups.addAll(getConfigurationSection(PER_SERVER).keySet());
		worldGroups.addAll(getConfigurationSection(PER_WORLD).keySet());
	}

	@Override
	public void setProperty(String name, String property, String server, String world, String value) {
		if (server != null) {
			set(String.format("%s.%s.%s.%s", PER_SERVER, server, name, property), value);
		} else if (world != null) {
			set(String.format("%s.%s.%s.%s", PER_WORLD, world, name, property), value);
		} else {
			set(String.format("%s.%s", name, property), value);
		}
	}

	@Override
	public String[] getProperty(String name, String property, String server, String world) {
		Object value;
		if ((value = getObject(new String[] {PER_SERVER, TAB.getInstance().getConfiguration().getGroup(serverGroups, server), name, property})) != null) {
			return new String[] {toString(value), category + "=" + name + ", server=" + server};
		}
		if ((value = getObject(new String[] {PER_SERVER, TAB.getInstance().getConfiguration().getGroup(serverGroups, server), DEFAULT_GROUP, property})) != null) {
			return new String[] {toString(value), category + "=" + DEFAULT_GROUP + ", server=" + server};
		}
		if ((value = getObject(new String[] {PER_WORLD, TAB.getInstance().getConfiguration().getGroup(worldGroups, world), name, property})) != null) {
			return new String[] {toString(value), category + "=" + name + ", world=" + world};
		}
		if ((value = getObject(new String[] {PER_WORLD, TAB.getInstance().getConfiguration().getGroup(worldGroups, world), DEFAULT_GROUP, property})) != null) {
			return new String[] {toString(value), category + "=" + DEFAULT_GROUP + ", world=" + world};
		}
		if ((value = getObject(new String[] {name, property})) != null) {
			return new String[] {toString(value), category + "=" + name};
		}
		if ((value = getObject(new String[] {DEFAULT_GROUP, property})) != null) {
			return new String[] {toString(value), category + "=" + DEFAULT_GROUP};
		}
		return new String[0];
	}
	
	@SuppressWarnings("unchecked")
	private String toString(Object obj) {
		if (obj instanceof List) {
			return ((List<Object>)obj).stream().map(Object::toString).collect(Collectors.joining("\n"));
		}
		return obj.toString();
	}

	@Override
	public void remove(String name) {
		set(name, null);
		getConfigurationSection(PER_WORLD).keySet().forEach(world -> set(PER_WORLD + "." + world + "." + name, null));
		getConfigurationSection(PER_SERVER).keySet().forEach(server -> set(PER_SERVER + "." + server + "." + name, null));
	}
}