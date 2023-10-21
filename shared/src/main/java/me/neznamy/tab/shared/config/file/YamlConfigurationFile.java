package me.neznamy.tab.shared.config.file;

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

import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.yamlassist.YamlAssist;

/**
 * YAML implementation of ConfigurationFile
 */
public class YamlConfigurationFile extends ConfigurationFile {

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
    public YamlConfigurationFile(@Nullable InputStream source, @NonNull File destination) throws YAMLException, IOException {
        super(source, destination);
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            values = new Yaml().load(input);
            if (values == null) values = new LinkedHashMap<>();
            input.close();
        } catch (YAMLException e) {
            if (input != null) input.close();
            TAB tab = TAB.getInstance();
            tab.setBrokenFile(destination.getName());
            tab.getPlatform().logWarn(new IChatBaseComponent("File " + destination + " has broken syntax."));
            tab.getPlatform().logInfo(IChatBaseComponent.fromColoredText("&6Error message from yaml parser: " + e.getMessage()));
            List<String> suggestions = YamlAssist.getSuggestions(file);
            if (!suggestions.isEmpty()) {
                tab.getPlatform().logInfo(IChatBaseComponent.fromColoredText("&dSuggestions to fix yaml syntax:"));
                for (String suggestion : suggestions) {
                    tab.getPlatform().logInfo(IChatBaseComponent.fromColoredText("&d- " + suggestion));
                }
            }
            throw e;
        }
    }

    @Override
    public void save() {
        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            new Yaml(options).dump(values, writer);
            writer.close();
            fixHeader();
        } catch (IOException e) {
            TAB.getInstance().getPlatform().logWarn(new IChatBaseComponent("Failed to save yaml file " + file.getPath() + " with content " + values.toString()));
        }
    }
}