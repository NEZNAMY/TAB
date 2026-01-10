package me.neznamy.tab.shared.data;

import lombok.Getter;
import me.neznamy.tab.shared.features.globalplayerlist.GlobalPlayerListConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Class for storing servers and worlds, as well as other data related to them.
 */
@Getter
public class DataManager {

    /** Map of all servers, indexed by their name */
    private final Map<String, Server> servers = new HashMap<>();

    /** Map of all server groups defined in global playerlist configuration */
    private final Map<String, ServerGroup> serverGroups = new HashMap<>();

    /** Default server group used for grouping unlisted servers */
    private final ServerGroup defaultServerGroup = new ServerGroup("DEFAULT", new ArrayList<>());

    /** Map of all worlds, indexed by their name */
    private final Map<String, World> worlds = new HashMap<>();

    /** Global playerlist configuration, null if not loaded yet or feature is disabled */
    @Nullable
    private GlobalPlayerListConfiguration globalPlayerListConfiguration;

    /**
     * Applies global playerlist configuration to data manager, marking spy-servers and server groups.
     *
     * @param   configuration
     *          Global playerlist configuration to apply
     */
    public void applyConfiguration(@NotNull GlobalPlayerListConfiguration configuration) {
        globalPlayerListConfiguration = configuration;
        for (String server : configuration.getSpyServers()) {
            servers.computeIfAbsent(server, Server::new).markSpyServer();
        }

        for (Map.Entry<String, List<String>> entry : configuration.getSharedServers().entrySet()) {
            serverGroups.put(entry.getKey(), new ServerGroup(entry.getKey(), entry.getValue()));
        }

        for (Server server : servers.values()) {
            server.setServerGroup(computeServerGroup(server));
        }
    }

    @Nullable
    ServerGroup computeServerGroup(@NotNull Server server) {
        if (globalPlayerListConfiguration == null) return null;
        for (ServerGroup group : serverGroups.values()) {
            for (String serverDefinition : group.getPatterns()) {
                if (matchesServerPattern(server.getName(), serverDefinition)) {
                    return group;
                }
            }
        }
        if (globalPlayerListConfiguration.isIsolateUnlistedServers()) {
            return new ServerGroup("", Collections.emptyList()); // Values are not used, just identity is compared
        } else {
            return defaultServerGroup;
        }
    }

    /**
     * Checks if a server name matches the given pattern. Supports:
     * - Exact match: "lobby"
     * - Prefix wildcard: "lobby*"
     * - Suffix wildcard: "*lobby"
     * - Regex pattern: "regex:lobby-[0-9]+"
     *
     * @param   serverName
     *          Server name to check
     * @param   pattern
     *          Pattern to match against
     * @return  {@code true} if server name matches the pattern, {@code false} otherwise
     */
    public boolean matchesServerPattern(@NotNull String serverName, @NotNull String pattern) {
        if (pattern.startsWith("regex:")) {
            try {
                return Pattern.compile(pattern.substring(6)).matcher(serverName).matches();
            } catch (PatternSyntaxException e) {
                // Invalid regex pattern, treat as literal match
                return serverName.equals(pattern);
            }
        } else if (pattern.endsWith("*")) {
            return serverName.startsWith(pattern.substring(0, pattern.length()-1));
        } else if (pattern.startsWith("*")) {
            return serverName.endsWith(pattern.substring(1));
        } else {
            return serverName.equals(pattern);
        }
    }
}
