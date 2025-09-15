package me.neznamy.tab.shared.features.globalplayerlist;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Class for storing global playerlist configuration.
 */
@Getter
@RequiredArgsConstructor
public class GlobalPlayerListConfiguration {

    private final boolean othersAsSpectators;
    private final boolean vanishedAsSpectators;
    private final boolean isolateUnlistedServers;
    private final boolean updateLatency;
    @NotNull private final List<String> spyServers;
    @NotNull private final Map<String, List<String>> sharedServers;

    /**
     * Returns instance of this class created from given configuration section. If there are
     * issues in the configuration, console warns are printed.
     *
     * @param   section
     *          Configuration section to load from
     * @return  Loaded instance from given configuration section
     */
    @NotNull
    public static GlobalPlayerListConfiguration fromSection(@NotNull ConfigurationSection section) {
        // Check keys
        section.checkForUnknownKey(Arrays.asList("enabled", "display-others-as-spectators", "display-vanished-players-as-spectators",
                "isolate-unlisted-servers", "update-latency", "spy-servers", "server-groups"));

        ConfigurationSection serverGroupSection = section.getConfigurationSection("server-groups");
        Map<String, List<String>> sharedServers = new HashMap<>();
        Map<String, String> takenServers = new HashMap<>();
        for (Object serverGroup : serverGroupSection.getKeys()) {
            String group = serverGroup.toString();
            List<String> servers = serverGroupSection.getStringList(group, Collections.emptyList());
            sharedServers.put(group, servers);
            for (String server : servers) {
                if (takenServers.containsKey(server)) {
                    section.startupWarn(String.format("Server \"%s\" is defined in global playerlist groups \"%s\" and \"%s\", but it can only be a part of one group.",
                            server, takenServers.get(server), group));
                    continue;
                }
                takenServers.put(server, group);
            }
        }

        return new GlobalPlayerListConfiguration(
                section.getBoolean("display-others-as-spectators", false),
                section.getBoolean("display-vanished-players-as-spectators", true),
                section.getBoolean("isolate-unlisted-servers", false),
                section.getBoolean("update-latency", false),
                section.getStringList("spy-servers", Collections.singletonList("spyserver1")),
                sharedServers
        );
    }
}
