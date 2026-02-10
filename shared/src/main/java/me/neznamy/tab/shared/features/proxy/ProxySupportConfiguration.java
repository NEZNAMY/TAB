package me.neznamy.tab.shared.features.proxy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.neznamy.tab.shared.ProjectVariables;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration for proxy support feature.
 */
@Getter
@AllArgsConstructor
public class ProxySupportConfiguration {

    @NotNull private final ConfigurationSection section;
    @NotNull private final String type;
    @NotNull private final String channelName;
    @NotNull private final String pluginName;
    @NotNull private final String redisUrl;
    @NotNull private final String rabbitmqExchange;
    @NotNull private final String rabbitmqUrl;

    /**
     * Returns instance of this class created from given configuration section. If there are
     * issues in the configuration, console warns are printed.
     *
     * @param   section
     *          Configuration section to load from
     * @return  Loaded instance from given configuration section
     */
    @NotNull
    public static ProxySupportConfiguration fromSection(@NotNull ConfigurationSection section) {
        // Check keys
        section.checkForUnknownKey(Arrays.asList("enabled", "channel-name-suffix", "type", "plugin", "redis", "rabbitmq"));

        // Check type
        List<String> supportedTypes = Arrays.asList("PLUGIN", "REDIS", "RABBITMQ");
        String type = section.getString("type", "PLUGIN").toUpperCase();
        if (!supportedTypes.contains(type)) {
            section.startupWarn("Proxy support type \"" + type + "\" is not supported. Supported types are: " + String.join(", ", supportedTypes) + ". Defaulting to PLUGIN.");
            type = "PLUGIN";
        }

        String channelName = ProjectVariables.PLUGIN_NAME + "_3_" + section.getString("channel-name-suffix", "TAB");

        ConfigurationSection plugin = section.getConfigurationSection("plugin");
        String pluginName = plugin.getString("name", "RedisBungee");

        ConfigurationSection redis = section.getConfigurationSection("redis");
        String redisUrl = redis.getString("url", "redis://:password@localhost:6379/0");

        ConfigurationSection rabbitmq = section.getConfigurationSection("rabbitmq");
        String rabbitmqExchange = rabbitmq.getString("exchange", "plugin");
        String rabbitmqUrl = rabbitmq.getString("url", "amqp://guest:guest@localhost:5672/%2F");

        return new ProxySupportConfiguration(
                section,
                type,
                channelName,
                pluginName,
                redisUrl,
                rabbitmqExchange,
                rabbitmqUrl
        );
    }
}
