package me.neznamy.tab.api.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.yamlassist.YamlAssist;

/**
 * YAML implementation of ConfigurationFile
 */
public class YamlConfigurationFile extends ConfigurationFile {

	/** SnakeYAML instance */
	private static final Yaml yaml;
	
	static {
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		yaml = new Yaml(options);
	}

	/**
	 * Constructs new instance and attempts to load specified configuration file.
	 * If file does not exist, default file is copied from {@code source}.
	 * 
	 * @param	source
	 * 			Source to copy file from if it does not exist
	 * @param	destination
	 * 			File destination to use
	 * @throws	IllegalArgumentException
	 * 			if {@code destination} is null
	 * @throws	IllegalStateException
	 * 			if file does not exist and source is null
	 * @throws	YAMLException
	 * 			if file has invalid YAML syntax
	 * @throws	IOException
	 * 			if I/O operation with the file unexpectedly fails
	 */
	public YamlConfigurationFile(InputStream source, File destination) throws YAMLException, IOException {
		super(source, destination);
		FileInputStream input = null;
		try {
			input = new FileInputStream(file);
			values = yaml.load(input);
			if (values == null) values = new LinkedHashMap<>();
			input.close();
		} catch (YAMLException e) {
			if (input != null) input.close();
			TabAPI tab = TabAPI.getInstance();
			tab.sendConsoleMessage("&c[TAB] File " + destination + " has broken syntax.", true);
			tab.sendConsoleMessage("&6[TAB] Error message from yaml parser: " + e.getMessage(), true);
			List<String> suggestions = YamlAssist.getSuggestions(file);
			if (!suggestions.isEmpty()) {
				tab.sendConsoleMessage("&d[TAB] Suggestions to fix yaml syntax:", true);
				for (String suggestion : suggestions) {
					tab.sendConsoleMessage("&d[TAB] - " + suggestion, true);
				}
			}
			throw e;
		}
	}

	@Override
	public void save() {
		try {
			Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
			yaml.dump(values, writer);
			writer.close();
			fixHeader();
		} catch (IOException e) {
			TabAPI.getInstance().sendConsoleMessage("&c[TAB] Failed to save yaml file " + file.getPath() + " with content " + values.toString(), true);
		}
	}
}