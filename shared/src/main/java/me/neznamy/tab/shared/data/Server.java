package me.neznamy.tab.shared.data;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Class storing information about a server. Instances are re-used,
 * so identity comparison is available to see if two servers are the same.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Server {

    /** Map of all servers, indexed by their name */
    private static final Map<String, Server> servers = new HashMap<>();

    /** Name of the server */
    @NonNull
    private final String name;

    /**
     * Returns server with given name, creating it if it does not exist.
     *
     * @param   name
     *          Name of the server
     * @return  Server instance with given name
     */
    @Contract("!null -> !null")
    public static Server byName(@Nullable String name) {
        if (name == null) return null;
        return servers.computeIfAbsent(name, Server::new);
    }
}
