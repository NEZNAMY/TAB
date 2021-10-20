package me.neznamy.tab.shared.config.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.tab.api.PropertyConfiguration;
import me.neznamy.tab.api.config.YamlConfigurationFile;
import me.neznamy.tab.shared.TAB;

public class YamlPropertyConfigurationFile extends YamlConfigurationFile implements PropertyConfiguration {

	private String category;
	private List<Object> worldGroups = new ArrayList<>();
	private List<Object> serverGroups = new ArrayList<>();
	
	public YamlPropertyConfigurationFile(InputStream source, File destination) throws IllegalStateException, YAMLException, IOException {
		super(source, destination);
		category = destination.getName().contains("groups") ? "group" : "user";
		serverGroups.addAll(getConfigurationSection("per-server").keySet());
		worldGroups.addAll(getConfigurationSection("per-world").keySet());
	}

	@Override
	public void setProperty(String name, String property, String server, String world, String value) {
		if (server != null) {
			set(String.format("per-server.%s.%s.%s", server, name, property), value);
		} else if (world != null) {
			set(String.format("per-world.%s.%s.%s", world, name, property), value);
		} else {
			set(String.format("%s.%s", name, property), value);
		}
	}

	@Override
	public String[] getProperty(String name, String property, String server, String world) {
		Object value = null;
		if ((value = getObject(String.format("per-server.%s.%s.%s", TAB.getInstance().getConfiguration().getGroup(serverGroups, server), name, property))) != null) {
			return new String[] {toString(value), category + "=" + name + ", server=" + server};
		}
		if ((value = getObject(String.format("per-server.%s._DEFAULT_.%s", TAB.getInstance().getConfiguration().getGroup(serverGroups, server), property))) != null) {
			return new String[] {toString(value), category + "=_DEFAULT_, server=" + server};
		}
		if ((value = getObject(String.format("per-world.%s.%s.%s", TAB.getInstance().getConfiguration().getGroup(worldGroups, world), name, property))) != null) {
			return new String[] {toString(value), category + "=" + name + ", world=" + world};
		}
		if ((value = getObject(String.format("per-world.%s._DEFAULT_.%s", TAB.getInstance().getConfiguration().getGroup(worldGroups, world), property))) != null) {
			return new String[] {toString(value), category + "=_DEFAULT_, world=" + world};
		}
		if ((value = getObject(String.format("%s.%s", name, property))) != null) {
			return new String[] {toString(value), category + "=" + name};
		}
		if ((value = getObject("_DEFAULT_." + property)) != null) {
			return new String[] {toString(value), category + "=_DEFAULT_"};
		}
		return new String[0];
	}
	
	@SuppressWarnings("unchecked")
	private String toString(Object obj) {
		if (obj instanceof List) {
			return String.join("\n", ((List<Object>)obj).toArray(new String[0]));
		}
		return obj.toString();
	}

	@Override
	public void remove(String name) {
		set(name, null);
		getConfigurationSection("per-world").keySet().forEach(world -> set("per-world." + world + "." + name, null));
		getConfigurationSection("per-server").keySet().forEach(server -> set("per-server." + server + "." + name, null));
	}
}