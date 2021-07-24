package me.neznamy.tab.shared.config.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.tab.api.config.YamlConfigurationFile;
import me.neznamy.tab.shared.config.PropertyConfiguration;

public class YamlPropertyConfigurationFile extends YamlConfigurationFile implements PropertyConfiguration {

	public YamlPropertyConfigurationFile(InputStream source, File destination) throws IllegalStateException, YAMLException, IOException {
		super(source, destination);
	}

	@Override
	public void setProperty(String name, String property, String server, String world, String value) {
		if (server != null) {
			set("per-server." + server + "." + name + "." + property, value);
		} else if (world != null) {
			set("per-world." + world + "." + name + "." + property, value);
		} else {
			set(name + "." + property, value);
		}
	}

	@Override
	public String getProperty(String name, String property, String server, String world) {
		Object value = getObject("per-server." + server + "." + name + "." + property);
		if ((value = getObject("per-server." + server + "._DEFAULT_." + property)) != null) {
			//TODO return source;
			return toString(value);
		}
		if ((value = getObject("per-world." + world + "." + name + "." + property)) != null) {
			return toString(value);
		}
		if ((value = getObject("per-world." + world + "._DEFAULT_." + property)) != null) {
			return toString(value);
		}
		if ((value = getObject(name + "." + property)) != null) {
			return toString(value);
		}
		if ((value = getObject("_DEFAULT_." + property)) != null) {
			return toString(value);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private String toString(Object obj) {
		if (obj instanceof List) {
			return String.join("\n", (String[]) ((List<Object>)obj).toArray(new String[0]));
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