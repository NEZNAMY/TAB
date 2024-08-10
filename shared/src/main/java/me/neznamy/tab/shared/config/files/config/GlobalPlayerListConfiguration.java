package me.neznamy.tab.shared.config.files.config;

import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.files.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class GlobalPlayerListConfiguration extends ConfigurationSection {

    private final String SECTION = "global-playerlist";
    public final boolean othersAsSpectators = getBoolean(SECTION + ".display-others-as-spectators", false);
    public final boolean vanishedAsSpectators = getBoolean(SECTION + ".display-vanished-players-as-spectators", true);
    public final boolean isolateUnlistedServers = getBoolean(SECTION + ".isolate-unlisted-servers", false);
    public final boolean updateLatency = getBoolean(SECTION + ".update-latency", false);
    @NotNull public final List<String> spyServers = getStringList(SECTION + ".spy-servers",
            Collections.singletonList("spyserver1")).stream().map(String::toLowerCase).collect(Collectors.toList());
    @NotNull public final Map<String, List<String>> sharedServers = new HashMap<>();

    public GlobalPlayerListConfiguration(@NotNull ConfigurationFile config) {
        super(config);
        checkForUnknownKey(SECTION, Arrays.asList("enabled", "display-others-as-spectators", "display-vanished-players-as-spectators",
                "isolate-unlisted-servers", "update-latency", "spy-servers", "shared-playerlist-world-groups"));
        for (Object worldGroup : getMap(SECTION + ".server-groups", Collections.emptyMap()).keySet()) {
            sharedServers.put(worldGroup.toString(), getStringList(SECTION + ".server-groups", Collections.emptyList()));
        }
    }
}
