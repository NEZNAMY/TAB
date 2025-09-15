package me.neznamy.tab.shared.data;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * Class storing information about a server. Instances are re-used,
 * so identity comparison is available to see if two servers are the same.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Server {

    /** Name of the server */
    @NonNull
    private final String name;

    /** Flag tracking whether this server is marked as spy-server in global playerlist configuration or not */
    private boolean isSpyServer;

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
        return TAB.getInstance().getDataManager().getServers().computeIfAbsent(name, Server::new);
    }

    /**
     * Marks this server as spy-server defined in global playerlist configuration.
     */
    public void markSpyServer() {
        isSpyServer = true;
    }
}
