package me.neznamy.tab.shared.config.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.yaml.snakeyaml.error.YAMLException;

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
		String value = getString("per-server." + server + "." + name + "." + property);
		if ((value = getString("per-server." + server + "._DEFAULT_." + property)) != null) {
			//TODO return source;
			return value;
		}
		if ((value = getString("per-world." + world + "." + name + "." + property)) != null) {
			return value;
		}
		if ((value = getString("per-world." + world + "._DEFAULT_." + property)) != null) {
			return value;
		}
		if ((value = getString(name + "." + property)) != null) {
			return value;
		}
		if ((value = getString("_DEFAULT_." + property)) != null) {
			return value;
		}
		return null;
	}

	@Override
	public void remove(String name) {
		set(name, null);
		getConfigurationSection("per-world").keySet().forEach(world -> set("per-world." + world + "." + name, null));
		getConfigurationSection("per-server").keySet().forEach(server -> set("per-server." + server + "." + name, null));
	}
}