package me.neznamy.tab.shared.data;

import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Class storing information about a server. Instances are re-used,
 * so identity comparison is available to see if two servers are the same.
 */
@Getter
public class Server {

    /** Map of all servers, indexed by their name */
    private static final Map<String, Server> servers = new HashMap<>();

    /** Name of the server */
    @NonNull
    private final String name;

    /** Lowercase version of the server name for case-insensitive comparisons */
    @NonNull
    private final String nameLowerCase;

    /**
     * Constructs new instance with given name.
     * This constructor is private to ensure that instances are only created through
     * the {@link #byName(String)} method, which manages the server instances.
     *
     * @param   name
     *          Name of the server
     */
    private Server(@NonNull String name) {
        this.name = name;
        nameLowerCase = name.toLowerCase();
    }

    /**
     * Returns server with given name, creating it if it does not exist.
     *
     * @param   name
     *          Name of the server
     * @return  World instance with given name
     */
    @Contract("!null -> !null")
    public static Server byName(@Nullable String name) {
        if (name == null) return null;
        return servers.computeIfAbsent(name.toLowerCase(), Server::new);
    }
}
