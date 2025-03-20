package me.neznamy.tab.shared.config.file;

import lombok.NonNull;
import me.neznamy.chat.TextColor;
import me.neznamy.chat.component.SimpleTextComponent;
import me.neznamy.chat.component.TextComponent;
import me.neznamy.tab.shared.TAB;
import me.neznamy.yamlassist.YamlAssist;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;

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
    public YamlConfigurationFile(@Nullable InputStream source, @NonNull File destination) throws IOException {
        super(source, destination);
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            LoaderOptions loaderOptions = new LoaderOptions();
            loaderOptions.setCodePointLimit(Integer.MAX_VALUE);
            Yaml yaml = new Yaml(loaderOptions);
            values = yaml.load(input);
            if (values == null) values = new LinkedHashMap<>();
            input.close();
        } catch (YAMLException e) {
            if (input != null) input.close();
            TAB tab = TAB.getInstance();
            tab.setBrokenFile(destination.getName());
            tab.getPlatform().logWarn(SimpleTextComponent.text("File " + destination + " has broken syntax."));
            tab.getPlatform().logInfo(new TextComponent("Error message from yaml parser: " + e.getMessage(), TextColor.GOLD));
            List<String> suggestions = YamlAssist.getSuggestions(file);
            if (!suggestions.isEmpty()) {
                tab.getPlatform().logInfo(new TextComponent("Suggestions to fix yaml syntax:", TextColor.LIGHT_PURPLE));
                for (String suggestion : suggestions) {
                    tab.getPlatform().logInfo(new TextComponent("- " + suggestion, TextColor.LIGHT_PURPLE));
                }
            }
            throw e;
        }
    }

    @Override
    public synchronized void save() {
        try {
            Writer writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8);
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            new Yaml(options).dump(values, writer);
            writer.close();
        } catch (IOException e) {
            TAB.getInstance().getPlatform().logWarn(SimpleTextComponent.text("Failed to save yaml file " + file.getPath() + " with content " + values.toString() + ": " + e.getMessage()));
        }
    }
}