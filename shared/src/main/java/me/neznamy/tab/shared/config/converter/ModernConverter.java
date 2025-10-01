package me.neznamy.tab.shared.config.converter;

import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.placeholders.conditions.ConditionsSection;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Config converter that uses new config-version field.
 */
public class ModernConverter {

    /** Converters from one config version to another */
    private final Map<Integer, Consumer<ConfigurationFile>> converters = new HashMap<>();

    /**
     * Constructs new instance and initializes all converters.
     */
    public ModernConverter() {
        converters.put(0, config -> {
            Map<String, Object> components = new LinkedHashMap<>();
            components.put("minimessage-support", true);
            components.put("disable-shadow-for-heads", true);
            config.set("components", components);
        });
        converters.put(1, config -> {
            // Warn users
            TAB.getInstance().getPlatform().logWarn(new TabTextComponent("Please note that header/footer conversion is only " +
                    "symbolic and does not fully convert old format to new one. The following content is not converted: per-group settings and per-user settings." +
                    " Review your config to make sure it is set up the way you want.", TabTextColor.RED));

            // Read old data
            ConfigurationSection headerFooter = config.getConfigurationSection("header-footer");
            boolean enabled = headerFooter.getBoolean("enabled", true);
            List<String> defaultHeader = headerFooter.getStringList("header", Collections.emptyList());
            List<String> defaultFooter = headerFooter.getStringList("footer", Collections.emptyList());
            String disableCondition = headerFooter.getString("disable-condition", "%world%=disabledworld");
            ConfigurationSection perWorld = headerFooter.getConfigurationSection("per-world");
            ConfigurationSection perServer = headerFooter.getConfigurationSection("per-server");

            // Write new data
            Map<String, Object> designs = new LinkedHashMap<>();

            for (Object world : perWorld.getKeys()) {
                ConfigurationSection worldSection = perWorld.getConfigurationSection(world.toString());
                Map<String, Object> design = new LinkedHashMap<>();
                design.put("display-condition", Arrays.stream(world.toString().split(";")).map(part -> {
                    if (part.endsWith("*")) {
                        return "%world%|-" + part.substring(0, part.length() - 1);
                    } else if (part.startsWith("*")) {
                        return "%world%-|" + part.substring(1);
                    } else {
                        return "%world%=" + part;
                    }
                }).collect(Collectors.joining("|")));
                design.put("header", worldSection.getStringList("header", Collections.emptyList()));
                design.put("footer", worldSection.getStringList("footer", Collections.emptyList()));
                designs.put("world-" + world, design);
            }

            for (Object server : perServer.getKeys()) {
                ConfigurationSection serverSection = perServer.getConfigurationSection(server.toString());
                Map<String, Object> design = new LinkedHashMap<>();
                design.put("display-condition", Arrays.stream(server.toString().split(";")).map(part -> {
                    if (part.endsWith("*")) {
                        return "%server%|-" + part.substring(0, part.length() - 1);
                    } else if (part.startsWith("*")) {
                        return "%server%-|" + part.substring(1);
                    } else {
                        return "%server%=" + part;
                    }
                }).collect(Collectors.joining("|")));
                design.put("header", serverSection.getStringList("header", Collections.emptyList()));
                design.put("footer", serverSection.getStringList("footer", Collections.emptyList()));
                designs.put("server-" + server, design);
            }

            Map<String, Object> defaultDesign = new LinkedHashMap<>();
            if (!disableCondition.isEmpty()) {
                ConditionsSection conditions = ConditionsSection.fromSection(config.getConfigurationSection("conditions"));
                ConditionsSection.ConditionDefinition namedCondition = conditions.getConditions().get(disableCondition);
                if (namedCondition != null) {
                    // Named condition
                    defaultDesign.put("display-condition", "%condition:" + namedCondition.getName() + "%=" + namedCondition.getNo());
                } else {
                    // Short format
                    defaultDesign.put("display-condition", new Condition(disableCondition).invert().toShortFormat());
                }
            }
            defaultDesign.put("header", defaultHeader);
            defaultDesign.put("footer", defaultFooter);
            designs.put("default", defaultDesign);

            Map<String, Object> newHeaderFooter = new LinkedHashMap<>();
            newHeaderFooter.put("enabled", enabled);
            newHeaderFooter.put("designs", designs);
            config.set("header-footer", newHeaderFooter);
        });
    }

    /**
     * Converts config to the latest version.
     *
     * @param   config
     *          config file to convert
     */
    public void convert(@NonNull ConfigurationFile config) {
        int configVersion = config.getInt("config-version", 0);
        while (converters.containsKey(configVersion)) {
            TAB.getInstance().getPlatform().logInfo(new TabTextComponent("Performing configuration conversion from config version " + configVersion + " to " + (configVersion + 1), TabTextColor.YELLOW));
            converters.get(configVersion).accept(config);
            configVersion++;
            config.set("config-version", configVersion);
        }
    }
}
