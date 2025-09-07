package me.neznamy.tab.shared.config.converter;

import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TextColor;
import me.neznamy.tab.shared.chat.component.TextComponent;
import me.neznamy.tab.shared.config.file.ConfigurationFile;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

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
            TAB.getInstance().getPlatform().logInfo(new TextComponent("Performing configuration conversion from config version " + configVersion + " to " + (configVersion + 1), TextColor.YELLOW));
            converters.get(configVersion).accept(config);
            configVersion++;
            config.set("config-version", configVersion);
        }
    }
}
