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
    public void applyConfiguration(@Nullable GlobalPlayerListConfiguration configuration) {
        globalPlayerListConfiguration = configuration;
        if (configuration != null) {
            for (String server : configuration.getSpyServers()) {
                servers.computeIfAbsent(server, Server::new).markSpyServer();
            }
            for (Map.Entry<String, List<String>> entry : configuration.getSharedServers().entrySet()) {
                serverGroups.put(entry.getKey(), new ServerGroup(entry.getKey(), entry.getValue()));
            }
        }
        for (Server server : servers.values()) {
            server.setServerGroup(computeServerGroup(server));
        }
    }

    /**
     * Calculates global playerlist group of the specified server.
     * If global playerlist is disabled, returns {@code null}.
     * If the server is not part of any group, returns either default value
     * or a new group for isolating unlisted servers, depending on configuration.
     *
     * @param   server
     *          Server to compute group for
     * @return  Global playerlist group of the server, or {@code null} if global playerlist is disabled
     */
    @Nullable
    ServerGroup computeServerGroup(@NotNull Server server) {
        if (globalPlayerListConfiguration == null) return null;
        for (ServerGroup group : serverGroups.values()) {
            for (String serverDefinition : group.getPatterns()) {
                if (matchesPattern(server.getName(), serverDefinition)) {
                    return group;
                }
            }
        }
        if (globalPlayerListConfiguration.isIsolateUnlistedServers()) {
            return new ServerGroup("<isolated: " + server.getName() + ">", Collections.emptyList()); // Values are not used, just identity is compared
        } else {
            return ServerGroup.DEFAULT;
        }
    }

    /**
     * Checks if name of an object (server / world) matches the given pattern. Supports:
     * - Exact match: "lobby"
     * - Prefix wildcard: "lobby*"
     * - Suffix wildcard: "*lobby"
     * - Regex pattern: "regex:lobby-[0-9]+"
     *
     * @param   objectName
     *          Server / world name to check
     * @param   pattern
     *          Pattern to match against
     * @return  {@code true} if name matches the pattern, {@code false} otherwise
     */
    public boolean matchesPattern(@NotNull String objectName, @NotNull String pattern) {
        if (pattern.startsWith("regex:")) {
            try {
                return Pattern.compile(pattern.substring(6)).matcher(objectName).matches();
            } catch (PatternSyntaxException e) {
                // Invalid regex pattern, treat as literal match
                return objectName.equals(pattern);
            }
        } else if (pattern.endsWith("*")) {
            return objectName.startsWith(pattern.substring(0, pattern.length()-1));
        } else if (pattern.startsWith("*")) {
            return objectName.endsWith(pattern.substring(1));
        } else {
            return objectName.equals(pattern);
        }
    }
}
